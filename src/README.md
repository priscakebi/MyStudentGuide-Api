# MyStudentGuide API

API REST développée avec Spring Boot 4 pour accompagner les étudiants étrangers dans leurs démarches administratives en France.

## Technologies

- Java 21
- Spring Boot 4.0.7
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Swagger/OpenAPI
- Lombok

## Architecture


src/main/java/com/mystudentguide

- config
- controller
- dto
- entity
- enums
- exception
- mapper
- repository
- security
- service
- validation


## Fonctionnalités

- Authentification JWT
- Gestion des utilisateurs
- Gestion du profil étudiant
- Génération des démarches administratives
- Notifications
- Gestion des documents
- Tableau de bord

## Installation

### Prérequis

- Java 21
- Maven
- MySQL

### Cloner le projet

```bash
git clone https://github.com/ton-compte/mystudentguide-api.git
```

### Configurer la base

Créer une base nommée :

```sql
CREATE DATABASE mystudentguide;
```

### Lancer l'application

```bash
mvn spring-boot:run
```

## Documentation

Une fois l'application démarrée :

- Swagger UI : `http://localhost:8080/swagger-ui.html`
- OpenAPI : `http://localhost:8080/api-docs`

## Structure de la base

Les principales entités sont :

- User
- StudentProfile
- Demarche
- Categorie
- Notification
- Document

## Configuration (variables d'environnement)

Voir `.env.example` à la racine du projet API. Principales variables :
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (32 caractères minimum),
`CORS_ALLOWED_ORIGINS`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

Lancer avec les logs SQL : `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

## Auteur

**Kouenin Kebi Prisca **

Projet réalisé dans le cadre du titre professionnel **Concepteur Développeur d'Applications (CDA)**.