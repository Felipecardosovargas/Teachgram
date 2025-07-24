import SignupForm from '@/components/auth/SignupForm';

export const metadata = {
  title: 'Cadastro - TeachGram',
  description: 'Crie sua conta no TeachGram.',
};

export default function SignupPage() {
  return (
    <div className="mt-8">
      <SignupForm />
    </div>
  );
}