import time
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
    role: str | None = None
    prenom: str | None = None


class ChatResponse(BaseModel):
    response: str


def generate_with_fallback(prompt: str) -> str:
    print(f"[LLM] 🤖 Étape finale : génération de la réponse via {LLM_MODEL_NAME}...")

    print(f"[LLM] 🔌 Tentative Local ({LOCAL_OLLAMA_URL})...")
    t0 = time.perf_counter()
    try:
        llm_local = ChatOllama(model=LLM_MODEL_NAME, base_url=LOCAL_OLLAMA_URL, keep_alive="30m")
        res = llm_local.invoke(prompt)
        t1 = time.perf_counter()
        print(f"[LLM] ✅ Réponse générée via Local en {t1 - t0:.2f}s")
        return res.content
    except Exception as local_err:
        t1 = time.perf_counter()
        print(f"[LLM] ❌ Échec Local après {t1 - t0:.2f}s : {local_err}")

    print(f"[LLM] 🔌 Tentative Docker ({DOCKER_OLLAMA_URL})...")
    t0 = time.perf_counter()
    try:
        llm_docker = ChatOllama(model=LLM_MODEL_NAME, base_url=DOCKER_OLLAMA_URL, keep_alive="30m")
        res = llm_docker.invoke(prompt)
        t1 = time.perf_counter()
        print(f"[LLM] ✅ Réponse générée via Docker en {t1 - t0:.2f}s")
        return res.content
    except Exception as docker_err:
        t1 = time.perf_counter()
        print(f"[LLM] ❌ Échec Docker après {t1 - t0:.2f}s : {docker_err}")

    raise RuntimeError(
        "Impossible de se connecter à Ollama (ni en local, ni via Docker). "
        "Vérifiez qu'Ollama est bien démarré."
    )


@app.get("/")
def root():
    return {"message": "Tech57 Tracking IA service (RAG) opérationnel", "model": LLM_MODEL_NAME}


@app.post("/api/ia/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    t_start = time.perf_counter()
    print(f"\n{'='*60}")
    print(f"[CHAT] 📩 Nouvelle question reçue : « {request.message} » (role={request.role})")
    try:
        role_label = ROLE_LABELS.get(request.role, "")

        print(f"[CHAT] 🔍 Vérification intention SQL directe...")
        t0 = time.perf_counter()
        reponse_sql = detecter_intention_sql(request.message)
        t1 = time.perf_counter()

        if reponse_sql is not None:
            print(f"[CHAT] ✅ Intention SQL détectée et traitée en {t1 - t0:.2f}s (pas de LLM)")
            salutation = f"Bonjour {role_label} !" if role_label else "Bonjour !"
            total = time.perf_counter() - t_start
            print(f"[CHAT] 🏁 Requête terminée en {total:.2f}s au total")
            print(f"{'='*60}\n")
            return ChatResponse(response=f"{salutation} {reponse_sql}")

        print(f"[CHAT] ↪️ Pas d'intention SQL trouvée ({t1 - t0:.2f}s), bascule sur le RAG")
        prompt = build_context_prompt(request.message, role_label, request.prenom)
        reponse = generate_with_fallback(prompt)

        total = time.perf_counter() - t_start
        print(f"[CHAT] 🏁 Requête terminée en {total:.2f}s au total")
        print(f"{'='*60}\n")
        return ChatResponse(response=reponse)

    except Exception as e:
        total = time.perf_counter() - t_start
        print(f"[CHAT] 💥 Erreur après {total:.2f}s : {e}")
        print(f"{'='*60}\n")
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la communication avec l'assistant : {str(e)}",
        )


@app.post("/api/ia/reindex")
def reindex():
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