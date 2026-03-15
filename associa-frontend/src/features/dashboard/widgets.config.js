export const DASHBOARD_WIDGET_LIMITS = Object.freeze({
  hero: 3,
  core: 6,
  secondary: 12
});

export const DASHBOARD_WIDGETS = Object.freeze([
  {
    id: 'StatsOverview',
    component: 'StatsOverview',
    permissions: ['admin_all', 'members.view', 'finances.view', 'events.view'],
    requireAll: false,
    zones: {
      preferred: 'hero',
      fallback: ['core']
    },
    priority: 1,
    span: {
      hero: 'xl:col-span-2'
    }
  },
  {
    id: 'PresidentFocusWidget',
    component: 'PresidentFocusWidget',
    permissions: ['admin_all', 'members.approve', 'finances.view', 'events.view'],
    requireAll: false,
    zones: {
      preferred: 'hero',
      fallback: ['core']
    },
    priority: 2,
    span: {
      hero: 'xl:col-span-1',
      core: 'xl:col-span-2'
    }
  },
  {
    id: 'FinanceSummaryWidget',
    component: 'FinanceSummaryWidget',
    permissions: ['finances.view', 'finances.approve', 'finances_all', 'admin_all'],
    requireAll: false,
    zones: {
      preferred: 'core',
      fallback: ['secondary']
    },
    priority: 3,
    span: {
      core: 'xl:col-span-2'
    }
  },
  {
    id: 'ContributionsWidget',
    component: 'ContributionsWidget',
    permissions: ['finances.view', 'finances.approve', 'finances_all', 'admin_all'],
    requireAll: false,
    zones: {
      preferred: 'core',
      fallback: ['secondary']
    },
    priority: 4,
    span: {
      core: 'xl:col-span-1'
    }
  },
  {
    id: 'UpcomingEventsWidget',
    component: 'UpcomingEventsWidget',
    permissions: ['events.view', 'events.manage', 'events_all', 'admin_all'],
    requireAll: false,
    zones: {
      preferred: 'core',
      fallback: ['secondary']
    },
    priority: 5
  },
  {
    id: 'QuickActionsWidget',
    component: 'QuickActionsWidget',
    permissions: [
      'admin_all',
      'members.approve',
      'finances.approve',
      'events.manage',
      'roles.manage',
      'posts.manage',
      'settings.view',
      'settings.update'
    ],
    requireAll: false,
    zones: {
      preferred: 'secondary',
      fallback: []
    },
    priority: 10
  }
]);
