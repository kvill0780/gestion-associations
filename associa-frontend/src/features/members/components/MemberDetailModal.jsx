import { useState } from 'react';
import { 
  UserIcon, 
  CreditCardIcon, 
  BriefcaseIcon, 
  CalendarIcon,
  PencilIcon,
  EnvelopeIcon,
  PhoneIcon,
  MapPinIcon
} from '@heroicons/react/24/outline';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { formatDate, formatCurrency } from '@utils/formatters';
import { getInitials } from '@utils/helpers';
import { useContributions } from '@hooks/useContributions';

const MemberDetailModal = ({ member, isOpen, onClose, onEdit }) => {
  const [activeTab, setActiveTab] = useState('info');

  if (!member) return null;

  const tabs = [
    { id: 'info', label: 'Informations', icon: UserIcon },
    { id: 'payments', label: 'Cotisations', icon: CreditCardIcon },
    { id: 'mandates', label: 'Mandats', icon: BriefcaseIcon },
    { id: 'events', label: 'Événements', icon: CalendarIcon }
  ];

  const normalizeStatus = (status) => {
    if (!status) return status;
    return String(status).toLowerCase();
  };

  const getStatusBadge = (status) => {
    const normalizedStatus = normalizeStatus(status);
    const variants = {
      active: 'success',
      pending: 'warning',
      inactive: 'danger',
      suspended: 'danger',
      expired: 'warning',
      left: 'secondary'
    };
    const labels = {
      active: 'Actif',
      pending: 'En attente',
      inactive: 'Inactif',
      suspended: 'Suspendu',
      expired: 'Expiré',
      left: 'Parti'
    };
    return <Badge variant={variants[normalizedStatus] || 'default'}>{labels[normalizedStatus] || status}</Badge>;
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="2xl" title="">
      <div className="space-y-6">
        {/* Header avec photo et infos principales */}
        <div className="flex items-start justify-between border-b pb-6">
          <div className="flex items-start space-x-4">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary-600 text-2xl font-bold text-white">
              {getInitials(member.firstName, member.lastName)}
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                {member.firstName} {member.lastName}
              </h2>
              <p className="text-gray-600">{member.email}</p>
              <div className="mt-2 flex items-center gap-2">
                {getStatusBadge(member.membershipStatus)}
                {member.roles?.map((role, idx) => (
                  <Badge key={idx} variant="info">
                    {role.name || role}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
          <Button variant="secondary" onClick={() => onEdit(member)}>
            <PencilIcon className="mr-2 h-4 w-4" />
            Modifier
          </Button>
        </div>

        {/* Onglets */}
        <div className="border-b">
          <nav className="-mb-px flex space-x-8">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center border-b-2 px-1 py-4 text-sm font-medium ${
                    activeTab === tab.id
                      ? 'border-primary-500 text-primary-600'
                      : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                  }`}
                >
                  <Icon className="mr-2 h-5 w-5" />
                  {tab.label}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Contenu des onglets */}
        <div className="min-h-[300px]">
          {activeTab === 'info' && <InfoTab member={member} />}
          {activeTab === 'payments' && <PaymentsTab member={member} />}
          {activeTab === 'mandates' && <MandatesTab member={member} />}
          {activeTab === 'events' && <EventsTab member={member} />}
        </div>
      </div>
    </Modal>
  );
};

// Onglet Informations
const InfoTab = ({ member }) => (
  <div className="space-y-6">
    <div className="grid grid-cols-2 gap-6">
      <InfoItem icon={EnvelopeIcon} label="Email" value={member.email} />
      <InfoItem icon={PhoneIcon} label="WhatsApp / Téléphone" value={member.whatsapp || 'Non renseigné'} />
      <InfoItem icon={MapPinIcon} label="Centres d'intérêt" value={member.interests || 'Non renseigné'} />
      <InfoItem
        icon={CalendarIcon}
        label="Date d'adhésion"
        value={member.membershipDate ? formatDate(member.membershipDate) : 'Non renseigné'}
      />
    </div>

    <div>
      <h4 className="mb-2 font-semibold text-gray-900">Rôles et Permissions</h4>
      <div className="space-y-2">
        {member.roles?.map((role, idx) => (
          <div key={idx} className="rounded-lg border p-3">
            <div className="flex items-center justify-between">
              <span className="font-medium text-gray-900">{role.name || role}</span>
              {typeof role === 'object' && role.permissions && (
                <Badge variant="default">
                  {Object.values(role.permissions).filter(v => v === true).length} permissions
                </Badge>
              )}
            </div>
            {typeof role === 'object' && role.permissions && (
              <div className="mt-2 flex flex-wrap gap-1">
                {Object.entries(role.permissions)
                  .filter(([_, value]) => value === true)
                  .slice(0, 5)
                  .map(([key], i) => (
                    <span key={i} className="rounded bg-gray-100 px-2 py-1 text-xs text-gray-600">
                      {key}
                    </span>
                  ))}
                {Object.values(role.permissions).filter(v => v === true).length > 5 && (
                  <span className="rounded bg-gray-100 px-2 py-1 text-xs text-gray-600">
                    +{Object.values(role.permissions).filter(v => v === true).length - 5}
                  </span>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  </div>
);

// Onglet Cotisations
const PaymentsTab = ({ member }) => {
  const year = new Date().getFullYear();
  const { data, isLoading } = useContributions({ year });
  const contributions = (data?.data || []).filter((c) => c.member_id === member.id);

  const getStatusBadge = (status) => {
    const normalized = String(status || '').toLowerCase();
    const variants = {
      paid: 'success',
      partial: 'warning',
      late: 'danger',
      late_partial: 'danger',
      pending: 'default',
      waived: 'info'
    };
    const labels = {
      paid: 'Payee',
      partial: 'Partielle',
      late: 'En retard',
      late_partial: 'Retard partiel',
      pending: 'En attente',
      waived: 'Exoneree'
    };
    return (
      <Badge variant={variants[normalized] || 'default'}>
        {labels[normalized] || status}
      </Badge>
    );
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="md" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {contributions.length > 0 ? (
        <div className="space-y-3">
          {contributions.map((contribution) => (
            <div key={contribution.id} className="flex flex-col justify-between gap-3 rounded-lg border p-4 sm:flex-row sm:items-center">
              <div>
                <p className="font-medium text-gray-900">
                  {contribution.period_label || '—'}
                </p>
                <p className="text-sm text-gray-500">
                  Echeance {contribution.due_date ? formatDate(contribution.due_date) : '-'}
                </p>
              </div>
              <div className="text-right">
                <p className="font-semibold text-gray-900">
                  {formatCurrency(contribution.expected_amount || 0)}
                </p>
                <p className="text-xs text-gray-500">
                  Paye {formatCurrency(contribution.paid_amount || 0)} • Reste {formatCurrency(contribution.remaining_amount || 0)}
                </p>
                <div className="mt-2 flex justify-end">
                  {getStatusBadge(contribution.status)}
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-12 text-center text-gray-500">
          <CreditCardIcon className="mx-auto h-12 w-12 text-gray-400" />
          <p className="mt-2">Aucune cotisation pour cette annee</p>
        </div>
      )}
    </div>
  );
};

// Onglet Mandats
const MandatesTab = ({ member }) => {
  const mandates = member.mandates || member.currentMandates || [];

  return (
    <div className="space-y-4">
      {mandates.length > 0 ? (
        <div className="space-y-3">
          {mandates.map((mandate) => (
            <div key={mandate.id} className="rounded-lg border p-4">
              <div className="flex items-start justify-between">
                <div>
                  <h4 className="font-semibold text-gray-900">{mandate.postName || mandate.post_name || '-'}</h4>
                  <p className="text-sm text-gray-500">
                    {formatDate(mandate.startDate || mandate.start_date)} - {(mandate.endDate || mandate.end_date)
                      ? formatDate(mandate.endDate || mandate.end_date)
                      : 'En cours'}
                  </p>
                </div>
                <Badge variant={mandate.active ? 'success' : 'default'}>
                  {mandate.active ? 'Actif' : 'Terminé'}
                </Badge>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-12 text-center text-gray-500">
          <BriefcaseIcon className="mx-auto h-12 w-12 text-gray-400" />
          <p className="mt-2">Aucun mandat attribué</p>
        </div>
      )}
    </div>
  );
};

// Onglet Événements
const EventsTab = ({ member }) => {
  const events = member.events || [];

  return (
    <div className="space-y-4">
      {events.length > 0 ? (
        <div className="space-y-3">
          {events.map((event) => (
            <div key={event.id} className="flex items-center justify-between rounded-lg border p-4">
              <div>
                <h4 className="font-medium text-gray-900">{event.title}</h4>
                <p className="text-sm text-gray-500">{formatDate(event.date)}</p>
              </div>
              <Badge variant={event.attended ? 'success' : 'default'}>
                {event.attended ? 'Présent' : 'Inscrit'}
              </Badge>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-12 text-center text-gray-500">
          <CalendarIcon className="mx-auto h-12 w-12 text-gray-400" />
          <p className="mt-2">Aucun événement</p>
        </div>
      )}
    </div>
  );
};

// Composant InfoItem
const InfoItem = ({ icon: Icon, label, value }) => (
  <div className="flex items-start space-x-3">
    <Icon className="h-5 w-5 text-gray-400" />
    <div>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="font-medium text-gray-900">{value}</p>
    </div>
  </div>
);

export default MemberDetailModal;
