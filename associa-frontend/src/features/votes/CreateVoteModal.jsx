import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import { Modal } from '@components/common/feedback/Modal';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { usePermissions } from '@hooks/usePermissions';
import { votesService } from '@api/services/votes.service';
import toast from 'react-hot-toast';

const CreateVoteModal = ({ isOpen, onClose }) => {
  const [options, setOptions] = useState(['', '']);
  const queryClient = useQueryClient();
  const { can } = usePermissions();
  const canCreateVotes = can('votes.create');
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm();

  const createMutation = useMutation({
    mutationFn: async (data) => votesService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['votes']);
      toast.success('Vote créé avec succès');
      reset();
      setOptions(['', '']);
      onClose();
    },
    onError: () => toast.error('Erreur lors de la création')
  });

  const onSubmit = (data) => {
    if (!canCreateVotes) return;

    const validOptions = options.filter((opt) => opt.trim());
    if (validOptions.length < 2) {
      toast.error('Au moins 2 options requises');
      return;
    }
    createMutation.mutate({ ...data, options: validOptions });
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Nouveau vote">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Titre"
          required
          {...register('title', { required: 'Titre requis' })}
          error={errors.title?.message}
        />

        <div>
          <RequiredLabel required className="mb-2 block text-sm font-medium text-gray-700">
            Description
          </RequiredLabel>
          <textarea
            {...register('description')}
            rows={3}
            required
            className="w-full rounded-lg border border-gray-300 px-4 py-2"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Quorum (%)"
            required
            type="number"
            {...register('quorum', { required: true, min: 0, max: 100 })}
            defaultValue={50}
          />
          <Input
            label="Majorité (%)"
            required
            type="number"
            {...register('majority', { required: true, min: 0, max: 100 })}
            defaultValue={50}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Date début"
            required
            type="datetime-local"
            {...register('start_date', { required: true })}
          />
          <Input
            label="Date fin"
            required
            type="datetime-local"
            {...register('end_date', { required: true })}
          />
        </div>

        <div>
          <RequiredLabel required className="mb-2 block text-sm font-medium text-gray-700">
            Type
          </RequiredLabel>
          <select {...register('type')} className="w-full rounded-lg border border-gray-300 px-4 py-2" required>
            <option value="simple">Vote simple (1 choix)</option>
            <option value="multiple">Vote multiple</option>
          </select>
        </div>

        <div>
          <RequiredLabel required className="mb-2 block text-sm font-medium text-gray-700">
            Options
          </RequiredLabel>
          {options.map((opt, idx) => (
            <div key={idx} className="mb-2 flex gap-2">
              <input
                value={opt}
                onChange={(e) => {
                  const newOpts = [...options];
                  newOpts[idx] = e.target.value;
                  setOptions(newOpts);
                }}
                placeholder={`Option ${idx + 1}`}
                className="flex-1 rounded-lg border border-gray-300 px-4 py-2"
                required
              />
              {options.length > 2 ? (
                <button
                  type="button"
                  onClick={() => setOptions(options.filter((_, i) => i !== idx))}
                  className="rounded-lg p-2 text-red-600 hover:bg-red-50"
                >
                  <TrashIcon className="h-5 w-5" />
                </button>
              ) : null}
            </div>
          ))}
          <Button type="button" variant="secondary" size="sm" onClick={() => setOptions([...options, ''])}>
            <PlusIcon className="mr-2 h-4 w-4" />
            Ajouter une option
          </Button>
        </div>

        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={!canCreateVotes || createMutation.isPending}>
            Créer
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CreateVoteModal;
