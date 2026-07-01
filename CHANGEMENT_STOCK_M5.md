# Module Stock

## Nouveaux fichiers

### `controller/stock/StockController.java`
| Méthode | Route |
|---------|-------|
| `dashboard(Model)` | GET `/stock/dashboard` |
| `listeProduits(Model)` | GET `/stock/produits` |
| `formulaireEntree(Model)` | GET `/stock/entree` |
| `enregistrerEntree(EntreeStockIntrantDTO, RedirectAttributes)` | POST `/stock/entree` |
| `formulaireSortie(Model)` | GET `/stock/sortie` |
| `enregistrerSortie(SortieStockIntrantDTO, RedirectAttributes)` | POST `/stock/sortie` |
| `historiqueMouvements(Model)` | GET `/stock/mouvements` |
| `lotsCrevettes(Model)` | GET `/stock/lots-crevettes` |
| `preparerLayout(Model, String, String)` | Helper layout |

### `service/stock/StockService.java`
| Méthode | Description |
|---------|-------------|
| `getDashboard()` | KPIs + alertes + 5 derniers mouvements |
| `estimerAutonomieAliment()` | Jours restants via conso 7j |
| `getAlertes()` | Alertes rupture/faible/périmé |
| `getListeProduits()` | Produits avec statut |
| `enregistrerEntreeIntrant(EntreeStockIntrantDTO)` | Entrée médicament |
| `enregistrerSortieManuelle(SortieStockIntrantDTO)` | Sortie FIFO perte/destruction |
| `getHistoriqueMouvements()` | Journal tous mouvements |
| `getDerniersMouvements(int)` | N derniers mouvements |
| `getStockAlimentTotal()` | Total stock alimentaire |
| `getLotsCrevette()` | Liste lots crevette |
| `getAliments()` | Liste tous aliments |
| `getMedicaments()` | Liste tous médicaments |
| `remplirStockCrevette(StockDashboardDTO)` | Biomasse + valorisation |
| `remplirStockAliment(StockDashboardDTO)` | Stock aliment + autonomie |
| `remplirStockMedicament(StockDashboardDTO)` | Somme quantités restantes |
| `compterProduitsFaibles()` | Produits sous seuil |
| `compterLotsPerimes()` | Lots expirés avec stock > 0 |
| `buildAlertesRuptureFaible()` | Alertes rupture/faible |
| `buildAlertesPeremption()` | Alertes expiration |
| `ajouterAlerteCrevette(List<StockAlerteDTO>)` | Alerte biomasse nulle |
| `buildProduitsAliment()` | DTO pour chaque aliment |
| `buildProduitsMedicament()` | DTO pour chaque médicament |
| `validerMotifSortie(String)` | Valide motif PERTE/DESTRUCTION |
| `lotsDisponiblesTriesFIFO()` | Lots triés par expiration |
| `retirerStockFIFO(...)` | Retrait FIFO + mouvements |
| `buildSortiesAliment()` | Mouvements sortie aliment |
| `buildSortiesMedicament()` | Mouvements sortie médicament |
| `buildSortiesCrevette()` | Mouvements sortie crevette |
| `buildEntreesAliment()` | Entrées stock aliment |
| `buildEntreesMedicament()` | Entrées stock médicament |
| `buildEntreesCrevette()` | Entrées lots crevette |
| `calcStockMedicament(Medicament)` | Somme quantiteRestante par médicament |
| `seuilMedicament(Medicament)` | Shortcut seuil minimum |
| `toFloat(BigDecimal)` | Conversion null-safe |

### `dto/stock/`
| DTO | Description |
|-----|-------------|
| `StockDashboardDTO` | KPIs dashboard |
| `StockAlerteDTO` | Alerte stock |
| `ProduitStockDTO` | Produit avec statut |
| `EntreeStockIntrantDTO` | Entrée médicament |
| `SortieStockIntrantDTO` | Sortie perte/destruction |
| `MouvementStockDTO` | Mouvement historique |

### `repository/stock/`
| Interface | Méthodes |
|-----------|----------|
| `LotCrevetteRepository` | `sumBiomasseActuelle()`, `sumBiomasseDisponible()`, `findAllByOrderByDateRecolteDesc()` |
| `MouvementStockAlimentRepository` | `findAllByOrderByDateMouvementDesc()` |
| `MouvementStockCrevetteRepository` | `findAllByOrderByDateMouvementDesc()`, `findByLotCrevetteIdOrderByDateMouvementDesc(Integer)` |

### Templates
| Fichier | Description |
|---------|-------------|
| `stock/dashboard.html` | KPIs + alertes + derniers mouvements |
| `stock/index.html` | Tableau produits avec statut |
| `stock/entree.html` | Formulaire entrée médicament |
| `stock/sortie.html` | Formulaire sortie perte/destruction |
| `stock/historique.html` | Journal tous mouvements |
| `stock/lots_crevettes.html` | Registre récoltes auto |

## Fichiers modifiés

| Fichier | Changement |
|---------|------------|
 | `fragments/sidebar.html` | Menu Stocks restructuré par catégorie (Crevette/Aliment/Médicament) avec sous-labels |
| `fragments/header.html` | Sous-menu `max-height: 400px` → `600px`. Ajout style `.nav-sub-label` |
| `service/BassinService.java` | `creerBassin()` : sauvegarde bassin avant histo. `changerStatutBassin()` : auto-création LotCrevette + clôture CycleBassinAssoc si RECOLTE. Fallback vers dernière pesée pour `poidsMoyenFinalG`/`tailleMoyenneFinaleMm` (évite biomasse=0). Injection `SuiviHebdoBassinRepository` |
| `service/TraitementService.java` | `enregistrerTraitement()` : décrémente `quantiteRestante` + crée `MouvementStockMedicament` type TRAITEMENT |
| `service/nourrissage/NourrissageService.java` | `valider()` : `@Transactional` + FIFO `EntreeStockAliment` + crée `MouvementStockAliment` type NOURRISSAGE |
