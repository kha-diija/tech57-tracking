"""
Point d'entrée FastAPI — orchestre :
  1. Détection d'intention "question chiffrée" -> requête SQL directe
     (rapide, fiable, pas de LLM impliqué).
  2. Sinon -> RAG : recherche pgvector dans les documents indexés + génération LLM.
Le tout personnalisé par le rôle (et le prénom) de l'utilisateur, transmis
de façon sécurisée par le Controller Spring Boot (jamais par Angular directement).

Le fallback "essaie en local, sinon Docker" (repris de votre version d'origine)
est ici explicite dans generate_with_fallback(), directement dans ce fichier.
"""

import traceback
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from langchain_ollama import ChatOllama
from pydantic import BaseModel

from config import LOCAL_OLLAMA_URL, DOCKER_OLLAMA_URL, LLM_MODEL_NAME, ROLE_LABELS
from sql_service import detecter_intention_sql
from rag.retrieval import build_context_prompt

app = FastAPI(title="Tech57 Tracking - IA Service (RAG)")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class ChatRequest(BaseModel):
    message: str
    role: str | None = None      # ex: "ADMINISTRATEUR" -- transmis par Spring Boot (depuis le JWT)
    prenom: str | None = None    # ex: "Assia" -- idem


class ChatResponse(BaseModel):
    response: str


def generate_with_fallback(prompt: str) -> str:
    """
    Tente de communiquer avec Ollama sur Local (127.0.0.1), puis bascule
    sur le nom de conteneur Docker si le premier échoue. Exactement la
    même logique que votre main.py d'origine, réutilisée ici pour la
    génération finale de réponse (modèle llama3.2).
    """
    # Option 1 : Local
    try:
        llm_local = ChatOllama(model=LLM_MODEL_NAME, base_url=LOCAL_OLLAMA_URL)
        res = llm_local.invoke(prompt)
        return res.content
    except Exception as local_err:
        print(f"[IA Service] Échec connexion Local ({LOCAL_OLLAMA_URL}): {local_err}")

    # Option 2 : Docker
    try:
        llm_docker = ChatOllama(model=LLM_MODEL_NAME, base_url=DOCKER_OLLAMA_URL)
        res = llm_docker.invoke(prompt)
        return res.content
    except Exception as docker_err:
        print(f"[IA Service] Échec connexion Docker ({DOCKER_OLLAMA_URL}): {docker_err}")

    raise RuntimeError(
        "Impossible de se connecter à Ollama (ni en local, ni via Docker). "
        "Vérifiez qu'Ollama est bien démarré."
    )


@app.get("/")
def root():
    return {"message": "Tech57 Tracking IA service (RAG) opérationnel", "model": LLM_MODEL_NAME}


@app.post("/api/ia/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    try:
        role_label = ROLE_LABELS.get(request.role, "")

        # 1. Tentative : question "chiffrée" connue -> réponse directe via SQL (sans LLM)
        reponse_sql = detecter_intention_sql(request.message)
        if reponse_sql is not None:
            salutation = f"Bonjour {role_label} !" if role_label else "Bonjour !"
            return ChatResponse(response=f"{salutation} {reponse_sql}")

        # 2. Sinon : RAG (recherche documentaire dans data/ + génération LLM)
        prompt = build_context_prompt(request.message, role_label, request.prenom)
        reponse = generate_with_fallback(prompt)
        return ChatResponse(response=reponse)

    except Exception as e:
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la communication avec l'assistant : {str(e)}",
        )


@app.post("/api/ia/reindex")
def reindex():
    """
    Relance l'indexation complète du dossier data/.
    À appeler manuellement (Swagger, Postman) après avoir ajouté/supprimé
    un fichier Excel/PDF/CSV/TXT dans ia/data/.
    """
    from ingest import run
    try:
        run()
        return {"message": "Réindexation terminée."}
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)