# 🎓 ASSOCIA - Backend Spring Boot

> Plateforme de gestion d'associations étudiantes - Backend REST API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table des Matières

- [Vue d'Ensemble](#-vue-densemble)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Démarrage](#-démarrage)
- [Tests](#-tests)
- [API Documentation](#-api-documentation)
- [Modules](#-modules)
- [Permissions](#-permissions)
- [Contribution](#-contribution)

---

## 🎯 Vue d'Ensemble

**Associa** est une plateforme complète de gestion d'associations étudiantes qui permet de :
- Gérer les membres et leurs adhésions
- Attribuer des rôles et permissions granulaires
- Gérer les postes du bureau et les mandats
- Tracer toutes les actions (audit)
- Authentifier de manière sécurisée (JWT)

### Technologies Utilisées

- **Backend**: Spring Boot 3.5.9 (Java 21)
- **Base de données**: PostgreSQL 14+
- **Sécurité**: Spring Security + JWT
- **ORM**: Hibernate/JPA
- **Cache**: Spring Cache (Redis ready)
- **Build**: Maven

---

## ✨ Fonctionnalités

### ✅ Modules Complétés (MVP Ready)

#### 🔐 Authentification & Sécurité
- [x] Login/Register avec JWT
- [x] Refresh token (rotation)
- [x] Forgot/Reset password
- [x] Logout (révocation tokens)
- [x] Protection des endpoints
- [x] CORS configuré

#### 👥 Gestion des Membres
- [x] CRUD utilisateurs
- [x] Approbation des membres
- [x] Suspension/Activation
- [x] Profils utilisateurs
- [x] Recherche et filtres

#### 🎭 Rôles & Permissions
- [x] 39 permissions granulaires
- [x] 8 macros de permissions
- [x] Gestion des rôles par association
- [x] Attribution/Révocation de rôles
- [x] Vérification avec cache

#### 🏢 Postes & Mandats
- [x] Création de postes (Président, Trésorier, etc.)
- [x] Attribution de mandats avec historique
- [x] Révocation de mandats
- [x] Liaison poste-rôle suggérée

#### 🏛️ Associations
- [x] CRUD associations
- [x] Statistiques (membres, postes, etc.)
- [x] Configuration (auto-approbation, workflow finances)
- [x] Bureau exécutif

#### 📊 Audit & Traçabilité
- [x] Logging automatique des actions
- [x] Historique complet
- [x] Statistiques d'audit
- [x] Filtres par action/utilisateur

### ⚠️ Modules Partiels (Post-MVP)
- [ ] Finances (transactions, approbations)
- [ ] Événements (création, participations)
- [ ] Documents (upload, partage)
- [ ] Communication (messages, annonces)
- [ ] Votes (scrutins, options)

---

## 🏗️ Architecture

### Structure des Packages

```
bf.kvill.associa/
├── core/                    # Configuration centrale
│   ├── config/             # Permissions, cache, rôles
│   └── security/           # Annotations, intercepteurs
├── security/               # Authentification & JWT
│   ├── auth/              # Login, register, reset
│   ├── jwt/               # Service JWT, filtres
│   └── config/            # Spring Security config
├── members/                # Gestion membres
│   ├── user/              # Utilisateurs
│   ├── role/              # Rôles & permissions
│   ├── post/              # Postes du bureau
│   └── mandate/           # Mandats
├── system/                 # Système
│   ├── association/       # Associations
│   └── audit/             # Audit & logs
├── shared/                 # Partagé
│   ├── exception/         # Gestion erreurs
│   ├── dto/               # DTOs communs
│   ├── enums/             # Énumérations
│   └── util/              # Utilitaires
└── [autres modules...]     # Finance, Events, etc.
```

### Base de Données

```
associations
    ├── users
    ├── roles
    │   └── user_roles (pivot)
    ├── posts
    │   └── mandates
    ├── refresh_tokens
    ├── password_reset_tokens
    └── audit_logs
```

---

## 📦 Prérequis

- **Java 21** ou supérieur
- **PostgreSQL 14+**
- **Maven 3.8+**
- **Git**

### Installation des Prérequis

#### Ubuntu/Debian
```bash
# Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# PostgreSQL
sudo apt install postgresql postgresql-contrib

# Maven
sudo apt install maven
```

#### macOS
```bash
# Homebrew
brew install openjdk@21
brew install postgresql@14
brew install maven
```

---

## 🚀 Installation

### 1. Cloner le Projet

```bash
git clone https://github.com/votre-repo/associa.git
cd associa/associa_backend_spring_boot
```

### 2. Créer la Base de Données

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql

# Créer la base de données
CREATE DATABASE associa;
CREATE USER kvill WITH PASSWORD 'postgre';
GRANT ALL PRIVILEGES ON DATABASE associa TO kvill;
\q
```

### 3. Configurer l'Application

Éditer `src/main/resources/application.properties` :

```properties
# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/associa
spring.datasource.username=kvill
spring.datasource.password=postgre

# JWT Secret (CHANGER EN PRODUCTION)
jwt.secret=VotreCleSuperSecreteIci256BitsMinimum
```

### 4. Compiler le Projet

```bash
./mvnw clean install
```

---

## ⚙️ Configuration

### Variables d'Environnement

```bash
# JWT Secret (recommandé en production)
export JWT_SECRET="votre-cle-secrete-256-bits"

# Base de données
export DB_URL="jdbc:postgresql://localhost:5432/associa"
export DB_USERNAME="kvill"
export DB_PASSWORD="postgre"
```

### Profils Spring

```bash
# Développement (par défaut)
./mvnw spring-boot:run

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Test
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 🎬 Démarrage

### Démarrage Simple

```bash
./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:8080**

### Vérification

```bash
# Santé de l'application
curl http://localhost:8080/actuator/health

# Réponse attendue
{"status":"UP"}
```

### Données de Test

Au premier démarrage, le `DataSeeder` crée automatiquement :

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Super Admin | admin@associa.bf | password |
| Test User | test@associa.bf | password |

---

## 🧪 Tests

### Tests Automatiques

```bash
# Exécuter le script de test
./test_api.sh
```

Ce script teste :
- ✅ Santé de l'application
- ✅ Login
- ✅ Récupération associations
- ✅ Récupération membres
- ✅ Récupération rôles
- ✅ Permissions système

### Tests Manuels avec cURL

#### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@associa.bf",
    "password": "password"
  }'
```

#### 2. Récupérer les Associations

```bash
curl -X GET http://localhost:8080/api/v1/associations \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

#### 3. Récupérer les Membres

```bash
curl -X GET http://localhost:8080/api/v1/associations/1/members \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

## 📚 API Documentation

### Endpoints Principaux

#### Authentification

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/auth/refresh` | Renouveler token |
| POST | `/api/auth/logout` | Déconnexion |
| POST | `/api/auth/forgot-password` | Demande reset |
| POST | `/api/auth/reset-password` | Reset password |

#### Associations

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/associations` | Liste associations |
| POST | `/api/v1/associations` | Créer association |
| GET | `/api/v1/associations/{id}` | Détails association |
| PUT | `/api/v1/associations/{id}` | Modifier association |
| DELETE | `/api/v1/associations/{id}` | Supprimer association |
| GET | `/api/v1/associations/{id}/stats` | Statistiques |

#### Membres

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/associations/{id}/members` | Liste membres |
| POST | `/api/v1/associations/{id}/members` | Créer membre |
| GET | `/api/v1/members/{id}` | Détails membre |
| PUT | `/api/v1/members/{id}` | Modifier membre |
| DELETE | `/api/v1/members/{id}` | Supprimer membre |
| POST | `/api/v1/members/{id}/approve` | Approuver membre |
| POST | `/api/v1/members/{id}/suspend` | Suspendre membre |

#### Rôles

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/associations/{id}/roles` | Liste rôles |
| POST | `/api/v1/associations/{id}/roles` | Créer rôle |
| PUT | `/api/v1/roles/{id}` | Modifier rôle |
| DELETE | `/api/v1/roles/{id}` | Supprimer rôle |
| POST | `/api/v1/users/{userId}/roles/{roleId}` | Attribuer rôle |

#### Mandats

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/associations/{id}/assign-post` | Attribuer mandat |
| GET | `/api/v1/associations/{id}/mandates` | Liste mandats |
| PUT | `/api/v1/mandates/{id}/revoke` | Révoquer mandat |

---

## 🔐 Permissions

### 39 Permissions Disponibles

```
members.view, members.create, members.update, members.delete, members.approve
finances.view, finances.create, finances.update, finances.delete, finances.approve, finances.export
events.view, events.create, events.update, events.delete, events.manage
documents.view, documents.upload, documents.delete, documents.share
messages.view, messages.send, messages.delete
announcements.view, announcements.create, announcements.update, announcements.delete
votes.view, votes.create, votes.manage, votes.cast
gallery.view, gallery.upload, gallery.delete
roles.manage, posts.manage, settings.view, settings.update, super_admin
```

### Macros

```
admin_all         → Toutes les permissions
members_all       → Toutes permissions membres
finances_all      → Toutes permissions finances
events_all        → Toutes permissions événements
documents_all     → Toutes permissions documents
messages_all      → Toutes permissions messages
announcements_all → Toutes permissions annonces
votes_all         → Toutes permissions votes
gallery_all       → Toutes permissions galerie
```

---

## 📖 Modules

Pour plus de détails sur chaque module, consultez :

- [MVP_READY.md](MVP_READY.md) - Documentation complète MVP
- [CHECKLIST.md](CHECKLIST.md) - Checklist de validation
- [init_database.sql](init_database.sql) - Script SQL

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez suivre ces étapes :

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📝 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👥 Auteurs

- **Kvill** - *Développement initial* - [GitHub](https://github.com/kvill)

---

## 🙏 Remerciements

- Spring Boot Team
- PostgreSQL Community
- Tous les contributeurs

---

## 📞 Support

Pour toute question ou problème :
- 📧 Email: contact@associa.bf
- 🐛 Issues: [GitHub Issues](https://github.com/votre-repo/associa/issues)
- 📚 Documentation: [Wiki](https://github.com/votre-repo/associa/wiki)

---

**🎉 Votre backend Spring Boot est prêt pour le MVP ! 🚀**
