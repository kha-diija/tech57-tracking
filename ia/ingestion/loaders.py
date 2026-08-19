"""
Extraction du texte brut à partir des fichiers du dossier ia/data/.

Chaque loader renvoie une simple chaîne de texte. Le découpage en chunks
se fait séparément dans chunker.py (une responsabilité par fichier =
plus facile à tester et à déboguer indépendamment).
"""

import os
import pandas as pd
from pypdf import PdfReader


def load_txt(path: str) -> str:
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def load_pdf(path: str) -> str:
    reader = PdfReader(path)
    pages = [page.extract_text() or "" for page in reader.pages]
    return "\n".join(pages)


def load_csv(path: str) -> str:
    """
    Transforme un CSV en texte "une ligne = une phrase clé=valeur", pour que
    la recherche vectorielle puisse retrouver une ligne précise (une mission,
    un matériel...) par le sens plutôt que par correspondance exacte de mot.

    Exemple de ligne produite :
    "id_materiel=12 | nom=Ordinateur portable Dell | statut=DISPONIBLE"
    """
    df = pd.read_csv(path)
    lignes = [
        " | ".join(f"{col}={row[col]}" for col in df.columns)
        for _, row in df.iterrows()
    ]
    return "\n".join(lignes)


def load_excel(path: str) -> str:
    """Même principe que le CSV, mais parcourt toutes les feuilles du fichier Excel."""
    sheets = pd.read_excel(path, sheet_name=None)  # dict {nom_feuille: DataFrame}
    lignes = []
    for nom_feuille, df in sheets.items():
        lignes.append(f"--- Feuille: {nom_feuille} ---")
        for _, row in df.iterrows():
            lignes.append(" | ".join(f"{col}={row[col]}" for col in df.columns))
    return "\n".join(lignes)


LOADERS_PAR_EXTENSION = {
    ".txt": load_txt,
    ".pdf": load_pdf,
    ".csv": load_csv,
    ".xlsx": load_excel,
    ".xls": load_excel,
}


def load_file(path: str) -> str:
    ext = os.path.splitext(path)[1].lower()
    loader = LOADERS_PAR_EXTENSION.get(ext)
    if loader is None:
        raise ValueError(f"Type de fichier non supporté : {ext}")
    return loader(path)