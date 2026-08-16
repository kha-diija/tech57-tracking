"""
Génération des embeddings (texte -> vecteur de 768 nombres) via Ollama.

C'est un modèle DIFFÉRENT du modèle de chat (llama3.2). nomic-embed-text
est spécialisé pour produire des vecteurs sémantiques ; il ne "discute" pas,
il ne fait que transformer un texte en liste de nombres.

Prérequis : ollama pull nomic-embed-text
"""

from langchain_ollama import OllamaEmbeddings
from config import LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL, EMBEDDING_MODEL_NAME


def _get_embedder(base_url: str) -> OllamaEmbeddings:
    return OllamaEmbeddings(model=EMBEDDING_MODEL_NAME, base_url=base_url)


def embed_text(text: str) -> list[float]:
    """
    Transforme UN texte (ex: la question de l'utilisateur) en vecteur.
    Même logique de fallback local -> docker que pour le chat.
    """
    for url in (LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL):
        try:
            embedder = _get_embedder(url)
            return embedder.embed_query(text)
        except Exception as e:
            print(f"[embeddings] Échec avec {url} : {e}")
    raise RuntimeError("Impossible de générer l'embedding (Ollama injoignable).")


def embed_texts(texts: list[str]) -> list[list[float]]:
    """Version batch (plusieurs textes d'un coup), utilisée pendant l'indexation."""
    for url in (LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL):
        try:
            embedder = _get_embedder(url)
            return embedder.embed_documents(texts)
        except Exception as e:
            print(f"[embeddings] Échec batch avec {url} : {e}")
    raise RuntimeError("Impossible de générer les embeddings (Ollama injoignable).")