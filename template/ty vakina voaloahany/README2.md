# MOVE GASY — Architecture du Template

---

## Concept général

Le projet est organisé en **deux niveaux de pages** qui travaillent ensemble.

```
Page principale  →  le cadre (sidebar, topbar, navigation)
Sous-pages       →  le contenu qui s'affiche à l'intérieur de ce cadre
```

Concrètement, quand un utilisateur navigue dans l'application, il voit toujours la même sidebar et le même topbar — seul le contenu central change. C'est exactement ce que reflète la structure des fichiers.

---

## Les pages principales — le template

Les fichiers à la racine (`bassin.html`, `sanitaire.html`, `finances.html`…) sont les **templates de chaque module**. Leur rôle est de définir :

- La sidebar complète avec tous les liens de navigation
- Le topbar avec le breadcrumb et les actions
- L'onglet actif dans la sidebar (`class="nav-item active"`)
- Le lien vers le CSS du module

Ils représentent visuellement **le dashboard ou la vue d'accueil** de chaque module. C'est la page que l'utilisateur voit en premier quand il clique sur un module dans la sidebar.

```
dashboard.html      → accueil général de l'application
bassin.html         → accueil du module Bassins
bio.html            → accueil du module Suivi biologique
nourrissage.html    → accueil du module Nourrissage
sanitaire.html      → accueil du module Sanitaire
stock.html          → accueil du module Stock
commercial.html     → accueil du module Commercial
livraisons.html     → accueil du module Livraisons
finances.html       → accueil du module Finances
front-office.html   → accueil du module Front-office
administration.html → accueil du module Administration
login.html          → page de connexion (cas à part, pas de sidebar)
```

---

## Les sous-pages — les sections du template

Les fichiers dans `/sous-pages/<module>/` sont les **vues détaillées** de chaque module. Chaque sous-page correspond à une action ou une vue spécifique : une liste, un formulaire, un historique, un détail…

Elles héritent visuellement de leur page principale : même sidebar, même topbar, même CSS. **Seul le contenu dans `<main class="content">` est différent.**

```
sous-pages/
├── bassin/
│   ├── liste_bassin.html     → liste de tous les bassins
│   ├── detail_bassin.html    → fiche détaillée d'un bassin
│   ├── crud_bassin.html      → formulaire création / édition
│   ├── histo_bassin.html     → historique des cycles
│   └── cycle_bassin.html     → suivi du cycle en cours
│
├── sanitaire/
│   ├── dash_sani.html        → dashboard sanitaire
│   ├── liste_incident_sani.html
│   ├── crud_incident_sani.html
│   ├── traitement_sani.html
│   └── histo_sani.html
│
└── ...
```

---

## Schéma de navigation

```
Utilisateur clique sur "Sanitaire" dans la sidebar
        ↓
sanitaire.html          ← page principale = dashboard du module

Utilisateur clique sur "Voir tous les incidents"
        ↓
sous-pages/sanitaire/liste_incident_sani.html   ← sous-page = liste

Utilisateur clique sur "Créer un incident"
        ↓
sous-pages/sanitaire/crud_incident_sani.html    ← sous-page = formulaire
```

---

## Ce qui est identique entre une page principale et ses sous-pages

| Élément | Identique ? |
|---|---|
| Sidebar (structure, liens, icônes) | ✅ Copie conforme |
| Topbar (structure, avatar, icônes) | ✅ Copie conforme |
| Fichier CSS lié | ✅ Même module CSS |
| Script `toggleSidebar()` | ✅ Identique |
| `<div class="sidebar-overlay">` | ✅ Présent partout |
| `nav-item active` | ⚠️ Même module, mais vérifie le bon item |
| Breadcrumb | ⚠️ Adapté à la sous-page |
| `header-title` et `header-sub` | ⚠️ Adapté à la sous-page |
| Contenu dans `<main class="content">` | ❌ Différent à chaque page |

---

## Comment créer une nouvelle sous-page

**1.** Copier le fichier de la page principale du module concerné.

**2.** Changer le chemin du CSS (on descend d'un niveau) :
```html
<!-- Page principale (racine) -->
<link href="css/bassin.css" rel="stylesheet">

<!-- Sous-page (dans sous-pages/bassin/) -->
<link href="../../css/bassin.css" rel="stylesheet">
<link href="../css/responsive.css" rel="stylesheet">
```

**3.** Adapter uniquement ces 3 éléments du shell :
```html
<!-- Breadcrumb -->
<div class="breadcrumb">
  <span>Bassins</span>
  <span class="sep">/</span>
  <span class="active">Liste des bassins</span>  ← changer ici
</div>

<!-- Titre de la page -->
<h2 class="header-title">Liste des bassins</h2>   ← changer ici
<p class="header-sub">...</p>                      ← changer ici
```

**4.** Remplacer uniquement le contenu de `<main class="content">`. Tout le reste (sidebar, topbar, scripts) reste intact.

---

## Erreur fréquente à éviter

> "J'ai modifié la sidebar dans une sous-page pour ajouter un lien."

Mauvaise approche — si la sidebar change, il faut la mettre à jour **dans toutes les pages et sous-pages du module** à la main.

Bonne pratique — toute modification de la sidebar se fait d'abord dans la page principale, puis répercutée sur toutes ses sous-pages. C'est volontaire : le projet n'utilise pas de système d'inclusion (pas de PHP, pas de composants JS) pour rester en HTML pur.

# LIBRE À VOUS D'AMÉLIORER LE TEMPLATE OU PAS POUR LA NAVIGATION FA MODÈLE AN'ILAY VUE ANTSIKA FOTSINY ITO 