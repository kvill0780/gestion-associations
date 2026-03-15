import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { PlusIcon, PencilIcon, TrashIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { Modal } from '@components/common/feedback/Modal';
import { usePosts, useCreatePost, useUpdatePost, useDeletePost } from '@hooks/usePosts';
import { useRoles } from '@hooks/useRoles';
import { useAuthStore } from '@store/authStore';
import toast from 'react-hot-toast';

const PostsTab = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingPost, setEditingPost] = useState(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm();

  const associationId = useAuthStore((state) => state.user?.associationId);

  const { data: posts, isLoading } = usePosts();
  const { data: roles } = useRoles(associationId ? { associationId } : undefined);
  const createMutation = useCreatePost();
  const updateMutation = useUpdatePost();
  const deleteMutation = useDeletePost();

  const normalizeDefaultRoleId = (value) => {
    if (value === undefined || value === null || value === '') {
      return null;
    }
    const parsed = Number(value);
    return Number.isNaN(parsed) ? null : parsed;
  };

  const onSubmit = (data) => {
    const defaultRoleId = normalizeDefaultRoleId(data.defaultRoleId);

    if (editingPost) {
      const updateData = {
        name: data.name,
        description: data.description,
        defaultRoleId: defaultRoleId ?? undefined,
        clearDefaultRole: defaultRoleId === null
      };

      updateMutation.mutate(
        { id: editingPost.id, data: updateData },
        {
          onSuccess: () => {
            setIsModalOpen(false);
            setEditingPost(null);
            reset();
          }
        }
      );
      return;
    }

    if (!associationId) {
      toast.error("Aucune association active : impossible de créer un poste");
      return;
    }

    createMutation.mutate(
      {
        name: data.name,
        description: data.description,
        associationId,
        defaultRoleId: defaultRoleId ?? undefined
      },
      {
        onSuccess: () => {
          setIsModalOpen(false);
          reset();
        }
      }
    );
  };

  const handleEdit = (post) => {
    setEditingPost(post);
    reset({
      name: post.name,
      description: post.description || '',
      defaultRoleId: post.defaultRoleId || ''
    });
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce poste ?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingPost(null);
    reset({ name: '', description: '', defaultRoleId: '' });
  };

  const handleOpenCreate = () => {
    if (!associationId) {
      toast.error("Aucune association active : impossible de créer un poste");
      return;
    }
    reset({ name: '', description: '', defaultRoleId: '' });
    setIsModalOpen(true);
  };

  if (isLoading) {
    return <div>Chargement...</div>;
  }

  const postsList = posts || [];
  const rolesList = roles || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Postes</h3>
          <p className="text-sm text-gray-600">Gérer les postes de l'organisation</p>
        </div>
        <Button onClick={handleOpenCreate} disabled={!associationId}>
          <PlusIcon className="mr-2 h-5 w-5" />
          Nouveau poste
        </Button>
      </div>

      <Card>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Nom
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Rôle par défaut
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Description
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {postsList.map((post) => (
                <tr key={post.id}>
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                    {post.name}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600">
                    {post.defaultRoleName || '-'}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{post.description || '-'}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-right text-sm">
                    <button
                      onClick={() => handleEdit(post)}
                      className="mr-3 text-primary-600 hover:text-primary-900"
                    >
                      <PencilIcon className="h-5 w-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(post.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <TrashIcon className="h-5 w-5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={editingPost ? 'Modifier le poste' : 'Nouveau poste'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Nom du poste"
            required
            {...register('name', { required: 'Le nom est requis' })}
            error={errors.name?.message}
          />

          <Input
            label="Description"
            {...register('description')}
            error={errors.description?.message}
          />

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Rôle par défaut</label>
            <select
              {...register('defaultRoleId')}
              className="w-full rounded-md border px-3 py-2"
            >
              <option value="">Aucun rôle par défaut</option>
              {rolesList.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            <p className="mt-1 text-xs text-gray-500">
              Utilisé pour l'assignation automatique lors de la création d'un mandat.
            </p>
          </div>

          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={handleCloseModal}>
              Annuler
            </Button>
            <Button type="submit" disabled={!editingPost && !associationId}>
              {editingPost ? 'Modifier' : 'Créer'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default PostsTab;
