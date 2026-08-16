"""
Génération des embeddings (texte -> vecteur de 768 nombres) via Ollama.
"""

import time
from langchain_ollama import OllamaEmbeddings
from config import LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL, EMBEDDING_MODEL_NAME


def _get_embedder(base_url: str) -> OllamaEmbeddings:
    return OllamaEmbeddings(model=EMBEDDING_MODEL_NAME, base_url=base_url, keep_alive=1800)


def embed_text(text: str) -> list[float]:
    for url in (LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL):
        print(f"[EMBEDDINGS] 🔌 Tentative de connexion à {url} (modèle={EMBEDDING_MODEL_NAME})...")
        t0 = time.perf_counter()
        try:
            embedder = _get_embedder(url)
            result = embedder.embed_query(text)
            t1 = time.perf_counter()
            print(f"[EMBEDDINGS] ✅ Succès via {url} en {t1 - t0:.2f}s")
            return result
        except Exception as e:
            t1 = time.perf_counter()
            print(f"[EMBEDDINGS] ❌ Échec avec {url} après {t1 - t0:.2f}s : {e}")
    raise RuntimeError("Impossible de générer l'embedding (Ollama injoignable).")


def embed_texts(texts: list[str]) -> list[list[float]]:
    for url in (LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL):
        print(f"[EMBEDDINGS] 🔌 Tentative batch ({len(texts)} textes) via {url}...")
        t0 = time.perf_counter()
        try:
            embedder = _get_embedder(url)
            result = embedder.embed_documents(texts)
            t1 = time.perf_counter()
            print(f"[EMBEDDINGS] ✅ Batch réussi via {url} en {t1 - t0:.2f}s")
            return result
        except Exception as e:
            t1 = time.perf_counter()
            print(f"[EMBEDDINGS] ❌ Échec batch avec {url} après {t1 - t0:.2f}s : {e}")
    raise RuntimeError("Impossible de générer les embeddings (Ollama injoignable).")