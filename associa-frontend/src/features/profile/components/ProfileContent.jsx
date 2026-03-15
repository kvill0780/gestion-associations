import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card } from '@components/common/data/Card';
import { Input } from '@components/common/forms/Input';
import { Button } from '@components/common/forms/Button';
import { useAuthStore } from '@store/authStore';
import { getInitials } from '@utils/helpers';
import { useChangePassword, useUpdateProfile } from '@hooks/useProfile';
import ChangePasswordModal from './ChangePasswordModal';

const ProfileContent = () => {
  const user = useAuthStore((state) => state.user);
  const updateProfile = useUpdateProfile();
  const changePassword = useChangePassword();
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);

  const profileSchema = useMemo(
    () =>
      z.object({
        firstName: z.string().min(2, 'Le prenom est requis').max(100),
        lastName: z.string().min(2, 'Le nom est requis').max(100),
        whatsapp: z
          .string()
          .regex(/^[+]?[0-9]{8,20}$/, 'Numero WhatsApp invalide')
          .optional()
          .or(z.literal('')),
        interests: z.string().max(255, 'Texte trop long').optional().or(z.literal(''))
      }),
    []
  );

  const passwordSchema = useMemo(
    () =>
      z
        .object({
          currentPassword: z.string().min(1, 'Le mot de passe actuel est requis'),
          newPassword: z
            .string()
            .min(8, 'Le nouveau mot de passe doit contenir au moins 8 caracteres')
            .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/, 'Ajoutez une majuscule, une minuscule et un chiffre'),
          confirmPassword: z.string().min(1, 'La confirmation est requise')
        })
        .refine((data) => data.newPassword === data.confirmPassword, {
          message: 'Les mots de passe ne correspondent pas',
          path: ['confirmPassword']
        }),
    []
  );

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      whatsapp: user?.whatsapp || '',
      interests: user?.interests || ''
    }
  });

  const {
    register: registerPassword,
    handleSubmit: handleSubmitPassword,
    reset: resetPassword,
    formState: { errors: passwordErrors }
  } = useForm({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  });

  useEffect(() => {
    if (!user) return;
    reset({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      whatsapp: user.whatsapp || '',
      interests: user.interests || ''
    });
  }, [user, reset]);

  const onSubmit = (data) => {
    updateProfile.mutate({
      firstName: data.firstName.trim(),
      lastName: data.lastName.trim(),
      whatsapp: data.whatsapp?.trim() || null,
      interests: data.interests?.trim() || null
    });
  };

  const onSubmitPassword = (data) => {
    changePassword.mutate(
      {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword
      },
      {
        onSuccess: () => {
          resetPassword();
          setIsPasswordModalOpen(false);
        }
      }
    );
  };

  const handleClosePasswordModal = () => {
    setIsPasswordModalOpen(false);
    resetPassword();
  };

  return (
    <div className="space-y-6">
      <Card title="Informations personnelles">
        <div className="mb-6 flex items-center space-x-4">
          <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary-600 text-2xl font-bold text-white">
            {getInitials(user?.firstName, user?.lastName)}
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              {user?.firstName} {user?.lastName}
            </h3>
            <p className="text-sm text-gray-500">{user?.email}</p>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input label="Prenom" required {...register('firstName')} error={errors.firstName?.message} />
            <Input label="Nom" required {...register('lastName')} error={errors.lastName?.message} />
          </div>

          <Input label="Email" type="email" value={user?.email || ''} disabled />
          <Input label="WhatsApp" {...register('whatsapp')} error={errors.whatsapp?.message} />
          <Input label="Centres d'interet" {...register('interests')} error={errors.interests?.message} />

          <div className="flex justify-end">
            <Button type="submit" disabled={updateProfile.isPending}>
              {updateProfile.isPending ? 'Enregistrement...' : 'Enregistrer les modifications'}
            </Button>
          </div>
        </form>
      </Card>

      <Card title="Securite">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm text-gray-600">
            Le changement de mot de passe se fait dans une fenetre dediee pour plus de securite.
          </p>
          <Button
            type="button"
            onClick={() => setIsPasswordModalOpen(true)}
            disabled={changePassword.isPending}
          >
            Changer le mot de passe
          </Button>
        </div>
      </Card>

      <ChangePasswordModal
        isOpen={isPasswordModalOpen}
        onClose={handleClosePasswordModal}
        onSubmit={handleSubmitPassword(onSubmitPassword)}
        registerPassword={registerPassword}
        passwordErrors={passwordErrors}
        isPending={changePassword.isPending}
      />
    </div>
  );
};

export default ProfileContent;
