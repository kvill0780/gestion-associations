import { useState } from 'react';
import { PlusIcon, UserGroupIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import {
  useCurrentMandates,
  useAssignPost,
  useExtendMandate,
  useGetSuggestedRole,
  useRevokeMandate
} from '@hooks/useMandates';
import { useMembers } from '@hooks/useMembers';
import { usePosts } from '@hooks/usePosts';
import { formatDate } from '@utils/formatters';
import { useAuthStore } from '@store/authStore';
import toast from 'react-hot-toast';

const MandatesTab = () => {
  const associationId = useAuthStore((state) => state.user?.associationId);
  const { data: mandates, isLoading } = useCurrentMandates();
  const { data: posts } = usePosts();
  const { data: members } = useMembers();
  const assignPost = useAssignPost(); // Utilise l'endpoint atomique
  const revokeMandate = useRevokeMandate();
  const extendMandate = useExtendMandate();
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    post_id: '',
    user_id: '',
    start_date: '',
    end_date: '',
    assign_role: true,  // Par défaut, assigner le rôle
    notes: ''
  });

  // Récupérer le rôle suggéré pour le poste sélectionné
  const { data: suggestedRole } = useGetSuggestedRole(formData.post_id);

  const handleSubmit = (e) => {
    e.preventDefault();
    assignPost.mutate(formData, {
      onSuccess: () => {
        setShowModal(false);
        setFormData({
          post_id: '',
          user_id: '',
          start_date: '',
          end_date: '',
          assign_role: true,
          notes: ''
        });
      }
    });
  };

  if (isLoading) return <Spinner size="lg" />;

  const handleOpenAssign = () => {
    if (!associationId) {
      toast.error("Aucune association active : impossible d'assigner un poste");
      return;
    }
    setShowModal(true);
  };

  const handleRevoke = (mandate) => {
    const endDate = window.prompt('Date de fin (YYYY-MM-DD) :', '');
    if (!endDate) return;

    const reason = window.prompt('Raison (optionnel) :', '') || null;

    revokeMandate.mutate({
      mandateId: mandate.id,
      endDate,
      reason
    });
  };

  const handleExtend = (mandate) => {
    const newEndDate = window.prompt('Nouvelle date de fin (YYYY-MM-DD) :', '');
    if (!newEndDate) return;

    extendMandate.mutate({
      mandateId: mandate.id,
      newEndDate
    });
  };

  const mandatesList = mandates || [];
  const postsList = posts || [];
  const membersList = members || [];

  const activeMandates = mandatesList.filter((m) => m.active);
  const pastMandates = mandatesList.filter((m) => !m.active);

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <Button onClick={handleOpenAssign} disabled={!associationId}>
          <PlusIcon className="mr-2 h-5 w-5" />
          Assigner un poste
        </Button>
      </div>

      {/* Bureau actuel */}
      <Card title="Bureau actuel" actions={<Badge variant="success">Actif</Badge>}>
        {activeMandates.length > 0 ? (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {activeMandates.map((mandate) => (
              <div key={mandate.id} className="rounded-lg border p-4">
                <div className="flex items-start space-x-3">
                  <div className="rounded-full bg-primary-100 p-3">
                    <UserGroupIcon className="h-6 w-6 text-primary-600" />
                  </div>
                  <div className="flex-1">
                    <h4 className="font-semibold text-gray-900">{mandate.postName}</h4>
                    <p className="text-sm text-gray-600">{mandate.userFullName}</p>
                    <p className="mt-2 text-xs text-gray-500">
                      Début: {mandate.startDate ? formatDate(mandate.startDate) : '-'}
                    </p>
                    <div className="mt-3 flex gap-2">
                      <Button
                        variant="danger"
                        className="text-xs"
                        onClick={() => handleRevoke(mandate)}
                        disabled={revokeMandate.isPending}
                      >
                        Révoquer
                      </Button>
                      <Button
                        variant="secondary"
                        className="text-xs"
                        onClick={() => handleExtend(mandate)}
                        disabled={extendMandate.isPending}
                      >
                        Prolonger
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="py-8 text-center text-gray-500">Aucun mandat actif</p>
        )}
      </Card>

      {/* Historique */}
      {pastMandates.length > 0 && (
        <Card title="Historique des mandats">
          <div className="space-y-3">
            {pastMandates.map((mandate) => (
              <div key={mandate.id} className="flex items-center justify-between border-b pb-3 last:border-0">
                <div>
                  <p className="font-medium text-gray-900">{mandate.postName}</p>
                  <p className="text-sm text-gray-600">{mandate.userFullName}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500">
                    Fin {mandate.endDate ? formatDate(mandate.endDate) : '-'}
                  </p>
                  <Badge variant="default" className="mt-1">Terminé</Badge>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Modal d'assignation */}
      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Assigner un poste">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <RequiredLabel required>Poste</RequiredLabel>
            <select
              value={formData.post_id}
              onChange={(e) => setFormData({ ...formData, post_id: e.target.value })}
              className="w-full rounded-md border px-3 py-2"
              required
            >
              <option value="">Sélectionner un poste...</option>
              {postsList.map((post) => (
                <option key={post.id} value={post.id}>
                  {post.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <RequiredLabel required>Membre</RequiredLabel>
            <select
              value={formData.user_id}
              onChange={(e) => setFormData({ ...formData, user_id: e.target.value })}
              className="w-full rounded-md border px-3 py-2"
              required
            >
              <option value="">Sélectionner un membre...</option>
              {membersList.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.firstName} {member.lastName}
                </option>
              ))}
            </select>
          </div>

          {/* Affichage du rôle suggéré */}
          {suggestedRole && (
            <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
              <label className="flex items-start space-x-2">
                <input
                  type="checkbox"
                  checked={formData.assign_role}
                  onChange={(e) => setFormData({ ...formData, assign_role: e.target.checked })}
                  className="mt-1 rounded"
                />
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <CheckCircleIcon className="h-5 w-5 text-blue-600" />
                    <span className="font-medium text-gray-900">
                      Attribuer automatiquement le rôle <strong>{suggestedRole.name}</strong>
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-gray-600">
                    Ce rôle est recommandé pour ce poste et donnera les permissions appropriées.
                  </p>
                  {suggestedRole.description && (
                    <p className="mt-1 text-xs text-gray-500">
                      {suggestedRole.description}
                    </p>
                  )}
                </div>
              </label>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <RequiredLabel required>Date début</RequiredLabel>
              <input
                type="date"
                value={formData.start_date}
                onChange={(e) => setFormData({ ...formData, start_date: e.target.value })}
                className="w-full rounded-md border px-3 py-2"
                required
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Date fin (optionnel)</label>
              <input
                type="date"
                value={formData.end_date}
                onChange={(e) => setFormData({ ...formData, end_date: e.target.value })}
                className="w-full rounded-md border px-3 py-2"
              />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Notes (optionnel)</label>
            <textarea
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="Raison de l'attribution, contexte de l'élection..."
              className="w-full rounded-md border px-3 py-2"
              rows={3}
            />
          </div>

          <div className="flex justify-end space-x-3">
            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={assignPost.isPending}>
              {assignPost.isPending ? 'Assignation...' : 'Assigner'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default MandatesTab;
