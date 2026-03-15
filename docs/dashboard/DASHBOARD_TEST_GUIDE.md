# Guide de Test - Dashboard Dynamique Basé sur les Permissions

## 🎯 Objectif
Vérifier que le dashboard affiche les bons widgets selon les permissions de l'utilisateur et que les messages d'accueil sont personnalisés.

---

## 📋 Comptes de test disponibles

### 1. Super Admin (Plateforme)
- **Email**: `admin@associa.bf`
- **Password**: `password`
- **Permissions**: Toutes (gère toutes les associations)
- **Redirection attendue**: `/system/dashboard`
- **Widgets attendus**: Dashboard système (pas le dashboard association)

### 2. Président C2I (Admin complet)
- **Email**: `president.c2i@associa.bf`
- **Password**: `password`
- **Permissions**: `admin_all` (toutes permissions sauf super_admin)
- **Redirection attendue**: `/dashboard`
- **Message attendu**: 🎯 "Tableau de bord administrateur"
- **Widgets attendus**: TOUS les widgets (10)
  - StatsOverview
  - PendingMembersWidget
  - FinanceSummaryWidget
  - TransactionsListWidget
  - BudgetWidget
  - UpcomingEventsWidget
  - DocumentsWidget
  - QuickActionsWidget
  - ActivityFeedWidget
  - WelcomeWidget

### 3. Utilisateur Test (Membre C2I)
- **Email**: `test@associa.bf`
- **Password**: `password`
- **Permissions**: À vérifier (probablement aucune ou limitées)
- **Redirection attendue**: `/dashboard`
- **Message attendu**: ✨ "Bienvenue [Prénom]" ou 👋 "Tableau de bord"
- **Widgets attendus**: Widgets publics uniquement
  - ActivityFeedWidget
  - WelcomeWidget
  - (Possiblement UpcomingEventsWidget si events.view)

---

## ✅ Scénarios de test

### Scénario 1: Super Admin
**Étapes:**
1. Se connecter avec `admin@associa.bf`
2. Vérifier la redirection vers `/system/dashboard`
3. Vérifier que c'est le `SuperAdminDashboard` qui s'affiche
4. Se déconnecter

**Résultat attendu:** ✅ Dashboard système affiché

---

### Scénario 2: Président (Admin complet)
**Étapes:**
1. Se connecter avec `president.c2i@associa.bf`
2. Vérifier la redirection vers `/dashboard`
3. Vérifier le header:
   - Icône: 🎯
   - Titre: "Tableau de bord administrateur"
   - Description: "Gestion complète de votre association"
4. Compter les widgets affichés (devrait être ~10)
5. Vérifier la présence de:
   - Stats (4 cartes en haut)
   - Membres en attente
   - Graphique finances
   - Actions rapides
6. Vérifier le footer: "10 widgets affichés"

**Résultat attendu:** ✅ Tous les widgets visibles

---

### Scénario 3: Membre simple
**Étapes:**
1. Se connecter avec `test@associa.bf`
2. Vérifier la redirection vers `/dashboard`
3. Vérifier le header:
   - Icône: ✨ ou 👋
   - Titre: "Bienvenue [Prénom]" ou "Tableau de bord"
4. Compter les widgets (probablement 2-3)
5. Si aucun widget:
   - Vérifier l'affichage de l'état vide
   - Vérifier les liens "Voir les événements" et "Lire les annonces"

**Résultat attendu:** ✅ Widgets limités ou état vide

---

## 🔍 Points de vérification techniques

### Backend - Endpoint `/api/auth/me`
**Vérifier la structure de la réponse:**
```json
{
  "status": "success",
  "message": "User info retrieved",
  "data": {
    "id": 2,
    "email": "president.c2i@associa.bf",
    "firstName": "Président",
    "lastName": "C2I",
    "isSuperAdmin": false,
    "roles": ["Président"],
    "permissions": [
      "members.view",
      "members.create",
      "finances.view",
      "finances.approve",
      ...
    ],
    "associationId": 1,
    "associationName": "C2I - Club Informatique de l'IBAM"
  }
}
```

**Points critiques:**
- ✅ `permissions` doit être un **array de strings**
- ✅ `isSuperAdmin` doit être un **boolean**
- ✅ `roles` doit contenir les noms des rôles

---

### Frontend - Console du navigateur
**Vérifier dans la console:**
```javascript
// Récupérer l'utilisateur du store
const user = useAuthStore.getState().user;
console.log('User:', user);
console.log('Permissions:', user.permissions);
console.log('Is Super Admin:', user.isSuperAdmin);

// Tester les fonctions de permissions
import { hasPermission } from '@utils/permissions';
console.log('Has members.view:', hasPermission(user, 'members.view'));
console.log('Has finances_all:', hasPermission(user, 'finances_all'));
```

---

## 🐛 Problèmes potentiels et solutions

### Problème 1: Aucun widget affiché pour le président
**Cause possible:** Backend n'envoie pas les permissions
**Solution:** Vérifier `/api/auth/me` et s'assurer que `permissions` est rempli

### Problème 2: Tous les widgets affichés pour un membre simple
**Cause possible:** `isSuperAdmin` est `true` par erreur
**Solution:** Vérifier le DataSeeder et la réponse `/api/auth/me`

### Problème 3: Erreur "Widget not found"
**Cause possible:** Nom de composant incorrect dans `widgets.config.js`
**Solution:** Vérifier que tous les noms correspondent aux imports dans `DashboardPage.jsx`

### Problème 4: Message "Bienvenue undefined"
**Cause possible:** `user.firstName` est null
**Solution:** Vérifier que le backend envoie bien `firstName` dans `/api/auth/me`

---

## 📊 Matrice de permissions attendues

| Rôle | Permissions | Widgets visibles |
|------|-------------|------------------|
| Super Admin | Toutes | Dashboard système |
| Président | `admin_all` | 10 widgets |
| Trésorier | `finances_all` | 5-6 widgets (stats, finances, budget, transactions, activités) |
| Secrétaire | `members.approve`, `documents.upload` | 4-5 widgets (stats, membres, documents, activités) |
| Membre | Aucune | 2-3 widgets (welcome, activités, événements) |

---

## ✅ Checklist finale

- [ ] Build frontend réussi sans erreur
- [ ] Backend démarré et accessible
- [ ] DataSeeder exécuté (comptes créés)
- [ ] Login super admin → `/system/dashboard`
- [ ] Login président → `/dashboard` avec tous les widgets
- [ ] Login membre → `/dashboard` avec widgets limités
- [ ] Messages d'accueil personnalisés
- [ ] Indicateur de nombre de widgets affiché
- [ ] État vide bien formaté (si applicable)
- [ ] Aucune erreur dans la console navigateur
- [ ] Aucune erreur dans les logs backend

---

## 🚀 Commandes utiles

### Démarrer le backend
```bash
cd associa_backend_spring_boot
./mvnw spring-boot:run
```

### Démarrer le frontend
```bash
cd associa-frontend
npm run dev
```

### Vérifier les logs backend
```bash
tail -f associa_backend_spring_boot/logs/application.log
```

### Tester l'API directement
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"president.c2i@associa.bf","password":"password"}'

# Me (avec le token)
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```
