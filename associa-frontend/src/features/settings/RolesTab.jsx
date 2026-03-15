import { useState } from 'react';
import { PlusIcon, PencilIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { useRoles, usePermissionsGrouped, useCreateRole, useUpdateRole } from '@hooks/useRoles';
import { usePermissions } from '@hooks/usePermissions';
import { useAuthStore } from '@store/authStore';

const RolesTab = () => {
  const associationId = useAuthStore((state) => state.user?.associationId);
  const { can } = usePermissions();
  const canManageRoles = can('roles.manage');
  const { data: roles, isLoading } = useRoles(associationId ? { associationId } : undefined);
  const { data: permissions, isError: permissionsError } = usePermissionsGrouped({
    enabled: canManageRoles
  });
  const createRole = useCreateRole();
  const updateRole = useUpdateRole();
  const [showModal, setShowModal] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [formData, setFormData] = useState({ name: '', permissions: {}, type: 'CUSTOM' });

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Générer le slug depuis le nom
    const slug = formData.name
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '') // Retirer accents
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '')
      .replace(/-+/g, '-');
    
    const dataToSend = editingRole
      ? {
        name: formData.name,
        permissions: formData.permissions,
        type: formData.type
      }
      : {
        name: formData.name,
        slug,
        permissions: formData.permissions,
        type: formData.type,
        associationId
      };
    
    if (editingRole) {
      updateRole.mutate(
        { roleId: editingRole.id, roleData: dataToSend },
        { onSuccess: () => setShowModal(false) }
      );
    } else {
      createRole.mutate(dataToSend, { onSuccess: () => setShowModal(false) });
    }
  };

  const handleEdit = (role) => {
    setEditingRole(role);
    setFormData({
      name: role.name,
      permissions: role.permissions || {},
      type: role.type || 'CUSTOM'
    });
    setShowModal(true);
  };

  const handleNew = () => {
    setEditingRole(null);
    setFormData({ name: '', permissions: {}, type: 'CUSTOM' });
    setShowModal(true);
  };

  const togglePermission = (permission) => {
    setFormData((prev) => ({
      ...prev,
      permissions: {
        ...prev.permissions,
        [permission]: !prev.permissions[permission]
      }
    }));
  };

  if (isLoading) return <Spinner size="lg" />;

  const rolesList = roles || [];
  const permissionCategories = permissions || [];
  return (
    <div className="space-y-6">
      {canManageRoles && (
        <div className="flex justify-end">
          <Button onClick={handleNew} disabled={!associationId}>
            <PlusIcon className="mr-2 h-5 w-5" />
            Créer un rôle
          </Button>
        </div>
      )}

      {!canManageRoles && (
        <Card>
          <p className="text-sm text-gray-600">
            Accès réservé aux administrateurs des rôles.
          </p>
        </Card>
      )}

      {canManageRoles && permissionsError && (
        <Card>
          <p className="text-sm text-red-600">
            Impossible de charger les permissions.
          </p>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        {rolesList.map((role) => (
          <Card key={role.id}>
            <div className="space-y-3">
              <div className="flex items-start justify-between">
                <h3 className="text-lg font-semibold text-gray-900">{role.name}</h3>
                {canManageRoles && (
                  <Button variant="secondary" onClick={() => handleEdit(role)} className="text-xs">
                    <PencilIcon className="h-4 w-4" />
                  </Button>
                )}
              </div>
              <div className="flex flex-wrap gap-1">
                {typeof role.permissions === 'object' && role.permissions !== null ? (
                  Object.entries(role.permissions)
                    .filter(([_, value]) => value === true)
                    .slice(0, 5)
                    .map(([key], idx) => (
                      <Badge key={idx} variant="info" className="text-xs">
                        {key}
                      </Badge>
                    ))
                ) : (
                  <Badge variant="info" className="text-xs">
                    Aucune permission
                  </Badge>
                )}
                {typeof role.permissions === 'object' && 
                 Object.values(role.permissions).filter(v => v === true).length > 5 && (
                  <Badge variant="default" className="text-xs">
                    +{Object.values(role.permissions).filter(v => v === true).length - 5}
                  </Badge>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title={editingRole ? 'Modifier le rôle' : 'Créer un rôle'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <RequiredLabel required>Nom du rôle</RequiredLabel>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full rounded-md border px-3 py-2"
              required
            />
          </div>

          <div>
            <RequiredLabel required>Type</RequiredLabel>
            <select
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              className="w-full rounded-md border px-3 py-2"
              required
              disabled={!!editingRole}
            >
              <option value="LEADERSHIP">Direction</option>
              <option value="COMMITTEE">Commission</option>
              <option value="MEMBER">Membre</option>
              <option value="CUSTOM">Personnalisé</option>
            </select>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-gray-700">Permissions</label>
            <div className="max-h-96 space-y-4 overflow-y-auto rounded-lg border p-4">
              {permissionCategories.map((cat) => (
                <div key={cat.key}>
                  <h4 className="mb-2 font-medium text-gray-900">{cat.label || cat.key}</h4>
                  <div className="space-y-2">
                    {(cat.permissions || []).map((perm) => (
                      <label key={perm} className="flex items-center space-x-2">
                        <input
                          type="checkbox"
                          checked={formData.permissions[perm] === true}
                          onChange={() => togglePermission(perm)}
                          className="rounded"
                        />
                        <span className="text-sm text-gray-700">{perm}</span>
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-end space-x-3">
            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={!associationId || createRole.isPending || updateRole.isPending}>
              {editingRole ? 'Mettre à jour' : 'Créer'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default RolesTab;
