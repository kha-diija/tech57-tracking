from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from langchain_ollama import ChatOllama
from pydantic import BaseModel

app = FastAPI(title="Tech57 Tracking - IA Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Connexion à Ollama via le port exposé par Docker sur votre machine
llm = ChatOllama(
    model="llama3",
    base_url="http://localhost:11434"
)

class ChatRequest(BaseModel):
    message: str

@app.post("/api/ia/chat")
def chat_with_ollama(request: ChatRequest):
    # Envoie le message au modèle Llama3 dans le conteneur Docker
    response = llm.invoke(request.message)
    return {"response": response.content}