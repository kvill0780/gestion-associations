import {
  HomeIcon,
  UserCircleIcon,
  UsersIcon,
  BanknotesIcon,
  CreditCardIcon,
  CalendarIcon,
  DocumentTextIcon,
  ChatBubbleLeftRightIcon,
  MegaphoneIcon,
  PhotoIcon,
  CheckBadgeIcon,
  Cog6ToothIcon
} from '@heroicons/react/24/outline';

export const navigationItems = [
  { name: 'Tableau de bord', href: '/dashboard', icon: HomeIcon },
  { name: 'Mon profil', href: '/profile', icon: UserCircleIcon },
  { name: 'Membres', href: '/members', icon: UsersIcon, permissionsAny: ['members.view'] },
  { name: 'Finances', href: '/transactions', icon: BanknotesIcon, permissionsAny: ['finances.view'] },
  { name: 'Cotisations', href: '/contributions', icon: CreditCardIcon, permissionsAny: ['finances.view'] },
  { name: 'Événements', href: '/events', icon: CalendarIcon, permissionsAny: ['events.view'] },
  {
    name: 'Documents',
    href: '/documents',
    icon: DocumentTextIcon,
    feature: 'documents',
    permissionsAny: ['documents.view']
  },
  {
    name: 'Messages',
    href: '/messages',
    icon: ChatBubbleLeftRightIcon,
    feature: 'messages',
    permissionsAny: ['messages.view']
  },
  {
    name: 'Annonces',
    href: '/announcements',
    icon: MegaphoneIcon,
    feature: 'announcements',
    permissionsAny: ['announcements.view']
  },
  {
    name: 'Galerie',
    href: '/gallery',
    icon: PhotoIcon,
    feature: 'gallery',
    permissionsAny: ['gallery.view']
  },
  {
    name: 'Votes',
    href: '/votes',
    icon: CheckBadgeIcon,
    feature: 'votes',
    permissionsAny: ['votes.view']
  },
  {
    name: 'Paramètres',
    href: '/settings',
    icon: Cog6ToothIcon,
    permissionsAny: ['roles.manage', 'posts.manage', 'settings.view', 'settings.update']
  }
];
