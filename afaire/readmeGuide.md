# 📘 Guide de Développement - Projet Orose

Ce guide explique comment organiser le code selon l'architecture MVC avec Spring Boot.

---

## 📁 Structure du Projet

```
Orose/
├── src/
│   ├── main/
│   │   ├── java/com/example/orose/
│   │   │   ├── controller/      # Contrôleurs MVC
│   │   │   ├── service/         # Services métier
│   │   │   ├── repository/      # Accès aux données
│   │   │   ├── model/           # Entités JPA
│   │   │   ├── dto/             # Objets de transfert
│   │   │   └── config/          # Configuration
│   │   └── resources/
│   │       ├── templates/       # Templates Thymeleaf
│   │       ├── static/          # CSS, JS, images
│   │       └── application.properties  # ⚠️ À configurer localement
│   └── test/                    # Tests unitaires
├── template/                    # Templates HTML de design (référence)
│   ├── administration.html
│   ├── bassin.html
│   ├── sous-pages/              # Sous-pages par module
│   └── css/                     # Feuilles de style
├── pom.xml                      # Configuration Maven
└── mvnw                         # Maven Wrapper
```

---

## 1️⃣ Créer un Model (Entité)

**Chemin :** `src/main/java/com/example/orose/model/`

**Package :** `com.example.orose.model`

**Convention de nommage :** `NomEntite.java` (ex: `Utilisateur.java`, `Bassin.java`)

**Exemple :**
```java
package com.example.orose.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nom_table")
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class NomEntite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom;
    
    private String description;
    
    private LocalDateTime dateCreation;
    
    // Autres champs selon besoins
}
```

**Annotations importantes :**
- `@Entity` : Déclare la classe comme entité JPA
- `@Table(name = "nom_table")` : Nom de la table en base de données
- `@Id` : Clé primaire
- `@GeneratedValue` : Génération automatique de l'ID
- `@Data` (Lombok) : Génère getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` : Constructeur sans arguments
- `@AllArgsConstructor` : Constructeur avec tous les arguments

---

## 2️⃣ Créer un Repository

**Chemin :** `src/main/java/com/example/orose/repository/`

**Package :** `com.example.orose.repository`

**Convention de nommage :** `NomEntiteRepository.java`

**Exemple :**
```java
package com.example.orose.repository;

import com.example.orose.model.NomEntite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NomEntiteRepository extends JpaRepository<NomEntite, Long> {
    
    // Méthodes personnalisées (optionnel)
    Optional<NomEntite> findByNom(String nom);
    
    List<NomEntite> findByActiveTrue();
    
    @Query("SELECT n FROM NomEntite n WHERE n.nom LIKE %?1%")
    List<NomEntite> searchByNom(String keyword);
}
```

**Méthodes héritées de JpaRepository :**
- `save(entity)` : Sauvegarder
- `findById(id)` : Trouver par ID
- `findAll()` : Tout récupérer
- `deleteById(id)` : Supprimer par ID
- `count()` : Compter le nombre d'entités

---

## 3️⃣ Créer un Service

**Chemin :** `src/main/java/com/example/orose/service/`

**Package :** `com.example.orose.service`

**Convention de nommage :** `NomEntiteService.java`

**Exemple :**
```java
package com.example.orose.service;

import com.example.orose.model.NomEntite;
import com.example.orose.repository.NomEntiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NomEntiteService {
    
    @Autowired
    private NomEntiteRepository repository;
    
    public List<NomEntite> findAll() {
        return repository.findAll();
    }
    
    public Optional<NomEntite> findById(Long id) {
        return repository.findById(id);
    }
    
    public NomEntite save(NomEntite entity) {
        return repository.save(entity);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<NomEntite> search(String keyword) {
        return repository.searchByNom(keyword);
    }
}
```

---

## 4️⃣ Créer un DTO (Data Transfer Object)

**Chemin :** `src/main/java/com/example/orose/dto/`

**Package :** `com.example.orose.dto`

**Convention de nommage :** `NomEntiteDTO.java`

**Utilité :** Transférer les données sans exposer directement l'entité JPA

**Exemple :**
```java
package com.example.orose.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class NomEntiteDTO {
    
    private Long id;
    private String nom;
    private String description;
    private LocalDateTime dateCreation;
    
    // Constructeur depuis l'entité
    public NomEntiteDTO(com.example.orose.model.NomEntite entity) {
        this.id = entity.getId();
        this.nom = entity.getNom();
        this.description = entity.getDescription();
        this.dateCreation = entity.getDateCreation();
    }
}
```

---

## 5️⃣ Créer un Controller

**Chemin :** `src/main/java/com/example/orose/controller/`

**Package :** `com.example.orose.controller`

**Convention de nommage :** `NomEntiteController.java`

**Exemple :**
```java
package com.example.orose.controller;

import com.example.orose.model.NomEntite;
import com.example.orose.service.NomEntiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/nom-entite")
public class NomEntiteController {
    
    @Autowired
    private NomEntiteService service;
    
    // Liste tous les éléments
    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.findAll());
        return "nom-entite-list";  // Retourne le template
    }
    
    // Afficher le formulaire de création
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("item", new NomEntite());
        return "nom-entite-form";
    }
    
    // Enregistrer un nouvel élément
    @PostMapping("/save")
    public String save(@ModelAttribute NomEntite entity) {
        service.save(entity);
        return "redirect:/nom-entite";
    }
    
    // Afficher les détails
    @GetMapping("/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        service.findById(id).ifPresent(item -> 
            model.addAttribute("item", item)
        );
        return "nom-entite-details";
    }
    
    // Afficher le formulaire d'édition
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        service.findById(id).ifPresent(item -> 
            model.addAttribute("item", item)
        );
        return "nom-entite-form";
    }
    
    // Supprimer un élément
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/nom-entite";
    }
}
```

---

## 6️⃣ Créer une View (Template)

### 📍 Emplacement des Templates

**Templates HTML de référence (design) :**
- **Chemin :** `template/` à la racine du projet
- **Fichiers principaux :**
  - `template/administration.html`
  - `template/bassin.html`
  - `template/dashboard.html`
  - `template/login.html`
  - `template/front-office.html`
  - etc.

**Sous-pages par module :**
- **Chemin :** `template/sous-pages/`
- **Exemples :**
  - `template/sous-pages/administration/crud_user.html`
  - `template/sous-pages/administration/list_user.html`
  - `template/sous-pages/bassin/liste_bassin.html`
  - `template/sous-pages/bassin/crud_bassin.html`
  - `template/sous-pages/commercial/liste_client.html`
  - etc.

### 🔄 Intégration avec Thymeleaf

Pour utiliser un template dans l'application Spring Boot :

1. **Copier le template** depuis `template/` vers `src/main/resources/templates/`
2. **Nommer le fichier** selon la convention : `nom-page.html`
3. **Dans le controller**, retourner le nom sans extension :
   ```java
   return "nom-page";  // Cherchera src/main/resources/templates/nom-page.html
   ```

**Structure des templates Thymeleaf :**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Titre de la page</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <h1 th:text="${title}">Titre par défaut</h1>
    
    <!-- Boucle sur une liste -->
    <div th:each="item : ${items}">
        <span th:text="${item.nom}">Nom</span>
    </div>
    
    <!-- Lien avec paramètre -->
    <a th:href="@{/nom-entite/{id}(id=${item.id})}">Voir détails</a>
    
    <!-- Formulaire -->
    <form th:action="@{/nom-entite/save}" th:object="${item}" method="post">
        <input type="text" th:field="*{nom}" />
        <button type="submit">Enregistrer</button>
    </form>
</body>
</html>
```

---

## ⚙️ Configuration de la Base de Données

### Fichier à modifier : `src/main/resources/application.properties`

**⚠️ IMPORTANT :** Ce fichier contient les informations de connexion à la base de données.
Chaque développeur doit le configurer selon son environnement local.

**Ne jamais committer ce fichier avec les vrais identifiants dans Git !**

### Configuration type pour MySQL :

```properties
# Nom de l'application
spring.application.name=Orose

# Configuration de la source de données MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/nom_de_ta_base?useSSL=false&serverTimezone=UTC
spring.datasource.username=ton_utilisateur_mysql
spring.datasource.password=ton_mot_de_passe_mysql
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Configuration Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=file:src/main/resources/templates/
spring.thymeleaf.suffix=.html

# Port du serveur (optionnel, par défaut 8080)
server.port=8080
```

### Explication des propriétés :

- `spring.datasource.url` : URL de connexion à ta base MySQL
- `spring.datasource.username` : Ton utilisateur MySQL
- `spring.datasource.password` : Ton mot de passe MySQL
- `spring.jpa.hibernate.ddl-auto=update` : Met à jour le schéma automatiquement
- `spring.jpa.show-sql=true` : Affiche les requêtes SQL dans la console (utile pour le debug)
- `spring.thymeleaf.cache=false` : Désactive le cache en développement (rechargement automatique)

### Pour les tests locaux :

1. **Créer ta base de données MySQL :**
   ```sql
   CREATE DATABASE nom_de_ta_base CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Modifier `application.properties`** avec TES identifiants :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/TA_BASE
   spring.datasource.username=TON_UTILISATEUR
   spring.datasource.password=TON_MOT_DE_PASSE
   ```

3. **Partager la structure sans les identifiants :**
   - Créer un fichier `application.properties.example` avec des valeurs factices
   - Ajouter `application.properties` dans `.gitignore`
   - Chaque développeur copie `application.properties.example` vers `application.properties` et le configure

---

## 🚀 Commandes de Compilation

### Avec Maven Wrapper (recommandé - pas besoin d'installer Maven)

```bash
# Sur Linux/Mac
./mvnw clean compile

# Sur Windows
mvnw.cmd clean compile
```

### Avec Maven installé globalement

```bash
mvn clean compile
```

### Compiler et exécuter l'application

```bash
# Avec Maven Wrapper
./mvnw spring-boot:run

# Ou compiler et créer un JAR exécutable
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Commandes Maven utiles

```bash
# Nettoyer le projet (supprime le dossier target)
./mvnw clean

# Compiler le code source
./mvnw compile

# Exécuter les tests unitaires
./mvnw test

# Compiler et exécuter les tests
./mvnw verify

# Créer le package JAR
./mvnw package

# Lancer l'application Spring Boot
./mvnw spring-boot:run

# Installer les dépendances en local
./mvnw install
```

### Options de compilation avancées

```bash
# Compiler sans exécuter les tests
./mvnw clean compile -DskipTests

# Compiler en mode silencieux (moins de logs)
./mvnw clean compile -q

# Voir les détails de la compilation
./mvnw clean compile -X
```

---

## 🔄 Workflow Type pour Ajouter une Fonctionnalité

1. **Créer l'entité (Model)**
   - Fichier : `src/main/java/com/example/orose/model/MonEntite.java`
   - Ajouter les annotations JPA nécessaires

2. **Créer le Repository**
   - Fichier : `src/main/java/com/example/orose/repository/MonEntiteRepository.java`
   - Étendre `JpaRepository`

3. **Créer le Service**
   - Fichier : `src/main/java/com/example/orose/service/MonEntiteService.java`
   - Injecter le repository avec `@Autowired`

4. **Créer le DTO (si nécessaire)**
   - Fichier : `src/main/java/com/example/orose/dto/MonEntiteDTO.java`
   - Utiliser pour les échanges de données

5. **Créer le Controller**
   - Fichier : `src/main/java/com/example/orose/controller/MonEntiteController.java`
   - Injecter le service avec `@Autowired`
   - Définir les routes avec `@GetMapping`, `@PostMapping`, etc.

6. **Préparer la View**
   - Copier le template depuis `template/` vers `src/main/resources/templates/`
   - Adapter le template Thymeleaf si nécessaire

7. **Configurer la base de données**
   - Modifier `src/main/resources/application.properties` avec tes identifiants

8. **Compiler**
   ```bash
   ./mvnw clean compile
   ```

9. **Tester**
   ```bash
   ./mvnw spring-boot:run
   ```

10. **Vérifier dans le navigateur**
    - Accéder à `http://localhost:8080/ton-endpoint`

---

## 📝 Bonnes Pratiques

### Nommage des packages et classes
- **Entités** : Nom au singulier, majuscule initiale (`Utilisateur`, `Bassin`)
- **Repository** : `NomEntiteRepository`
- **Service** : `NomEntiteService`
- **Controller** : `NomEntiteController`
- **DTO** : `NomEntiteDTO`
- **Templates** : kebab-case (`nom-entite-list.html`)

### Organisation du code
- Une classe = une responsabilité unique
- Les controllers ne doivent pas contenir de logique métier
- La logique métier va dans les services
- Les services communiquent avec les repositories
- Les DTOs pour les échanges de données

### Sécurité
- Ne jamais committer `application.properties` avec les vrais identifiants
- Utiliser des variables d'environnement pour les informations sensibles en production
- Valider les entrées utilisateur dans les controllers

### Performance
- Utiliser `@Transactional` au niveau des services pour les opérations multiples
- Éviter les requêtes N+1 avec `@Query` personnalisé si nécessaire
- Indexer les colonnes fréquemment recherchées en base de données

---

