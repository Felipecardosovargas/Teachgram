import React from 'react';

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = 'md', className = '' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4 border-2',
    md: 'w-6 h-6 border-3',
    lg: 'w-8 h-8 border-4',
  };

  return (
    <div
      className={`${sizeClasses[size]} border-t-blue-500 border-gray-200 rounded-full animate-spin ${className}`}
      role="status"
    >
      <span className="sr-only">Carregando...</span>
    </div>
  );
};

export default Spinner;