import LoginForm from '@/components/auth/LoginForm';

export const metadata = {
  title: 'Login - TeachGram',
  description: 'Faça login na sua conta TeachGram.',
};

export default function LoginPage() {
  return (
    <div className="mt-8">
      <LoginForm />
    </div>
  );
}