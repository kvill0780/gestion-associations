# Associa Frontend

Application de gestion d'associations developpee avec React + Vite.

## Demarrage rapide

```bash
npm install
npm run dev
```

Build et preview :

```bash
npm run build
npm run preview
```

## Configuration

Copier `.env.example` vers `.env` et configurer :

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=Associa
VITE_APP_VERSION=1.0.0
```

## Structure du projet

```
src/
├── app/              # App et router
├── features/         # Pages et domaines metiers
├── shared/           # Composants, hooks, api, utils, styles
└── main.jsx
```

## Technologies

- React 18
- Vite
- React Router
- TanStack Query
- Zustand
- Axios
- Tailwind CSS
- date-fns

## Scripts

- `npm run dev`
- `npm run build`
- `npm run preview`
- `npm run lint`
