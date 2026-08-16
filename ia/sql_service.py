"""
Interrogation DIRECTE de la base (sans RAG, sans LLM) pour les questions
"chiffrées" : compter, lister, filtrer. Plus fiable et plus rapide qu'un
passage par le LLM pour ce type de question.

Principe volontairement SÉCURISÉ : on ne demande JAMAIS au LLM de générer
du SQL librement (risque d'injection SQL et d'hallucination de colonnes
inexistantes). On fait correspondre des INTENTIONS connues (mots-clés dans
la question) à des requêtes SQL fixes et paramétrées.

Corrigé d'après vos vraies entités :
- Materiel   : PAS de colonne "statut" -> colonne "etat" (défaut "Neuf").
               Il n'y a pas de notion binaire "disponible/indisponible"
               dans ce schéma. La disponibilité réelle semble se déduire
               de SortieMateriel / RetourMateriel (une sortie sans retour
               correspondant = matériel actuellement sorti). Je n'ai pas
               encore vu ces deux entités en détail -> montrez-les moi si
               vous voulez une vraie requête "combien de matériel disponible".
               En attendant, je fournis une requête par "etat".
- MissionInstallation : colonne "statut", valeur par défaut "Planifiée".
               Je ne connais pas la liste exacte des statuts possibles
               (Planifiée / En cours / Terminée / Annulée ?) -> à confirmer.

NOTE (accents) : les utilisateurs ne tapent pas toujours les accents
("materiel" au lieu de "matériel"). On normalise donc systématiquement
la question ET les mots-clés de référence (minuscule + suppression des
accents) avant toute comparaison, via _normaliser().

NOTE (ordre de routage) : les intentions les plus SPÉCIFIQUES (matériel
par état précis, mission par statut précis) sont testées AVANT les
intentions génériques ("combien de matériel" -> total). Sinon une
question comme "Combien de matériel en panne ?" matchait d'abord le cas
générique (mots-clés "combien" + "matériel") et ne renvoyait jamais le
compte filtré par état.
"""

import unicodedata
from db import fetch_all


def _normaliser(texte: str) -> str:
    """
    Minuscule + suppression des accents, pour que 'matériel', 'materiel',
    'MATÉRIEL' soient traités comme équivalents lors du matching de
    mots-clés (et pareil pour les valeurs d'état/statut : 'Planifiée' /
    'planifiee').
    """
    texte = texte.lower()
    texte_sans_accents = unicodedata.normalize("NFKD", texte)
    return "".join(c for c in texte_sans_accents if not unicodedata.combining(c))


def _count_materiel_total() -> str:
    rows = fetch_all("SELECT COUNT(*) AS total FROM materiel;")
    return f"Il y a {rows[0]['total']} matériel(s) au total dans le système."


def _count_materiel_par_etat(etat: str) -> str:
    rows = fetch_all(
        "SELECT COUNT(*) AS total FROM materiel WHERE etat = %s;",
        (etat,),
    )
    return f"Il y a {rows[0]['total']} matériel(s) avec l'état « {etat} »."


def _count_missions_par_statut(statut: str) -> str:
    rows = fetch_all(
        "SELECT COUNT(*) AS total FROM mission_installation WHERE statut = %s;",
        (statut,),
    )
    return f"Il y a {rows[0]['total']} mission(s) avec le statut « {statut} »."


def _liste_etablissements() -> str:
    rows = fetch_all("SELECT nom FROM etablissement ORDER BY nom;")
    noms = [r["nom"] for r in rows]
    if not noms:
        return "Aucun établissement trouvé."
    return "Établissements enregistrés : " + ", ".join(noms)


def _liste_missions_par_etablissement(nom_etablissement: str) -> str:
    rows = fetch_all(
        """
        SELECT m.titre, m.statut
        FROM mission_installation m
        JOIN etablissement e ON e.id_etablissement = m.id_etablissement
        WHERE LOWER(e.nom) LIKE LOWER(%s)
        ORDER BY m.date_creation DESC;
        """,
        (f"%{nom_etablissement}%",),
    )
    if not rows:
        return f"Aucune mission trouvée pour un établissement contenant « {nom_etablissement} »."
    return "Missions trouvées : " + "; ".join(f"{r['titre']} ({r['statut']})" for r in rows)


# Table de routage : (mots-clés qui doivent TOUS apparaître, fonction sans argument)
# Les mots-clés sont écrits avec accents pour rester lisibles ; ils sont
# normalisés à la volée dans detecter_intention_sql().
INTENTIONS_SQL_SIMPLES = [
    (["combien", "matériel"], _count_materiel_total),
    (["liste", "établissement"], _liste_etablissements),
]

# Mots-clés -> valeur exacte de la colonne "etat" (Materiel) ou "statut" (MissionInstallation)
ETATS_MATERIEL_CONNUS = ["Neuf", "Bon état", "Usé", "En panne", "Hors service"]
STATUTS_MISSION_CONNUS = ["Planifiée", "En cours", "Terminée", "Annulée"]


def detecter_intention_sql(question: str) -> str | None:
    """
    Renvoie la réponse toute faite si la question correspond à une intention
    SQL connue, sinon None (on bascule alors sur le RAG dans main.py).
    """
    q = _normaliser(question)

    # 1. "Combien de mission(s) <statut> ?" -> testé en premier car plus spécifique
    if "mission" in q and ("combien" in q or "nombre" in q):
        for statut in STATUTS_MISSION_CONNUS:
            if _normaliser(statut) in q:
                try:
                    return _count_missions_par_statut(statut)
                except Exception as e:
                    print(f"[sql_service] Erreur requête SQL : {e}")
                    return None

    # 2. "Combien de matériel <etat> ?" -> testé avant le cas générique
    if "materiel" in q and ("combien" in q or "nombre" in q):
        for etat in ETATS_MATERIEL_CONNUS:
            if _normaliser(etat) in q:
                try:
                    return _count_materiel_par_etat(etat)
                except Exception as e:
                    print(f"[sql_service] Erreur requête SQL : {e}")
                    return None

    # 3. Cas simples génériques (sans paramètre)
    for mots_cles, fonction in INTENTIONS_SQL_SIMPLES:
        if all(_normaliser(mot) in q for mot in mots_cles):
            try:
                return fonction()
            except Exception as e:
                print(f"[sql_service] Erreur requête SQL : {e}")
                return None

    return None