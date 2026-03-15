import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Modal } from '@components/common/feedback/Modal';
import { Input } from '@components/common/forms/Input';
import { Button } from '@components/common/forms/Button';
import { useUpdateMember } from '@hooks/useMembers';

const MemberEditModal = ({ member, isOpen, onClose }) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
      whatsapp: '',
      interests: ''
    }
  });

  const updateMember = useUpdateMember();

  useEffect(() => {
    if (!member) return;
    reset({
      firstName: member.firstName || '',
      lastName: member.lastName || '',
      whatsapp: member.whatsapp || '',
      interests: member.interests || ''
    });
  }, [member, reset]);

  const onSubmit = (data) => {
    updateMember.mutate(
      { userId: member.id, userData: data },
      {
        onSuccess: () => {
          onClose();
        }
      }
    );
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Modifier le membre">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Prénom"
            required
            {...register('firstName', { required: 'Le prénom est requis' })}
            error={errors.firstName?.message}
          />
          <Input
            label="Nom"
            required
            {...register('lastName', { required: 'Le nom est requis' })}
            error={errors.lastName?.message}
          />
        </div>

        <Input
          label="Email"
          type="email"
          value={member?.email || ''}
          disabled
        />

        <Input
          label="WhatsApp / Téléphone"
          {...register('whatsapp')}
          error={errors.whatsapp?.message}
        />

        <Input
          label="Centres d'intérêt"
          {...register('interests')}
          error={errors.interests?.message}
        />

        <div className="flex justify-end space-x-3">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={updateMember.isPending}>
            {updateMember.isPending ? 'Enregistrement...' : 'Enregistrer'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default MemberEditModal;
