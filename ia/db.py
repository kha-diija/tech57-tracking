"""
Connexion à Neon PostgreSQL + utilitaires.

Pourquoi psycopg2 et pas un ORM ici ?
Le service Python n'a pas besoin d'un ORM complet comme JPA côté Spring :
on fait des requêtes ciblées sur document_source / document_chunk (RAG)
et quelques lectures directes sur les tables métier. psycopg2 + pgvector
suffit et reste simple à lire de bout en bout.

register_vector() permet à psycopg2 de convertir automatiquement une
liste Python [0.12, -0.5, ...] vers/depuis le type "vector" de pgvector,
sans conversion manuelle.
"""

import psycopg2
import psycopg2.extras
from pgvector.psycopg2 import register_vector
from contextlib import contextmanager

from config import DB_HOST, DB_NAME, DB_USER, DB_PASSWORD, DB_PORT, DB_SSLMODE


def _connect():
    conn = psycopg2.connect(
        host=DB_HOST,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        port=DB_PORT,
        sslmode=DB_SSLMODE,
    )
    register_vector(conn)
    return conn


@contextmanager
def get_connection():
    """
    Usage :
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute(...)

    Commit automatique si tout se passe bien, rollback + fermeture propre sinon.
    """
    conn = _connect()
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def fetch_all(query: str, params: tuple = ()):
    """Exécute un SELECT et renvoie une liste de dicts {nom_colonne: valeur}."""
    with get_connection() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(query, params)
            return cur.fetchall()