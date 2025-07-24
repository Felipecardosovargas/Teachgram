import { ErrorBoundary } from "react-error-boundary";

export function AppErrorBoundary({ children }: { children: React.ReactNode }) {
  return (
    <ErrorBoundary
      fallback={<div>Something went wrong. Please try again.</div>}
    >
      {children}
    </ErrorBoundary>
  );
}