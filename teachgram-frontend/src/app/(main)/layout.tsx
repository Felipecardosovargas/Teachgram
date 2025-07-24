import MainLayout from '@/components/common/MainLayout';
import ProtectedRoute from '@/components/common/ProtectedRoute';

export default function MainRootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ProtectedRoute>
      <MainLayout>
        {children}
      </MainLayout>
    </ProtectedRoute>
  );
}