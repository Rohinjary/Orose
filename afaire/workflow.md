# WORKFLOW.md — Git Flow Simplifié (DEV uniquement)

## 🎯 Objectif

Ce workflow est conçu pour une équipe de 4 développeurs travaillant sur le projet BAOVOLA. Il permet de :

- ✅ Travailler en parallèle sans se bloquer
- ✅ Éviter au maximum les conflits Git
- ✅ Avoir une branche `develop` toujours stable
- ✅ Livrer rapidement les fonctionnalités

---

## 📂 Structure des branches

main
└── develop (branche principale de développement)
├── feature/m1-m2 ← Dev 1 (Bassins + Biologique)
├── feature/m3-m5 ← Dev 2 (Nourrissage + Stock)
├── feature/m4-alertes ← Dev 3 (Sanitaire + Alertes)
└── feature/m10-setup ← Lead (Sécurité + Setup)


---

## 🔄 Règles de base

| Règle | Explication |
|-------|-------------|
| **Ne jamais push sur main** | `main` est protégée, réservée à la production |
| **Ne jamais push sur develop directement** | Toujours passer par une PR |
| **Une branche = une fonctionnalité** | Pas de mélange de modules |
| **Commits fréquents** | Au moins 1 commit par heure |
| **PR = code testé et fonctionnel** | Pas de code cassé |

---

## 📝 Convention de nommage des branches

```bash
# Format
feature/nom-module

# Exemples
feature/m1-m2        # Dev 1
feature/m3-m5        # Dev 2  
feature/m4-alertes   # Dev 3
feature/m10-setup    # Lead


# 1. Récupérer les dernières modifications
git checkout develop
git pull origin develop

# 2. Créer ou mettre à jour sa branche
git checkout -b feature/m1-m2  # Si première fois

# OU
git checkout feature/m1-m2
git rebase develop

# Sauvegarder régulièrement (toutes les 1-2 heures)
git add .
git commit -m "feat(bassin): ajout CRUD Bassin"
git push origin feature/m1-m2

# 1. S'assurer que develop est à jour
git checkout develop
git pull origin develop

# 2. Rebaser sa branche sur develop
git checkout feature/m1-m2
git rebase develop

# 3. Résoudre les conflits si présents
# (modifier les fichiers puis)
git add .
git rebase --continue

# 4. Pousser la branche
git push origin feature/m1-m2 --force-with-lease

# 5. Créer la PR sur GitHub
# Aller sur GitHub → New Pull Request
# feature/m1-m2 → develop




#Convention de commits

# Format
type(scope): description

# Types possibles
feat     # Nouvelle fonctionnalité
fix      # Correction de bug
docs     # Documentation
style    # Formatage, espacement
refactor # Refactorisation
test     # Ajout de tests
chore    # Tâches de maintenance

# Exemples
feat(bassin): ajouter CRUD Bassin
fix(pesee): corriger calcul biomasse
test(cycle): ajouter tests unitaires
docs(readme): mettre à jour installation





Si des conflits apparaissent lors du rebase :
# 1. Voir les fichiers en conflit
git status

# 2. Ouvrir les fichiers et résoudre manuellement
# (chercher les marqueurs <<<<<<<, =======, >>>>>>>)

# 3. Ajouter les fichiers résolus
git add .

# 4. Continuer le rebase
git rebase --continue

# 5. Si le rebase est bloqué, annuler
git rebase --abort