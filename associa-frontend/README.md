# Associa Frontend

Application de gestion d'associations développée avec React + Vite.

## 🚀 Démarrage rapide

```bash
# Installation des dépendances
npm install

# Lancer le serveur de développement
npm run dev

# Build pour la production
npm run build

# Prévisualiser le build
npm run preview
```

## 📁 Structure du projet

```
src/
├── api/              # Services API et client axios
├── assets/           # Images, icônes, fonts
├── components/       # Composants React
│   ├── common/      # Composants réutilisables
│   ├── layout/      # Layouts (Header, Sidebar, etc.)
│   └── features/    # Composants par fonctionnalité
├── config/          # Configuration (API, permissions, app)
├── contexts/        # React Contexts
├── hooks/           # Custom hooks
├── pages/           # Pages de l'application
├── router/          # Configuration du routing
├── store/           # State management (Zustand)
├── styles/          # Styles globaux
├── types/           # Types et enums
└── utils/           # Utilitaires et helpers
```

## 🛠️ Technologies

- **React 18** - Framework UI
- **Vite** - Build tool
- **React Router** - Routing
- **TanStack Query** - Data fetching
- **Zustand** - State management
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **date-fns** - Date manipulation

## 🔧 Configuration

Copier `.env.example` vers `.env` et configurer les variables:

```env
VITE_API_BASE_URL=http://192.168.11.121:8000/api
VITE_APP_NAME=Associa
VITE_APP_VERSION=1.0.0
```

## 📝 Conventions

- **Composants**: PascalCase (ex: `UserCard.jsx`)
- **Hooks**: camelCase avec préfixe `use` (ex: `useAuth.js`)
- **Services**: camelCase avec suffixe `.service.js`
- **Imports**: Utiliser les alias `@` (ex: `@components/...`)

## 🎨 Styling

Le projet utilise Tailwind CSS avec des variables CSS personnalisées définies dans `src/styles/variables.css`.

## 📦 Scripts disponibles

- `npm run dev` - Démarre le serveur de développement
- `npm run build` - Build pour la production
- `npm run preview` - Prévisualise le build
- `npm run lint` - Lint le code
- `npm run format` - Formate le code avec Prettier
