import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '@hooks/useAuth';
import { Input } from '@components/common/forms/Input';
import { Button } from '@components/common/forms/Button';

const loginSchema = z.object({
  email: z.string().email('Email invalide'),
  password: z.string().min(6, 'Minimum 6 caractères')
});

const LoginPage = () => {
  const { login, isLoggingIn } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(loginSchema)
  });

  const onSubmit = (data) => {
    login(data);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-lg">
        <h2 className="mb-6 text-center text-3xl font-bold text-gray-900">Connexion</h2>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Email"
            required
            type="email"
            error={errors.email?.message}
            {...register('email')}
          />

          <Input
            label="Mot de passe"
            required
            type="password"
            error={errors.password?.message}
            {...register('password')}
          />

          <Button type="submit" disabled={isLoggingIn} className="w-full">
            {isLoggingIn ? 'Connexion...' : 'Se connecter'}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
