"""
Phase B du RAG — au moment de la question (pas de l'indexation) :
recherche des chunks les plus pertinents, puis construction du prompt
final envoyé au LLM de génération.
"""

import time
from db import get_connection
from rag.embeddings import embed_text
from config import TOP_K_CHUNKS


def retrieve_relevant_chunks(question: str) -> list[str]:
    print(f"[RAG] 🔎 Étape 1/2 : génération de l'embedding de la question...")
    t0 = time.perf_counter()
    vecteur_question = embed_text(question)
    t1 = time.perf_counter()
    print(f"[RAG] ✅ Embedding généré en {t1 - t0:.2f}s (dimension={len(vecteur_question)})")

    print(f"[RAG] 🔎 Étape 2/2 : recherche des {TOP_K_CHUNKS} chunks les plus proches (pgvector)...")
    t2 = time.perf_counter()
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
    t3 = time.perf_counter()
    print(f"[RAG] ✅ Recherche pgvector terminée en {t3 - t2:.2f}s ({len(rows)} chunk(s) trouvé(s))")

    return [row[0] for row in rows]


def build_context_prompt(question: str, role_label: str, prenom: str | None) -> str:
    """Construit le prompt final envoyé au LLM, avec le contexte RAG injecté et le rôle."""
    print(f"[RAG] 📚 Construction du prompt pour la question : « {question} »")
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
        print(f"[RAG] 📎 {len(chunks)} chunk(s) injecté(s) dans le contexte")
    else:
        bloc_contexte = (
            "Aucun document interne pertinent n'a été trouvé pour cette question. "
            "Réponds avec tes connaissances générales, en précisant que ce n'est pas "
            "issu des documents internes.\n\n"
        )
        print(f"[RAG] ⚠️ Aucun chunk pertinent trouvé, fallback connaissances générales")

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

    print(f"[RAG] ✅ Prompt prêt ({len(system_prompt)} caractères), envoi au LLM...")
    return f"{system_prompt}Question de l'utilisateur : {question}"