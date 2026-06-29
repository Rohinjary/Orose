# PLAN DE TEST — DONNÉES À SAISIR DANS L'INTERFACE OROSE

Objectif : valider 9 bassins, 2 cycles actifs (sur 3 prévus), pesées de 0g à 20g sur 6 bassins, module sanitaire, nourrissage/distribution, médicaments, récolte + stock.

---

## 1. MODULE 1 — BASSINS

### 1.1 Créer les 9 bassins

| # | Code | Surface (m²) | Profondeur (m) | Notes |
|---|------|--------------|-----------------|-------|
| 1 | B01 | 500 | 1.20 | — |
| 2 | B02 | 500 | 1.20 | — |
| 3 | B03 | 450 | 1.10 | — |
| 4 | B04 | 450 | 1.10 | — |
| 5 | B05 | 600 | 1.30 | — |
| 6 | B06 | 600 | 1.30 | — |
| 7 | B07 | 400 | 1.00 | Bassin témoin |
| 8 | B08 | 400 | 1.00 | — |
| 9 | B09 | 550 | 1.15 | — |

> Tous créés à l'état **PREPARATION** par défaut (automatique, rien à saisir).

---

### 1.2 Démarrer 2 cycles (sur les 3 prévus), chacun avec 3 bassins

#### Cycle 1 — C01

| Champ | Valeur |
|---|---|
| Code unique cycle | C01 |
| Espèce | Fenneropenaeus indicus |
| Date début | 01/01/2026 |
| Date fin prévue | 01/05/2026 *(4 mois)* |
| Technicien | Njary |

**Bassins associés au cycle C01 :**

| Bassin | Effectif initial | Densité (m²) | Coût post-larves (Ar) |
|---|---|---|---|
| B01 | 50 000 | 100 | 750 000 |
| B02 | 50 000 | 100 | 750 000 |
| B03 | 45 000 | 100 | 675 000 |

#### Cycle 2 — C02

| Champ | Valeur |
|---|---|
| Code unique cycle | C02 |
| Espèce | Fenneropenaeus indicus |
| Date début | 05/01/2026 |
| Date fin prévue | 05/05/2026 *(4 mois)* |
| Technicien | Mickael |

**Bassins associés au cycle C02 :**

| Bassin | Effectif initial | Densité (m²) | Coût post-larves (Ar) |
|---|---|---|---|
| B04 | 45 000 | 100 | 675 000 |
| B05 | 60 000 | 100 | 900 000 |
| B06 | 60 000 | 100 | 900 000 |

> B07, B08, B09 restent en **PREPARATION** — réservés pour le futur Cycle 3 (non testé ici).

---

### 1.3 Changement d'état — cas de test

| Bassin | Transition | Motif | Résultat attendu |
|---|---|---|---|
| B05 | ACTIF → EN_TRAITEMENT | "pH anormal détecté à l'œil" | OK, bassin orange |
| B05 | EN_TRAITEMENT → ACTIF | "Problème résolu après aération" | OK, bassin redevient vert |
| B03 | ACTIF → QUARANTAINE | Test manuel (avant test sanitaire automatique) | Vérifier blocage récolte/vente |
| B03 | QUARANTAINE → PREPARATION | "Incident clôturé" | OK uniquement si incident lié marqué résolu |

---

## 2. MODULE 2 — SUIVI BIOLOGIQUE (PESÉES)

But : tester la croissance de 0g à 20g sur les 6 bassins actifs (C01 + C02), avec calcul automatique de biomasse, taux de survie, taux de mortalité.

### 2.1 Série de pesées — Bassin B01 (Cycle C01)

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 08/01/2026 | 0.5 | 8 | 49 800 | 200 | Njary |
| 4 | 29/01/2026 | 2.8 | 25 | 49 200 | 600 | Njary |
| 8 | 26/02/2026 | 8.5 | 55 | 48 500 | 700 | Njary |
| 12 | 25/03/2026 | 14.5 | 85 | 47 800 | 700 | Njary |
| 16 | 22/04/2026 | 20.0 | 120 | 47 000 | 800 | Njary |

> À la semaine 16 : poids ≥ 15g ET taille ≥ 110mm → **récolte débloquée**.

### 2.2 Série de pesées — Bassin B02 (Cycle C01)

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 08/01/2026 | 0.5 | 8 | 49 700 | 300 | Njary |
| 6 | 12/02/2026 | 5.5 | 40 | 48 900 | 800 | Njary |
| 10 | 12/03/2026 | 11.5 | 70 | 48 200 | 700 | Njary |
| 14 | 09/04/2026 | 17.5 | 105 | 47 500 | 700 | Njary |

> Vérifier alerte "retard de croissance" si comparé à la courbe standard à la semaine 14 (poids cible 17.5g — ici pile dans la norme, ne devrait pas alerter).

### 2.3 Série de pesées — Bassin B03 (Cycle C01) — **bassin du test quarantaine**

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 08/01/2026 | 0.5 | 8 | 44 800 | 200 | Njary |
| 5 | 05/02/2026 | 4.0 | 32 | 43 000 | 1 800 | Njary |

> Mortalité élevée volontaire (1800/44800 ≈ 4%) → doit déclencher une **alerte rouge mortalité anormale** après S8 (ici avant S8, donc tester aussi la règle J1-J28 si applicable selon ta date de démarrage).

### 2.4 Série de pesées — Bassin B04 (Cycle C02)

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 12/01/2026 | 0.5 | 8 | 44 700 | 300 | Mickael |
| 4 | 02/02/2026 | 2.8 | 25 | 44 100 | 600 | Mickael |
| 8 | 02/03/2026 | 8.5 | 55 | 43 500 | 600 | Mickael |
| 12 | 30/03/2026 | 14.5 | 85 | 42 900 | 600 | Mickael |
| 16 | 27/04/2026 | 20.0 | 120 | 42 200 | 700 | Mickael |

### 2.5 Série de pesées — Bassin B05 (Cycle C02) — **bassin du test EN_TRAITEMENT**

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 12/01/2026 | 0.5 | 8 | 59 700 | 300 | Mickael |
| 6 | 16/02/2026 | 5.5 | 40 | 58 800 | 900 | Mickael |
| 10 | 16/03/2026 | 11.5 | 70 | 58 000 | 800 | Mickael |

> Saisir le changement d'état EN_TRAITEMENT *entre* S6 et S10 (voir 1.3), puis reprendre la pesée normalement après retour en ACTIF — vérifie que le système accepte la pesée après réactivation.

### 2.6 Série de pesées — Bassin B06 (Cycle C02)

| Semaine | Date | Poids moyen (g) | Taille (mm) | Vivants | Morts | Technicien |
|---|---|---|---|---|---|---|
| 1 | 12/01/2026 | 0.5 | 8 | 59 800 | 200 | Mickael |
| 8 | 09/03/2026 | 8.5 | 55 | 58 600 | 1 200 | Mickael |
| 14 | 13/04/2026 | 17.5 | 105 | 57 800 | 800 | Mickael |
| 16 | 27/04/2026 | 20.0 | 120 | 57 500 | 300 | Mickael |

**Résultat attendu après saisie de toutes les pesées :**
- Biomasse calculée automatiquement (colonne générée) — ne jamais la saisir manuellement
- Taux de survie / mortalité recalculés via la vue `v_suivi_hebdo_bassin`
- Courbe de croissance affichée par bassin (réel vs standard `evolution_hebdo_espece`)
- Alerte automatique sur B03 (mortalité) à vérifier dans le module Alertes

---

## 3. MODULE 3 — NOURRISSAGE & DISTRIBUTION

### 3.1 Créer l'aliment et l'entrée de stock (multi-lots)

| Champ | Lot 101 | Lot 102 | Lot 103 |
|---|---|---|---|
| Aliment | Granulés Croissance Élevée | Granulés Croissance Élevée | Granulés Croissance Élevée |
| Quantité reçue (kg) | 500 | 800 | 600 |
| Prix unitaire (Ar/kg) | 2 200 | 2 100 | 2 300 |
| Date réception | 02/01/2026 | 01/02/2026 | 01/03/2026 |
| Date expiration | 01/09/2026 | 01/12/2026 | 15/03/2027 |

> Ces 3 lots permettent de tester le **FEFO** (premier expiré, premier sorti) déjà géré par ta procédure `pr_valider_nourrissage_direct`.

### 3.2 Générer le planning du jour

- Appeler la fonction de génération de planning (`fn_obtenir_ou_creer_planning_du_jour`) pour un jour donné, ex. **15/01/2026**, avec un technicien connecté (Njary ou Mickael).
- Vérifier que les 4 créneaux (MATIN, MIDI, SOIR, NUIT) sont générés pour chacun des 6 bassins actifs (B01, B02, B03, B04, B05, B06).

### 3.3 Valider des distributions — cas de test

| Bassin | Créneau | Heure de validation simulée | Résultat attendu |
|---|---|---|---|
| B01 | MATIN | 06:30 | Validé → statut NOURRI |
| B01 | MIDI | avant que MATIN soit validé | **Doit être refusé** (règle chronologie stricte) |
| B02 | MATIN | 05:50 | **Doit être refusé** (avant 06:00, hors plage horaire) |
| B04 | SOIR | 17:15 | Validé → statut NOURRI, doit piocher dans le lot 101 puis 102 si 101 épuisé (FEFO) |
| B06 | NUIT | 23:00 | Validé → statut NOURRI |

### 3.4 Test de distribution manuelle (correction)

| Bassin | Date | Heure visée | Aliment | Quantité (kg) | Résultat attendu |
|---|---|---|---|---|---|
| B05 | 16/01/2026 | 11:10 (proche de MIDI 11:00) | Granulés Croissance Élevée | 35 | Doit mettre à jour le créneau MIDI le plus proche, si pas déjà validé |

### 3.5 Vérification stock épuisé

- Distribuer volontairement une grosse quantité sur plusieurs jours jusqu'à épuiser le lot 101 (500 kg) → vérifier qu'à l'épuisement le système bascule automatiquement sur le lot 102 sans intervention manuelle.
- Forcer une situation où le **stock global de l'aliment est inférieur à la quantité prévue** → la validation doit être bloquée avec message d'erreur explicite (stock insuffisant).

### 3.6 Calcul FCR (sur cycle C01)

Après plusieurs semaines de distribution sur B01/B02/B03, consulter l'indicateur FCR par cycle :
- Vérifier la formule : FCR = total aliments distribués (kg) ÷ biomasse produite (kg)
- Objectif attendu entre 1,2 et 1,5

---

## 4. MODULE 4 — SANITAIRE

### 4.1 Créer le médicament et son stock

| Champ | Lot médicament |
|---|---|
| Médicament | Oxytétracycline |
| Unité | kg |
| Seuil minimum | 5 |
| Quantité reçue | 20 |
| Prix total (Ar) | 400 000 |
| Date réception | 05/01/2026 |
| Date expiration | 05/01/2027 |

### 4.2 Déclarer un incident sanitaire — bassin B03 (cohérent avec mortalité élevée du 2.3)

| Champ | Valeur |
|---|---|
| Bassin | B03 |
| Date détection | 06/02/2026 |
| Type incident | MORTALITE_ANORMALE |
| Description | "Mortalité supérieure à 4% en une semaine, eau trouble" |
| Niveau de gravité | CRITIQUE |
| Responsable | Njary |

> **Résultat attendu : mise en quarantaine automatique de B03** (trigger `fn_quarantaine_auto`). Vérifier dans le module Bassin que B03 passe bien en QUARANTAINE et que l'historique des statuts enregistre le changement avec le motif automatique.

### 4.3 Enregistrer le traitement lié

| Champ | Valeur |
|---|---|
| Incident parent | Incident B03 du 06/02/2026 |
| Médicament | Oxytétracycline |
| Dosage | "2g / 100L d'eau" |
| Durée | 7 jours |
| Date début | 06/02/2026 |
| Quantité utilisée | 3 kg |
| Responsable | Njary |

**Résultat attendu :** stock médicament décrémenté automatiquement (20 → 17 kg).

### 4.4 Test de blocage — stock médicament insuffisant

- Créer un second incident (sur un autre bassin, ex. B06) et tenter un traitement demandant **20 kg** de médicament alors qu'il n'en reste que 17 kg.
- **Résultat attendu : refus, message "stock médicament insuffisant"**

### 4.5 Résoudre l'incident et tester la levée de quarantaine

| Action | Résultat attendu |
|---|---|
| Marquer l'incident B03 comme RÉSOLU | Permet ensuite le changement d'état QUARANTAINE → PREPARATION |
| Tenter de changer B03 en QUARANTAINE → PREPARATION avant résolution | Doit être refusé |

---

## 5. RÉCOLTE & STOCK (crevettes)

### 5.1 Déclencher la récolte — Bassin B01 (poids 20g, taille 120mm à S16)

| Action | Détail |
|---|---|
| Changer l'état de B01 | ACTIF → RÉCOLTÉ |
| Condition validée | Dernière pesée : 20g / 120mm ≥ seuil 15g / 110mm |

**Résultat attendu (automatique) :**
- Création d'un lot crevette unique, ex. `LOT-B01-2026`
- `biomasse_totale_kg` = nb vivants (47 000) × poids moyen (20g) ÷ 1000 = **940 kg**
- Cycle clôturé pour ce bassin (`cycle_bassin_assoc.est_cloture = TRUE`)
- Entrée automatique visible dans le Dashboard stock (catégorie CREVETTE)

### 5.2 Récolter un second bassin — B04 (20g, 120mm à S16)

| Action | Détail |
|---|---|
| Changer l'état de B04 | ACTIF → RÉCOLTÉ |
| Biomasse attendue | 42 200 vivants × 20g ÷ 1000 = **844 kg** |

### 5.3 Test mouvement de stock crevette (perte manuelle)

| Champ | Valeur |
|---|---|
| Lot | LOT-B01-2026 |
| Type mouvement | PERTE |
| Quantité | 15 kg |
| Motif | "Casse pendant le transport interne" |

**Résultat attendu :** `biomasse_actuelle_kg` passe de 940 à 925 kg (trigger `fn_decrement_stock_crevette`).

### 5.4 Test de blocage stock négatif

- Tenter un mouvement PERTE de **2000 kg** sur LOT-B01-2026 (qui n'en contient que 925 restants).
- **Résultat attendu : refus**, exception "stock crevette insuffisant".

### 5.5 Inventaire

| Type produit | Stock théorique | Stock réel constaté | Écart attendu |
|---|---|---|---|
| CREVETTE (LOT-B01-2026) | 925 kg | 920 kg | -5 kg |
| ALIMENT (Granulés - lot 102) | (selon consommation réelle) | légèrement différent | calculé auto |

---

## 6. MODULE 10 — SÉCURITÉ (vérification transverse)

| Action | Résultat attendu |
|---|---|
| Se connecter avec l'utilisateur Admin créé par défaut | Accès complet à tous les modules |
| Consulter le journal des actions après les tests ci-dessus | Toutes les créations/modifications de bassins, pesées, incidents, distributions doivent apparaître, horodatées et liées à l'utilisateur exact |
| Tenter de supprimer un utilisateur ayant des actions au journal | Doit être refusé — seule la désactivation est permise |

---

## RÉCAPITULATIF — CE QUE CE PLAN COUVRE

| Module | Couverture |
|---|---|
| Bassins | 9 bassins créés, 2 cycles sur 3 démarrés, changements d'état (dont quarantaine auto et EN_TRAITEMENT) |
| Biologique | Pesées de 0g à 20g sur 6 bassins (2 cycles), calcul biomasse/survie/mortalité, alerte mortalité |
| Nourrissage | Multi-lots FEFO, règle chronologie stricte, règle horaire, blocage stock insuffisant, FCR |
| Sanitaire | Incident critique → quarantaine auto, traitement → décrément stock, blocage stock médicament insuffisant, résolution → déblocage |
| Stock crevettes | Récolte automatique (2 lots), mouvement perte, blocage stock négatif, inventaire avec écart |
| Sécurité | Traçabilité complète au journal, blocage suppression utilisateur actif |