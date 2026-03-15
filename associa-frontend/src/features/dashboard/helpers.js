import { hasAnyPermission, hasPermission } from '@utils/permissions';

export const getDashboardHeader = (user) => {
  if (hasPermission(user, 'admin_all')) {
    return {
      title: 'Tableau de bord administrateur',
      description: "Voici les informations cles pour piloter l'association aujourd'hui."
    };
  }

  if (hasAnyPermission(user, ['finances.approve', 'finances_all'])) {
    return {
      title: 'Tableau de bord financier',
      description: 'Suivi rapide des finances et des decisions a prendre.'
    };
  }

  if (hasAnyPermission(user, ['members.approve', 'documents.upload'])) {
    return {
      title: 'Tableau de bord secretariat',
      description: 'Gestion des membres et documents en un coup d\'oeil.'
    };
  }

  if (hasPermission(user, 'events.manage')) {
    return {
      title: 'Tableau de bord evenements',
      description: 'Vue claire pour planifier et suivre les evenements.'
    };
  }

  return {
    title: 'Tableau de bord',
    description: "Voici les informations cles de votre association aujourd'hui."
  };
};
