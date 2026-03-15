import { Modal } from '@components/common/feedback/Modal';
import { Input } from '@components/common/forms/Input';
import { Button } from '@components/common/forms/Button';

const ChangePasswordModal = ({
  isOpen,
  onClose,
  onSubmit,
  registerPassword,
  passwordErrors,
  isPending
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Changer le mot de passe">
      <form onSubmit={onSubmit} className="space-y-4">
        <Input
          label="Mot de passe actuel"
          required
          type="password"
          {...registerPassword('currentPassword')}
          error={passwordErrors.currentPassword?.message}
        />
        <Input
          label="Nouveau mot de passe"
          required
          type="password"
          {...registerPassword('newPassword')}
          error={passwordErrors.newPassword?.message}
        />
        <Input
          label="Confirmer le mot de passe"
          required
          type="password"
          {...registerPassword('confirmPassword')}
          error={passwordErrors.confirmPassword?.message}
        />

        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending ? 'Mise a jour...' : 'Mettre a jour'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default ChangePasswordModal;
