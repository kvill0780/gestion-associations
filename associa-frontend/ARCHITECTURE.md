# Frontend Architecture

## Objectif
Cette structure est orientee "SaaS scalable" avec separation claire:
- `app`: bootstrap applicatif (router, providers)
- `features`: fonctionnalites metier (pages, composants metier)
- `shared`: briques reutilisables (UI, hooks, api, config, utils)

## Arborescence
- `src/app`
  - `App.jsx`
  - `router/AppRouter.jsx`
- `src/features/<feature>`
  - pages et composants metier de la feature
- `src/shared`
  - `api` client HTTP + services
  - `components` UI commune
  - `config` constantes globales
  - `hooks` hooks transverses
  - `i18n` traductions
  - `store` etat global
  - `styles` styles globaux
  - `utils` helpers transverses
  - `assets` ressources statiques partagees

## Regles de placement
- Un ecran metier va dans `features/<feature>`.
- Un composant re-utilise par plusieurs features va dans `shared/components`.
- Un hook lie a une seule feature reste dans la feature.
- Un hook cross-feature va dans `shared/hooks`.
- Les appels API centralises vont dans `shared/api/services`.

## Imports
Utiliser les aliases (`@features`, `@components`, `@api`, `@hooks`, etc.) pour eviter les chemins relatifs longs.

## Performance
- Les pages sont chargees en lazy dans `AppRouter`.
- Les libs lourdes d'export (PDF/Excel) sont chargees dynamiquement a la demande.
- Garder les gros composants ou modules rarement utilises en import dynamique.
