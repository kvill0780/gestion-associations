# 📖 Documentation Système de Permissions - Associa

## 🎯 Vue d'Ensemble

Ce dossier contient la documentation complète du système de permissions d'Associa, incluant les corrections apportées, les guides utilisateur et les tests.

---

## 📚 Documents Disponibles

### ✅ README_PERMISSIONS.md
Ce fichier est le point d’entrée unique pour le moment. Les documents détaillés ci‑dessous sont planifiés mais non présents dans le repo.

### ⏳ À ajouter (non présents)
- `PERMISSIONS_FIXES.md`
- `GUIDE_PERMISSIONS.md`
- `ARCHITECTURE_PERMISSIONS.md`
- `TESTS_PERMISSIONS.md`

---

## 🚀 Quick Start

### Pour Administrateurs

1. Lire ce document
2. Comprendre la différence Rôles vs Postes
3. Créer vos rôles personnalisés
4. Attribuer les rôles aux membres

### Pour Développeurs

1. Lire ce document
2. Aligner le modèle permissions FE/BE
3. Tester les macros et le rôle `admin_all`
4. Coder en respectant l'architecture

---

## ⚠️ Points Clés à Retenir

### 1. Rôles ≠ Postes

```
RÔLE = Permissions (ce qu'on peut FAIRE)
POSTE = Titre (qui on EST)
```

**Exemple :**
- Jean est **Trésorier** (poste) → Titre officiel
- Jean a le rôle **Gestionnaire Financier** (rôle) → Accès finances

### 2. Hiérarchie des Permissions

```
super_admin > admin_all > *_all > permissions spécifiques
```

### 3. Cumul de Rôles

Un utilisateur peut avoir **plusieurs rôles**.  
Les permissions se **cumulent**.

---

## 🔍 Recherche Rapide

### Je veux...

**...créer un rôle personnalisé**  
→ Voir la section "Rôles ≠ Postes" ci‑dessus puis `RolesTab` côté frontend

**...comprendre une permission**  
→ Voir "Hiérarchie des Permissions"

**...tester le système**  
→ Utiliser les endpoints `/api/core/permissions/*` et les tests existants

**...voir l'architecture**  
→ Voir `../architecture/archi.md`

---

## 📊 Statistiques

```
✅ 39 permissions définies
✅ 10 catégories
✅ 7 templates de rôles
✅ 3 niveaux hiérarchiques
✅ 4 documents de référence
```

---

## 🆘 Support

### Questions Fréquentes

**Q: Pourquoi mon utilisateur n'a pas accès alors qu'il a le poste ?**  
R: Le poste est symbolique. Il faut AUSSI attribuer un rôle avec permissions.

**Q: Comment donner accès complet à quelqu'un ?**  
R: Attribuer le rôle "Administrateur" avec permission `admin_all`.

**Q: Puis-je modifier les rôles prédéfinis ?**  
R: Oui ! Ce sont des suggestions. Personnalisez selon vos besoins.

**Q: Un membre peut-il avoir plusieurs rôles ?**  
R: Oui ! Les permissions se cumulent.

---

## 🔄 Mises à Jour

### Version 1.0 (2024)
- ✅ Ajout 7 nouvelles permissions (votes, galerie, messages)
- ✅ Correction RolesTab (objet vs array)
- ✅ Mise à jour routes API

---

## 📞 Contact

Pour toute question :
- 📧 Contactez votre administrateur système
- 📚 Consultez la documentation
- 🐛 Signalez un bug

---

## 📝 Licence

© 2024 Associa - Tous droits réservés

---

**Dernière mise à jour :** 2024  
**Version :** 1.0  
**Statut :** ✅ Production Ready
