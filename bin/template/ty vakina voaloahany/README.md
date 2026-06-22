# MOVE GASY — Template HTML/CSS
> Documentation à destination des développeurs et intégrateurs du projet.

---

## Structure du projet

```
template/
├── css/                        ← Feuilles de style par module
├── sous-pages/                 ← Sous-pages de chaque module
│   ├── <module>/               ← HTML des sous-pages
│   └── css/responsive.css      ← Responsive commun aux sous-pages
├── rendu_visuel/               ← Captures de référence (.png)
├── *.html                      ← Pages principale de chaque module
└── README.md
```

---

## Règle n°1 — Un CSS par module

Chaque module a son propre fichier CSS dans `/css/`. **Ne jamais mélanger les CSS entre modules.**

| Module | CSS | Pages principale |
|---|---|---|
| Dashboard | `dashboard.css` | `dashboard.html` |
| Bassins | `bassin.css` | `bassin.html` |
| Suivi biologique | `bio.css` | `bio.html` |
| Nourrissage | `nourrissage.css` | `nourrissage.html` |
| Sanitaire | `sanitaire.css` | `sanitaire.html` |
| Stock | `stock.css` | `stock.html` |
| Commercial | `commercial.css` | `commercial.html` |
| Livraisons | `livraisons.css` | `livraisons.css` |
| Finances | `finances.css` | `finances.html` |
| Front-office | `front-office.css` | `front-office.html` |
| Administration | `administration.css` | `administration.html` |
| Connexion | `login.css` | `login.html` |

---

## Règle n°2 — Structure HTML obligatoire

Toutes les pages (principales et sous-pages) doivent respecter **exactement** ce shell. Ne pas renommer les classes, ne pas en inventer de nouvelles.

```html
<body>
<div class="sidebar-overlay"></div>  <!-- obligatoire pour le menu mobile -->

<div class="layout">

  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="brand-logo"><i class="bi bi-water"></i></div>
      <div class="brand-name">MOVE GASY</div>
    </div>
    <div class="nav-container">
      <div class="nav-label">Principal</div>
      <a class="nav-item" href="#"><span class="nav-icon"><i class="bi bi-speedometer2"></i></span> Dashboard</a>
      <div class="nav-label">Terrain</div>
      <a class="nav-item active" href="#"><span class="nav-icon"><i class="bi bi-droplet-half"></i></span> Bassins</a>
      <!-- autres nav-items... -->
    </div>
  </aside>

  <div class="main-wrapper">

    <header class="topbar">
      <button class="menu-toggle" onclick="toggleSidebar()">
        <i class="bi bi-list"></i>
      </button>
      <div class="breadcrumb">
        <span>Module</span><span class="sep">/</span><span class="active">Sous-page</span>
      </div>
      <div class="topbar-actions">
        <button class="icon-btn"><i class="bi bi-bell"></i></button>
        <button class="icon-btn"><i class="bi bi-gear"></i></button>
        <div class="user-profile">
          <div class="avatar">AD</div>
        </div>
      </div>
    </header>

    <main class="content">
      <div class="header-section">
        <h2 class="header-title">Titre de la page</h2>
        <p class="header-sub">Sous-titre descriptif</p>
      </div>
      <!-- contenu ici -->
    </main>

  </div>
</div>

<script>
  function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('open');
    document.querySelector('.sidebar-overlay').classList.toggle('open');
  }
  document.querySelector('.sidebar-overlay')
    ?.addEventListener('click', () => {
      document.querySelector('.sidebar').classList.remove('open');
      document.querySelector('.sidebar-overlay').classList.remove('open');
    });
</script>
</body>
```

---

## Règle n°3 — Liens CSS selon la profondeur

Le chemin vers le CSS change selon que la page est une page principale ou une sous-page.

**Page principale** (ex: `bassin.html` à la racine) :
```html
<link href="css/bassin.css" rel="stylesheet">
<link href="css/responsive.css" rel="stylesheet">
```

**Sous-page** (ex: `sous-pages/bassin/liste_bassin.html`) :
```html
<link href="../../css/bassin.css" rel="stylesheet">
<link href="../css/responsive.css" rel="stylesheet">
```

---

## Règle n°4 — Variables CSS (ne jamais en créer de nouvelles)

Toutes les couleurs et dimensions viennent de ces variables. **Ne jamais écrire une valeur hexadécimale ou une taille en dur dans le HTML.**

```css
--mg-primary: #1a1a1a        /* Noir principal — texte, boutons, nav active */
--mg-secondary: #4a4a4a      /* Gris — textes secondaires, labels */
--mg-surface: #f7f9fb        /* Fond de page */
--mg-surface-bright: #ffffff /* Fond des cards et sidebar */
--mg-surface-container: #eceef0  /* Hover, backgrounds neutres */
--mg-outline: #d1d5db        /* Bordures */
--mg-success: #10b981        /* Vert */
--mg-warning: #f59e0b        /* Orange */
--mg-danger: #ef4444         /* Rouge */
--radius: 4px                /* Arrondi des coins */
--sidebar-w: 240px           /* Largeur sidebar */
--topbar-h: 64px             /* Hauteur topbar */
```

---

## Règle n°5 — Classes de composants disponibles

Ne pas inventer de nouvelles classes. Utiliser uniquement ce qui existe.

**Layout**
```html
<div class="grid-detail">          <!-- grille 12 colonnes -->
  <div class="col-4">...</div>     <!-- span 4 colonnes -->
  <div class="col-6">...</div>     <!-- span 6 colonnes -->
  <div class="col-8">...</div>     <!-- span 8 colonnes -->
  <div class="col-12">...</div>    <!-- pleine largeur -->
</div>
```

**Cards**
```html
<div class="card">
  <div class="card-header">
    <h3 class="card-title">TITRE</h3>
    <button class="btn btn-sm btn-outline">Action</button>
  </div>
  <!-- contenu -->
</div>
```

**KPI**
```html
<div class="kpi-row">
  <div class="kpi-card">
    <div class="kpi-label">LABEL</div>
    <div class="kpi-value">42</div>
    <div class="kpi-sub">texte secondaire</div>
  </div>
</div>
```

**Boutons** — la classe `.btn` est toujours obligatoire en premier
```html
<button class="btn btn-primary">Enregistrer</button>
<button class="btn btn-outline">Annuler</button>
<button class="btn btn-primary btn-sm">Petit</button>
<button class="btn btn-primary btn-full">Pleine largeur</button>
```

**Badges**
```html
<span class="badge badge-success">Actif</span>
<span class="badge badge-warning">En attente</span>
<span class="badge badge-danger">Erreur</span>
<span class="badge badge-neutral">Inactif</span>
```

**Tableaux**
```html
<div class="table-container">
  <table>
    <thead><tr><th>Colonne</th></tr></thead>
    <tbody><tr><td>Valeur</td></tr></tbody>
  </table>
</div>
```

**Formulaires**
```html
<div class="form-group">
  <label class="form-label">Champ</label>
  <input class="form-control" type="text">
</div>
```

---

## Règle n°6 — Styles spécifiques à une page

Si une page nécessite des styles qui n'existent pas dans le CSS du module (ex: une grille particulière, une couleur d'accent), les ajouter dans un bloc `<style>` dans le `<head>` de cette page uniquement, en utilisant exclusivement les variables `--mg-*`.

```html
<head>
  <link href="../../css/sanitaire.css" rel="stylesheet">
  <style>
    /* Styles propres à cette page uniquement */
    .ma-grille-speciale {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 8px;
      border: 1px solid var(--mg-outline); /*  variable */
      background: #ff0000;                 /*  valeur en dur interdite */
    }
  </style>
</head>
```

---

## Règle n°7 — nav-item actif

Sur chaque page, **un seul** `nav-item` doit porter la classe `active` — celui qui correspond au module en cours.

```html
<!-- Sur une sous-page du module Sanitaire -->
<a class="nav-item active" href="#">
  <span class="nav-icon"><i class="bi bi-plus-circle"></i></span> Sanitaire
</a>
```

---

## Icônes

Le projet utilise **Bootstrap Icons**. Lien CDN déjà inclus dans toutes les pages :
```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
```
Référence : [icons.getbootstrap.com](https://icons.getbootstrap.com)

---

## Captures de référence

Le dossier `/rendu_visuel/` contient une capture `.png` pour chaque page. En cas de doute sur le rendu attendu, s'y référer avant de modifier quoi que ce soit.

---

## Ce qu'il ne faut jamais faire

|  Interdit |  À faire |
|---|---|
| Créer une nouvelle variable CSS `--ma-couleur` | Utiliser une variable `--mg-*` existante |
| Écrire `color: #10b981` en dur | Écrire `color: var(--mg-success)` |
| Utiliser `position: sticky` sur `.topbar` | Utiliser `position: fixed` |
| Ajouter `<style>` dans le `<body>` | Mettre les styles dans `<head>` |
| Oublier la classe `.btn` sur un bouton | `class="btn btn-primary"` |
| Modifier `nourrissage.css` pour une autre page | Créer un `<style>` local dans la page |
| Copier-coller le CSS d'un module dans un autre | Lier le bon fichier CSS du module |