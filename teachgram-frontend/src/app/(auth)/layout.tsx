import AuthLayout from '@/components/common/AuthLayout';

export default function AuthRootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthLayout>
      {children}
    </AuthLayout>
  );
}