# Module Association - Documentation Complète

## Vue d'Ensemble

Le module **Association** est le **module racine** du système Associa. Une association est l'entité centrale qui contient :

- Membres (Users)
- Postes (Posts)
- Rôles (Roles)
- Mandats (Mandates)
- Finances
- Événements
- Documents
- Et tous les autres modules

> **Concept clé** : Chaque utilisateur appartient à UNE association. Toutes les données sont isolées par association (multi-tenancy).

## Architecture

```
modules/system/association/
├── Association.java                      ← Entité JPA
├── AssociationController.java            ← REST Controller
├── AssociationService.java               ← Business Logic
├── AssociationRepository.java            ← Data Access
├── AssociationMapper.java                ← DTO Mapping
├── AssociationSecurityService.java       ← Vérification autorisations
├── listener/
│   ├── AssociationCreatedEvent.java      ← Événement
│   └── AssociationCreatedListener.java   ← Listener (crée rôles)
└── dto/
    ├── AssociationResponseDto.java
    ├── AssociationSummaryDto.java
    ├── CreateAssociationRequest.java
    ├── UpdateAssociationRequest.java
    ├── AssociationStatsDto.java
    ├── ExecutiveBoardMemberDto.java
    ├── MemberSummaryDto.java
    └── SlugAvailabilityDto.java
```

## Modèle de Données

### Entité Association

```java
@Entity
@Table(name = "associations")
public class Association {
    Long id;
    String name;                          // "MIAGE Ouagadougou"
    String slug;                          // "miage-ouagadougou" (unique)
    String description;
    String logoPath;
    String contactEmail;
    String contactPhone;
    String address;
    AssociationType type;                 // STUDENT, PROFESSIONAL, etc.
    AssociationStatus status;             // ACTIVE, SUSPENDED, ARCHIVED
    BigDecimal defaultMembershipFee;      // Cotisation par défaut
    Integer membershipValidityMonths;     // Durée adhésion (ex: 12 mois)
    Boolean financeApprovalWorkflow;      // Double validation finances ?
    Boolean autoApproveMembers;           // Auto-approuver nouveaux membres ?
    Integer foundedYear;
    String website;
    User createdBy;                       // Fondateur
    
    // Relations
    Set<User> members;                    // OneToMany
    Set<Post> posts;                      // OneToMany
    Set<Role> roles;                      // OneToMany
    Set<Mandate> mandates;                // OneToMany
}
```

### Table SQL

```sql
CREATE TABLE associations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    logo_path VARCHAR(255),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    address VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    default_membership_fee DECIMAL(10,2),
    membership_validity_months INTEGER,
    finance_approval_workflow BOOLEAN DEFAULT false,
    auto_approve_members BOOLEAN DEFAULT false,
    founded_year INTEGER,
    website VARCHAR(255),
    created_by_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    CHECK (founded_year >= 1900 AND founded_year <= 2100)
);

CREATE INDEX idx_associations_slug ON associations (slug);
CREATE INDEX idx_associations_type ON associations (type);
CREATE INDEX idx_associations_status ON associations (status);
CREATE INDEX idx_associations_created_by ON associations (created_by_id);
```

## Endpoints API

### 1. Liste des Associations

```http
GET /api/system/associations
```

**Query Parameters** :
- `type` (String) : Filtrer par type (STUDENT, PROFESSIONAL, etc.)
- `status` (String) : Filtrer par statut (ACTIVE, INACTIVE, etc.)

**Exemple** :
```bash
GET /api/system/associations?type=STUDENT&status=ACTIVE
```

**Response 200 OK** :
```json
[
  {
    "id": 1,
    "name": "MIAGE Ouagadougou",
    "slug": "miage-ouagadougou",
    "logoPath": "/logos/miage.png",
    "type": "STUDENT",
    "status": "ACTIVE",
    "activeMembersCount": 45,
    "totalMembersCount": 52,
    "contactEmail": "contact@miage-ouaga.bf"
  }
]
```

---

### 2. Détails d'une Association

```http
GET /api/system/associations/{id}
GET /api/system/associations/slug/{slug}
```

**Exemple** :
```bash
GET /api/system/associations/1
GET /api/system/associations/slug/miage-ouagadougou
```

**Response 200 OK** :
```json
{
  "id": 1,
  "name": "MIAGE Ouagadougou",
  "slug": "miage-ouagadougou",
  "description": "Association des étudiants de MIAGE",
  "logoPath": "/logos/miage.png",
  "contactEmail": "contact@miage-ouaga.bf",
  "contactPhone": "+22670123456",
  "address": "Université de Ouagadougou",
  "type": "STUDENT",
  "status": "ACTIVE",
  "defaultMembershipFee": 10000.00,
  "membershipValidityMonths": 12,
  "financeApprovalWorkflow": false,
  "autoApproveMembers": false,
  "foundedYear": 2010,
  "website": "https://miage-ouaga.bf",
  "createdById": 1,
  "createdByName": "Jean Dupont",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

---

### 3. Créer une Association

```http
POST /api/system/associations
```

**Permissions** : `SUPER_ADMIN` uniquement

**⚠️ Action importante** : Crée automatiquement 6 rôles templates via événement

**Request Body** :
```json
{
  "name": "MIAGE Ouagadougou",
  "description": "Association des étudiants de MIAGE",
  "contactEmail": "contact@miage-ouaga.bf",
  "contactPhone": "+22670123456",
  "address": "Université de Ouagadougou",
  "type": "STUDENT",
  "defaultMembershipFee": 10000.00,
  "membershipValidityMonths": 12,
  "financeApprovalWorkflow": false,
  "autoApproveMembers": false,
  "foundedYear": 2010,
  "website": "https://miage-ouaga.bf"
}
```

**Validation** :
- `name` : Obligatoire, max 100 caractères
- `contactEmail` : Obligatoire, format email valide
- `type` : Obligatoire (STUDENT, PROFESSIONAL, CULTURAL, SPORTS, CHARITY, RELIGIOUS, LEISURE, ADVOCACY, OTHER)
- `defaultMembershipFee` : Positif, max 8 chiffres + 2 décimales
- `membershipValidityMonths` : Entre 1 et 60 mois
- `foundedYear` : Entre 1900 et 2100
- `website` : Format URL valide

**Response 201 Created** :
```json
{
  "success": true,
  "message": "Association créée avec succès",
  "data": { ... },
  "timestamp": "2024-01-28T10:30:00"
}
```

**Workflow automatique** :

```
1. Création de l'association
2. Génération du slug unique
3. Sauvegarde en base
4. Émission événement AssociationCreatedEvent
5. AssociationCreatedListener écoute l'événement
6. Création automatique de 6 rôles :
   - Président (admin_all)
   - Trésorier (finances_all)
   - Secrétaire Général (members_all + documents_all)
   - Responsable Événements (events_all)
   - Responsable Communication (announcements_all + messages_all)
   - Membre (permissions de base)
7. Log audit
```

---

### 4. Modifier une Association

```http
PUT /api/system/associations/{id}
```

**Permissions** : `SUPER_ADMIN` ou `PRESIDENT` de l'association

**Request Body** (tous champs optionnels) :
```json
{
  "description": "Nouvelle description",
  "contactEmail": "nouveau@email.bf",
  "defaultMembershipFee": 15000.00
}
```

**Response 200 OK** :
```json
{
  "success": true,
  "message": "Association mise à jour",
  "data": { ... }
}
```

**Vérification de sécurité** :
```java
@PreAuthorize("hasRole('SUPER_ADMIN') or " +
              "(hasRole('PRESIDENT') and @associationSecurityService.canManage(#id, authentication.principal.id))")
```

---

### 5. Supprimer une Association

```http
DELETE /api/system/associations/{id}
```

**Permissions** : `SUPER_ADMIN` uniquement

**⚠️ Soft Delete** : L'association est marquée comme supprimée mais les données restent

**Règle** : Impossible de supprimer si membres actifs existent

**Response 200 OK** :
```json
{
  "success": true,
  "message": "Association supprimée",
  "data": null
}
```

**Erreur 409 - Membres actifs** :
```json
{
  "status": 409,
  "message": "Impossible de supprimer : 45 membre(s) actif(s)"
}
```

---

### 6. Suspendre une Association

```http
POST /api/system/associations/{id}/suspend
```

**Permissions** : `SUPER_ADMIN` uniquement

**Effet** :
- Les membres ne peuvent plus se connecter
- Toutes les opérations sont bloquées
- Utilisé en cas de problème (non-paiement, violation, etc.)

**Response 200 OK** :
```json
{
  "success": true,
  "message": "Association suspendue",
  "data": {
    "id": 1,
    "status": "SUSPENDED",
    ...
  }
}
```

---

### 7. Activer une Association

```http
POST /api/system/associations/{id}/activate
```

**Permissions** : `SUPER_ADMIN` uniquement

Réactive une association suspendue ou inactive.

---

### 8. Archiver une Association

```http
POST /api/system/associations/{id}/archive
```

**Permissions** : `SUPER_ADMIN` uniquement

**Effet** :
- L'association devient en lecture seule
- Utilisé pour les associations dissoutes
- Les données sont conservées pour historique

---

### 9. Statistiques d'une Association

```http
GET /api/system/associations/{id}/stats
```

**Response 200 OK** :
```json
{
  "associationId": 1,
  "associationName": "MIAGE Ouagadougou",
  "totalMembers": 52,
  "activeMembers": 45,
  "inactiveMembers": 7,
  "totalPosts": 8,
  "activeMandates": 6,
  "totalRoles": 7,
  "status": "ACTIVE",
  "type": "STUDENT",
  "createdAt": "2024-01-01T10:00:00"
}
```

---

### 10. Bureau Exécutif

```http
GET /api/system/associations/{id}/executive-board
```

Liste les membres du bureau exécutif avec leurs postes.

**Response 200 OK** :
```json
[
  {
    "postId": 1,
    "postName": "Président",
    "userId": 10,
    "userFullName": "Jean Dupont",
    "userEmail": "jean@example.com",
    "startDate": "2024-01-01",
    "endDate": "2025-12-31",
    "vacant": false
  },
  {
    "postId": 2,
    "postName": "Trésorier",
    "vacant": true
  }
]
```

---

### 11. Membres Actifs

```http
GET /api/system/associations/{id}/active-members
```

Liste tous les membres actifs.

---

### 12. Recherche d'Associations

```http
GET /api/system/associations/search?query=miage
```

Recherche par nom.

---

### 13. Vérifier Disponibilité Slug

```http
GET /api/system/associations/check-slug/miage-ouaga
```

**Response 200 OK** :
```json
{
  "slug": "miage-ouaga",
  "available": false
}
```

## 🔍 Cas d'Usage

### Cas 1 : Créer une Nouvelle Association

```bash
# En tant que SUPER_ADMIN
POST /api/system/associations
{
  "name": "MIAGE Ouagadougou",
  "contactEmail": "contact@miage.bf",
  "type": "STUDENT"
}

# Résultat :
# 1. Association créée avec slug "miage-ouagadougou"
# 2. 6 rôles créés automatiquement
# 3. Prête à recevoir des membres
```

---

### Cas 2 : Afficher le Bureau d'une Association

```bash
# Interface publique : "Qui est le président ?"
GET /api/system/associations/1/executive-board

# Résultat : Liste du bureau avec noms et contacts
```

---

### Cas 3 : Suspendre une Association (non-paiement)

```bash
# En tant que SUPER_ADMIN
POST /api/system/associations/1/suspend

# Effet :
# - Tous les membres déconnectés
# - Connexion bloquée
# - Message : "Votre association est suspendue"
```

---

### Cas 4 : Statistiques pour Tableau de Bord

```bash
GET /api/system/associations/1/stats

# Affiche :
# - Nombre de membres
# - Bureau complet ?
# - Activité récente
```

## Permissions

| Endpoint | Permissions |
|----------|-------------|
| GET /associations | Tous |
| GET /associations/{id} | Tous |
| POST /associations | SUPER_ADMIN |
| PUT /associations/{id} | SUPER_ADMIN ou PRESIDENT |
| DELETE /associations/{id} | SUPER_ADMIN |
| POST /associations/{id}/suspend | SUPER_ADMIN |
| POST /associations/{id}/activate | SUPER_ADMIN |
| POST /associations/{id}/archive | SUPER_ADMIN |

## Règles Métier

### 1. Unicité du Slug

Généré automatiquement depuis le nom :
- "MIAGE Ouagadougou" → "miage-ouagadougou"
- Si déjà pris → "miage-ouagadougou-2"

```java
String slug = SlugUtils.generateSlug(name);
while (associationRepository.existsBySlug(slug)) {
    slug = originalSlug + "-" + counter++;
}
```

---

### 2. Création Automatique des Rôles

**Workflow événementiel** :

```java
// 1. Service crée l'association
Association saved = associationRepository.save(association);

// 2. Émet l'événement
eventPublisher.publishEvent(new AssociationCreatedEvent(this, saved));

// 3. Listener écoute et crée les rôles
@EventListener
public void onAssociationCreated(AssociationCreatedEvent event) {
    // Créer 6 rôles templates
}
```

---

### 3. Types d'Association

9 types disponibles :
- **STUDENT** : Étudiante (BDE, asso de filière)
- **PROFESSIONAL** : Professionnelle (ordre, alumni)
- **CULTURAL** : Culturelle (théâtre, chorale)
- **SPORTS** : Sportive (club, ligue)
- **CHARITY** : Caritative (ONG, aide)
- **RELIGIOUS** : Religieuse
- **LEISURE** : Loisirs (échecs, jeux)
- **ADVOCACY** : Défense de droits (consommateurs, syndicat)
- **OTHER** : Autre

---

### 4. Statuts d'Association

4 statuts possibles :

| Statut | Description | Effet |
|--------|-------------|-------|
| **ACTIVE** | Opérationnelle | Tout fonctionne |
| **INACTIVE** | Temporairement inactive | Connexion OK, fonctions limitées |
| **SUSPENDED** | Suspendue | Connexion bloquée |
| **ARCHIVED** | Archivée | Lecture seule |

---

### 5. Workflow de Double Validation Financière

Si `financeApprovalWorkflow = true` :
- Une transaction nécessite **2 approbations**
- Exemple : Trésorier approuve → Président valide

Si `false` :
- Une seule approbation suffit

---

### 6. Auto-Approbation des Membres

Si `autoApproveMembers = true` :
- Nouveaux membres deviennent `ACTIVE` automatiquement

Si `false` :
- Nouveaux membres restent en `PENDING`
- Nécessitent approbation manuelle via `/api/members/users/{id}/approve`

## Multi-Tenancy

Chaque utilisateur appartient à **UNE association**.

```java
@Entity
public class User {
    @ManyToOne
    @JoinColumn(name = "association_id", nullable = false)
    private Association association; // ← UN SEUL !
}
```

**Isolation des données** :

```java
// ✅ BON : Filtrer par association
List<Post> posts = postRepository.findByAssociationId(associationId);

// ❌ MAUVAIS : Oublier le filtre
List<Post> posts = postRepository.findAll(); // Mélange toutes les assos !
```

## Événements

### AssociationCreatedEvent

**Émis quand** : Une association est créée

**Écouté par** :
- `AssociationCreatedListener` → Crée les rôles templates

**Comment écouter** :

```java
@Component
public class MyListener {
    
    @EventListener
    @Transactional
    public void onAssociationCreated(AssociationCreatedEvent event) {
        Association association = event.getAssociation();
        // Faire quelque chose
    }
}
```

**Autres usages possibles** :
- Envoyer email de bienvenue au fondateur
- Créer des postes par défaut
- Initialiser des paramètres
- Logger dans système externe

## Références

- **Code source** : `modules/system/association/`
- **Événements** : `modules/system/association/listener/`
- **Sécurité** : `AssociationSecurityService.java`

---

*Documentation générée le 2024-01-28 | Version 1.0*
