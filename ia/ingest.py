"""
Script d'INDEXATION (Phase A du RAG).

À relancer à chaque fois que vous ajoutez / modifiez / supprimez un fichier
dans ia/data/ (PDF, CSV, TXT, XLSX).

STRATÉGIE : UPSERT par fichier, PAS de suppression en bloc de document_source
(référencé par observateur_document -> un DELETE en masse casserait la FK).

Valeurs autorisées par les CHECK constraints réelles de votre base (Neon) :
  - type_source        : 'EXCEL', 'PDF', 'MANUEL', 'WEB'
  - statut_indexation   : 'EN_ATTENTE', 'EN_COURS', 'INDEXE', 'ERREUR'

Comme il n'existe pas de catégorie CSV/TXT dédiée, on mappe :
  - .pdf          -> 'PDF'
  - .xlsx / .xls  -> 'EXCEL'
  - .csv          -> 'EXCEL' (données tabulaires, catégorie la plus proche)
  - .txt          -> 'MANUEL' (texte libre / notes)

Et comme il n'existe pas de statut "fichier absent" : pour un fichier qui a
disparu de data/, on NE touche PAS au statut_indexation. On vide juste ses
chunks (document_chunk n'est référencé par personne d'autre, donc sans
risque) -> le document ne remontera plus jamais dans les recherches RAG
puisqu'il n'a plus aucun chunk, tout en gardant la ligne document_source
intacte pour ne pas casser observateur_document.

Usage :
    cd ia
    python ingest.py

Ou via l'API : POST http://localhost:8000/api/ia/reindex
"""

import os
from config import DATA_DIR
from db import get_connection
from ingestion.loaders import load_file, LOADERS_PAR_EXTENSION
from ingestion.chunker import split_into_chunks
from rag.embeddings import embed_texts

TYPE_SOURCE_PAR_EXTENSION = {
    ".pdf": "PDF",
    ".xlsx": "EXCEL",
    ".xls": "EXCEL",
    ".csv": "EXCEL",
    ".txt": "MANUEL",
}


def _get_source_existante(cur, nom_fichier: str):
    cur.execute(
        "SELECT id_source FROM document_source WHERE nom_fichier = %s;",
        (nom_fichier,),
    )
    row = cur.fetchone()
    return row[0] if row else None


def index_file(path: str):
    filename = os.path.basename(path)
    ext = os.path.splitext(path)[1].lower()

    if ext not in LOADERS_PAR_EXTENSION:
        print(f"[ingest] Ignoré (type non supporté) : {filename}")
        return

    print(f"[ingest] Lecture : {filename}")
    texte = load_file(path)

    chunks_texte = split_into_chunks(texte)
    if not chunks_texte:
        print("[ingest]   -> aucun contenu exploitable, ignoré.")
        return

    print(f"[ingest]   -> {len(chunks_texte)} chunks, génération des embeddings...")
    vecteurs = embed_texts(chunks_texte)

    type_source = TYPE_SOURCE_PAR_EXTENSION[ext]

    with get_connection() as conn:
        with conn.cursor() as cur:
            id_source = _get_source_existante(cur, filename)

            if id_source is not None:
                # Fichier déjà connu -> on ne touche pas document_source (relations
                # externes préservées), on remplace juste ses chunks.
                cur.execute("DELETE FROM document_chunk WHERE id_source = %s;", (id_source,))
                cur.execute(
                    """
                    UPDATE document_source
                    SET date_import = NOW(), statut_indexation = 'INDEXE', chemin_fichier = %s
                    WHERE id_source = %s;
                    """,
                    (path, id_source),
                )
            else:
                # Nouveau fichier -> nouvelle ligne document_source
                cur.execute(
                    """
                    INSERT INTO document_source
                        (nom_fichier, type_source, chemin_fichier, date_import, statut_indexation)
                    VALUES (%s, %s, %s, NOW(), 'INDEXE')
                    RETURNING id_source;
                    """,
                    (filename, type_source, path),
                )
                id_source = cur.fetchone()[0]

            for contenu, vecteur in zip(chunks_texte, vecteurs):
                cur.execute(
                    """
                    INSERT INTO document_chunk
                        (id_source, contenu, embedding, metadata, date_creation)
                    VALUES (%s, %s, %s, %s, NOW());
                    """,
                    (id_source, contenu, vecteur, '{}'),
                )

    print(f"[ingest]   -> OK ({filename} indexé, source #{id_source})")


def marquer_fichiers_absents(fichiers_presents: set[str]):
    """
    Pour les sources en base dont le fichier n'est plus dans data/ :
    on vide leurs chunks (ils ne doivent plus remonter dans le RAG). On ne
    touche PAS statut_indexation (pas de valeur "absent" autorisée par la
    contrainte) ni la ligne document_source elle-même.
    """
    with get_connection() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT id_source, nom_fichier FROM document_source;")
            sources = cur.fetchall()

            for id_source, nom_fichier in sources:
                if nom_fichier not in fichiers_presents:
                    cur.execute("DELETE FROM document_chunk WHERE id_source = %s;", (id_source,))
                    print(f"[ingest] Fichier absent de data/ -> chunks retirés : {nom_fichier}")


def run():
    print(f"[ingest] Indexation (upsert) depuis : {DATA_DIR}")

    fichiers = [
        f for f in os.listdir(DATA_DIR)
        if os.path.isfile(os.path.join(DATA_DIR, f))
    ]

    for nom_fichier in fichiers:
        path = os.path.join(DATA_DIR, nom_fichier)
        try:
            index_file(path)
        except Exception as e:
            print(f"[ingest] ERREUR sur {path} : {e}")

    marquer_fichiers_absents(set(fichiers))

    print("[ingest] Terminé.")


if __name__ == "__main__":
    run()