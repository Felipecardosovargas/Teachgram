'use client';

import React, { createContext, useContext, useCallback } from 'react';
import { Toaster, toast as hotToast } from 'react-hot-toast'; 

type ToastVariant = 'default' | 'destructive' | 'success' | 'info'; 

interface ToastProps {
  title: string;
  description?: string;
  variant?: ToastVariant;
  id?: string; 
}

const CustomToastContent: React.FC<ToastProps> = ({ title, description, variant }) => {
  let bgColor = 'bg-white';
  let textColor = 'text-gray-900';
  let borderColor = 'border-gray-200';

  switch (variant) {
    case 'destructive':
      bgColor = 'bg-red-500';
      textColor = 'text-white';
      borderColor = 'border-red-600';
      break;
    case 'success':
      bgColor = 'bg-green-500';
      textColor = 'text-white';
      borderColor = 'border-green-600';
      break;
    case 'info':
      bgColor = 'bg-blue-500';
      textColor = 'text-white';
      borderColor = 'border-blue-600';
      break;
    default:
      break;
  }

  return (
    <div className={`p-4 rounded-lg shadow-lg border ${bgColor} ${borderColor} flex items-start space-x-3`}>
      <div>
        <h3 className={`font-semibold ${textColor}`}>{title}</h3>
        {description && <p className={`text-sm ${textColor} opacity-90 mt-1`}>{description}</p>}
      </div>
    </div>
  );
};

interface UseToastReturn {
  toast: (props: ToastProps) => string; 
  dismiss: (toastId?: string) => void; 
}

const ToastContext = createContext<UseToastReturn | undefined>(undefined);

interface ToastProviderProps {
  children: React.ReactNode;
}

export default function ToastProvider({ children }: ToastProviderProps) {
  const showToast = useCallback((props: ToastProps) => {
    return hotToast.custom((t) => (
      <CustomToastContent {...props} id={t.id} />
    ), {
      duration: 4000,
      position: 'top-right',
    });
  }, []);

  const dismissToast = useCallback((toastId?: string) => {
    if (toastId) {
      hotToast.dismiss(toastId);
    } else {
      hotToast.dismiss();
    }
  }, []);

  const contextValue: UseToastReturn = {
    toast: showToast,
    dismiss: dismissToast,
  };

  return (
    <ToastContext.Provider value={contextValue}>
      {children}
      <Toaster />
    </ToastContext.Provider>
  );
}

export function useToast(): UseToastReturn {
  const context = useContext(ToastContext);

  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }

  return context;
}
