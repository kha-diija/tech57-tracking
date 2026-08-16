"""
Configuration centralisée du service IA.

NOTE : valeurs en dur pour l'instant (pas de .env), à la demande.
À migrer vers des variables d'environnement avant la mise en prod,
surtout DB_PASSWORD qui donne un accès direct à votre base Neon.
"""

# --- Ollama : deux URLs possibles selon que le service tourne en local ou en Docker ---
LOCAL_OLLAMA_URL = "http://127.0.0.1:11434"
DOCKER_OLLAMA_URL = "http://tech57-ollama:11434"

LLM_MODEL_NAME = "llama3.2:latest"          # génère la réponse finale (texte -> texte)
EMBEDDING_MODEL_NAME = "nomic-embed-text"   # transforme un texte en vecteur de 768 nombres
# Prérequis : ollama pull nomic-embed-text

# --- Neon PostgreSQL : MÊME base que Spring Boot (tables document_source / document_chunk
#     + tables métier comme materiel, mission_installation, etablissement...) ---
# Ce sont les mêmes infos que dans application.properties, juste au format psycopg2
# (pas de préfixe jdbc:, pas de paramètres dans l'URL).
DB_HOST = "ep-quiet-math-aglw807t-pooler.c-2.eu-central-1.aws.neon.tech"
DB_NAME = "neondb"
DB_USER = "neondb_owner"
DB_PASSWORD = "npg_JN2sakXUmlf8"
DB_PORT = 5432
DB_SSLMODE = "require"

# --- Paramètres du découpage en chunks (voir ingestion/chunker.py) ---
CHUNK_SIZE_WORDS = 200       # taille approximative d'un chunk, en mots
CHUNK_OVERLAP_WORDS = 50     # chevauchement entre deux chunks consécutifs

# --- Paramètres de la recherche vectorielle ---
TOP_K_CHUNKS = 3           # nombre de chunks les plus pertinents renvoyés au LLM

# --- Dossier contenant les fichiers à indexer ---
DATA_DIR = "./data"

# --- Libellés affichés selon le rôle (discriminant type_utilisateur côté Spring) ---
ROLE_LABELS = {
    "ADMINISTRATEUR": "Administrateur",
    "TECHNICIEN": "Technicien",
    "OBSERVATEUR": "Observateur",
    "GESTIONNAIRE_STOCK": "Gestionnaire de stock",
}