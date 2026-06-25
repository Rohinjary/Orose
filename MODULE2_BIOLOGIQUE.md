# Module 2 — Gestion Biologique (BAOVOLA / Orose)

Documentation des fichiers créés et modifiés pour l'implémentation du module biologique (F2.1 à F2.5).

---

## Résumé des fonctionnalités

| Code | Fonctionnalité | URL | Statut |
|------|----------------|-----|--------|
| F2.1 | Liste des bassins en suivi | `GET /biologique` | ✅ |
| F2.2 | CRUD Pesée (création, modification, archivage*) | `/biologique/{id}/pesee/nouvelle`, `/biologique/pesee/{id}/modifier`, `/biologique/pesee/{id}/archiver` | ✅ (*archivage documenté comme non disponible) |
| F2.3 | Dashboard détail suivi biologique | `GET /biologique/{id}/detail` | ✅ |
| F2.4 | Alertes biologiques (consultation + résolution) | `GET /biologique/alertes`, `POST /biologique/alertes/{id}/resoudre` | ✅ |
| F2.5 | Calcul automatique des alertes (Scheduler 08h00) | `@Scheduled(cron = "0 0 8 * * *")` | ✅ |

---

## Fichiers CRÉÉS

### DTOs (`src/main/java/com/example/orose/dto/`)

| Fichier | Description |
|---------|-------------|
| `BassinSuiviDTO.java` | DTO pour la liste F2.1 — bassin en suivi actif avec KPIs |
| `PeseeDTO.java` | DTO formulaire de saisie/modification de pesée F2.2 |
| `CourbeCroissanceDTO.java` | Point de courbe (semaine, poids, taille) pour graphique Chart.js |
| `SuiviBiologiqueDetailDTO.java` | DTO agrégé dashboard F2.3 (KPIs, courbes, pesées, alertes) |

### Repositories (`src/main/java/com/example/orose/repository/`)

| Fichier | Méthodes principales |
|---------|---------------------|
| `SuiviHebdoBassinRepository.java` | `findByCycleBassinAssocIdOrderByDateSuiviAsc`, `findTopByCycleBassinAssocIdOrderByDateSuiviDesc`, `findTop2ByCycleBassinAssocIdOrderByDateSuiviDesc` |
| `AlerteRepository.java` | `findByModuleSourceAndEstResolueFalseOrderByDateCreationDesc`, `findByCycleBassinAssocIdAndEstResolueFalse`, `existsByCycleBassinAssocIdAndTypeAlerteAndEstResolueFalse` |
| `EvolutionHebdoEspeceRepository.java` | `findByEspeceIdOrderBySemaineAsc`, `findByEspeceIdAndSemaine` |

### Services (`src/main/java/com/example/orose/service/`)

| Fichier | Responsabilité |
|---------|----------------|
| `AlerteService.java` | Création anti-doublon, résolution, liste alertes BIOLOGIQUE, alerte récolte |
| `PeseeService.java` | Enregistrement, modification, consultation pesées ; validation métier R3/R4/R8 |
| `BiologiqueService.java` | Liste bassins en suivi F2.1, détail biologique F2.3 avec calcul KPIs et courbes |
| `AlerteBiologiqueScheduler.java` | Vérification quotidienne : pesée manquante, mortalité, survie critique, récolte |

### Contrôleur (`src/main/java/com/example/orose/controller/`)

| Fichier | Mappings |
|---------|----------|
| `BiologiqueController.java` | Tous les endpoints `/biologique/**` (liste, détail, pesée CRUD, alertes) |

### Templates Thymeleaf (`src/main/resources/templates/biologique/`)

| Fichier | URL | Description |
|---------|-----|-------------|
| `liste.html` | `/biologique` | Tableau bassins actifs avec statut croissance coloré |
| `pesee-form.html` | `/biologique/{id}/pesee/nouvelle` et `/biologique/pesee/{id}/modifier` | Formulaire pesée avec indicateur seuil récolte JS |
| `detail.html` | `/biologique/{id}/detail` | Dashboard KPIs + Chart.js + tableau pesées |
| `alertes.html` | `/biologique/alertes` | Cards alertes par niveau (ROUGE/ORANGE/VERT) |

### CSS (`src/main/resources/static/css/biologique/`)

| Fichier | Styles |
|---------|--------|
| `liste.css` | Lignes colorées CRITIQUE/RETARD, badge pulse, taux survie coloré |
| `detail.css` | KPI cards, conteneur graphique 300px, tableau pesées hover |
| `alertes.css` | Cards avec ombre colorée, animation fadeIn |

---

## Fichiers MODIFIÉS

| Fichier | Modification |
|---------|--------------|
| `OroseApplication.java` | Ajout `@EnableScheduling` pour activer le scheduler F2.5 |
| `templates/bassin/detail.html` | Lien navigation « Suivi biologique » → `/biologique` |
| `templates/bassin/form.html` | Lien navigation « Suivi biologique » → `/biologique` |
| `templates/bassin/historique.html` | Lien navigation « Suivi biologique » → `/biologique` |
| `templates/bassin/liste.html` | Lien « Suivi biologique » → `/biologique` |
| `templates/cycle/form.html` | Lien navigation « Suivi biologique » → `/biologique` |

---

## Règles métier implémentées

| # | Règle | Implémentation |
|---|-------|----------------|
| R1 | `biomasseCalculeeKg` colonne générée PostgreSQL | Jamais settée depuis Java (entité existante `insertable=false, updatable=false`) |
| R2 | Suppression physique interdite | `archiverPesee()` lève `UnsupportedOperationException` — colonne `est_archive` absente en DB |
| R3 | Pesée uniquement sur cycle non clôturé + bassin ACTIF | Validé dans `PeseeService.enregistrerPesee()` |
| R4 | `nbVivants` ≤ `effectifInitial` | Validé dans `PeseeService` |
| R5 | Pas de doublon d'alertes | `AlerteService.creerAlerteSiAbsente()` via `existsBy...` |
| R6 | Scheduler à 08h00 | `@Scheduled(cron = "0 0 8 * * *")` |
| R7 | Mortalité anormale à partir semaine 8 | `AlerteBiologiqueScheduler.verifierMortaliteAnormale()` |
| R8 | Seuil récolte : poids ≥ 15g ET taille ≥ 110mm | PeseeService + Scheduler + indicateur JS formulaire |
| R9 | Survie critique < 40% | BiologiqueService + Scheduler |
| R10 | Pesée manquante > 7 jours | Scheduler |

---

## Points d'attention / limitations

### Archivage des pesées (R2)
La colonne `est_archive` **n'existe pas** dans la table `suivi_hebdo_bassin` (vérifié dans `db_modif.sql` et l'entité `SuiviHebdoBassin`). La méthode `PeseeService.archiverPesee()` :
- Vérifie que la pesée existe
- Lève une `UnsupportedOperationException` avec message explicite
- Le bouton « Archiver » est présent dans l'UI mais affichera un message d'erreur flash

**Pour activer l'archivage**, ajouter en base :
```sql
ALTER TABLE suivi_hebdo_bassin ADD COLUMN est_archive BOOLEAN NOT NULL DEFAULT FALSE;
```
Puis ajouter le champ dans l'entité `SuiviHebdoBassin` et implémenter le soft delete.

### Utilisateur connecté
`BiologiqueController.resoudreAlerte()` utilise `idUtilisateur = 1L` en dur (commentaire `// TODO: session`), conforme au pattern existant dans `BassinController`.

### IDs entités vs repositories
Les entités utilisent `Integer` pour les `@Id`, les repositories existants utilisent `Long` — pattern conservé du projet existant.

---

## Navigation

```
/biologique                            → Liste bassins en suivi (F2.1)
/biologique/{idCBA}/detail             → Dashboard détail (F2.3)
/biologique/{idCBA}/pesee/nouvelle     → Nouvelle pesée (F2.2)
/biologique/pesee/{id}/modifier        → Modifier pesée (F2.2)
/biologique/pesee/{id}/archiver        → POST archivage (F2.2 — non actif)
/biologique/alertes                    → Liste alertes (F2.4)
/biologique/alertes/{id}/resoudre      → POST résolution alerte (F2.4)
```

---

## Types d'alertes générées

| Type | Niveau | Déclencheur |
|------|--------|-------------|
| `PESEE_MANQUANTE` | ORANGE | Aucune pesée depuis > 7 jours |
| `MORTALITE_ANORMALE` | ORANGE (>1%) / ROUGE (>2%) | Semaine ≥ 8, mortalité hebdo calculée |
| `SURVIE_CRITIQUE` | ROUGE | Taux survie < 40% |
| `RECOLTE_POSSIBLE` | VERT | Poids ≥ 15g ET taille ≥ 110mm |

---

## Compilation

Le projet compile avec succès :
```bash
mvn compile -DskipTests
```

---

## Structure arborescence ajoutée

```
src/main/java/com/example/orose/
├── controller/
│   └── BiologiqueController.java          [CRÉÉ]
├── dto/
│   ├── BassinSuiviDTO.java                [CRÉÉ]
│   ├── CourbeCroissanceDTO.java           [CRÉÉ]
│   ├── PeseeDTO.java                      [CRÉÉ]
│   └── SuiviBiologiqueDetailDTO.java      [CRÉÉ]
├── repository/
│   ├── AlerteRepository.java              [CRÉÉ]
│   ├── EvolutionHebdoEspeceRepository.java [CRÉÉ]
│   └── SuiviHebdoBassinRepository.java      [CRÉÉ]
└── service/
    ├── AlerteBiologiqueScheduler.java     [CRÉÉ]
    ├── AlerteService.java                 [CRÉÉ]
    ├── BiologiqueService.java             [CRÉÉ]
    └── PeseeService.java                  [CRÉÉ]

src/main/resources/
├── static/css/biologique/
│   ├── alertes.css                        [CRÉÉ]
│   ├── detail.css                         [CRÉÉ]
│   └── liste.css                          [CRÉÉ]
└── templates/biologique/
    ├── alertes.html                       [CRÉÉ]
    ├── detail.html                        [CRÉÉ]
    ├── liste.html                         [CRÉÉ]
    └── pesee-form.html                    [CRÉÉ]
```

---

*Généré le 21/06/2026 — Module 2 Gestion Biologique Orose/BAOVOLA*
