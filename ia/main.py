import traceback
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from langchain_ollama import ChatOllama
from pydantic import BaseModel

app = FastAPI(title="Tech57 Tracking - IA Service")

# Configuration CORS pour autoriser Swagger, Spring Boot et Angular
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configuration des deux URLs (Local et Docker)
LOCAL_OLLAMA_URL = "http://127.0.0.1:11434"
DOCKER_OLLAMA_URL = "http://tech57-ollama:11434"

# Utilisation du modèle présent sur votre machine (ollama list)
MODEL_NAME = "llama3.2:latest"

# Schémas Pydantic
class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    response: str


def get_llm_response(prompt: str) -> str:
    """Tente de communiquer avec Ollama sur Local, puis bascule sur Docker si échec."""
    # Option 1 : Essai en Local
    try:
        llm_local = ChatOllama(model=MODEL_NAME, base_url=LOCAL_OLLAMA_URL)
        res = llm_local.invoke(prompt)
        return res.content
    except Exception as local_err:
        print(f"[IA Service] Échec connexion Local ({LOCAL_OLLAMA_URL}): {local_err}")

    # Option 2 : Essai avec le nom de conteneur Docker
    try:
        llm_docker = ChatOllama(model=MODEL_NAME, base_url=DOCKER_OLLAMA_URL)
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
    return {
        "message": "Tech57 Tracking IA service est opérationnel",
        "model": MODEL_NAME
    }


@app.post("/api/ia/chat", response_model=ChatResponse)
def chat_with_ollama(request: ChatRequest):
    try:
        system_prompt = (
            "Tu es l'assistant virtuel de l'application Tech57 Tracking. "
            "Tu aides les utilisateurs avec leurs questions sur la gestion des matériaux, "
            "des trajets, des équipements et des maintenances. "
            "Réponds de manière amicale, simple, claire et concise en français.\n\n"
        )
        
        prompt_complet = f"{system_prompt}Utilisateur : {request.message}"
        
        reply_content = get_llm_response(prompt_complet)
        return ChatResponse(response=reply_content)

    except Exception as e:
        # Affiche la stack trace complète dans le terminal FastAPI
        traceback.print_exc()
        raise HTTPException(
            status_code=500, 
            detail=f"Erreur lors de la communication avec l'assistant : {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)