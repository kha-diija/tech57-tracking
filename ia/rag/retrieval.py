"""
Phase B du RAG — au moment de la question (pas de l'indexation) :
recherche des chunks les plus pertinents, puis construction du prompt
final envoyé au LLM de génération.
"""

from db import get_connection
from rag.embeddings import embed_text
from config import TOP_K_CHUNKS


def retrieve_relevant_chunks(question: str) -> list[str]:
    """
    1. Transforme la question en vecteur.
    2. Cherche dans document_chunk les vecteurs les plus PROCHES du vecteur
       de la question (opérateur <=> = distance cosinus, fourni par pgvector).
    3. Renvoie les k contenus texte les plus pertinents.

    NOTE (cast ::vector) : psycopg2 adapte une liste Python en ARRAY[...]
    (typé numeric[] par défaut), pas en type "vector". Dans un INSERT,
    PostgreSQL déduit le type depuis la colonne cible et caste tout seul.
    Mais ici, dans ORDER BY embedding <=> %s, il n'y a pas de colonne cible
    pour déduire le type du paramètre -> il faut caster explicitement avec
    %s::vector, sinon l'opérateur <=> ne trouve aucune signature
    correspondante (vector <=> numeric[] n'existe pas).
    """
    vecteur_question = embed_text(question)

    with get_connection() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT contenu
                FROM document_chunk
                ORDER BY embedding <=> %s::vector
                LIMIT %s;
                """,
                (vecteur_question, TOP_K_CHUNKS),
            )
            rows = cur.fetchall()

    return [row[0] for row in rows]


def build_context_prompt(question: str, role_label: str, prenom: str | None) -> str:
    """Construit le prompt final envoyé au LLM, avec le contexte RAG injecté et le rôle."""
    chunks = retrieve_relevant_chunks(question)

    if chunks:
        contexte = "\n\n".join(f"- {c}" for c in chunks)
        bloc_contexte = (
            "Voici des extraits pertinents issus des documents internes de Tech57 :\n"
            f"{contexte}\n\n"
            "Réponds à la question en te basant PRIORITAIREMENT sur ces extraits. "
            "Si les extraits ne suffisent pas pour répondre, dis-le clairement plutôt "
            "que d'inventer une réponse.\n\n"
        )
    else:
        bloc_contexte = (
            "Aucun document interne pertinent n'a été trouvé pour cette question. "
            "Réponds avec tes connaissances générales, en précisant que ce n'est pas "
            "issu des documents internes.\n\n"
        )

    salutation = (
        f"L'utilisateur connecté est {role_label}" + (f" ({prenom})" if prenom else "") + ".\n"
        if role_label else ""
    )

    system_prompt = (
        "Tu es l'assistant virtuel de l'application Tech57 Tracking. "
        "Tu aides les utilisateurs avec leurs questions sur la gestion des matériaux, "
        "des trajets, des équipements et des maintenances. "
        "Réponds de manière amicale, simple, claire et concise en français.\n"
        f"{salutation}"
        f"{bloc_contexte}"
    )

    return f"{system_prompt}Question de l'utilisateur : {question}"