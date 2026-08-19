"""
Découpage d'un texte long en chunks (morceaux) plus petits.

Pourquoi découper ? Un embedding représente le SENS d'un texte court.
Si on embeddait un PDF entier de 20 pages en un seul vecteur, ce vecteur
serait une moyenne floue de tout le document et on ne retrouverait plus
un passage précis. Des chunks de ~300-400 mots donnent des vecteurs bien
plus précis, et permettent de remonter exactement le bon passage.

L'overlap (chevauchement) évite de couper une idée en plein milieu entre
deux chunks consécutifs.
"""

from config import CHUNK_SIZE_WORDS, CHUNK_OVERLAP_WORDS


def split_into_chunks(text: str) -> list[str]:
    mots = text.split()
    if not mots:
        return []

    chunks = []
    debut = 0
    while debut < len(mots):
        fin = debut + CHUNK_SIZE_WORDS
        chunk = " ".join(mots[debut:fin])
        if chunk.strip():
            chunks.append(chunk)
        debut += CHUNK_SIZE_WORDS - CHUNK_OVERLAP_WORDS  # on recule -> overlap

    return chunks