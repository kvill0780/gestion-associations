import { XMarkIcon } from '@heroicons/react/24/outline';
import { cn } from '@utils/helpers';

export const Modal = ({ isOpen, onClose, title, children, size = 'md' }) => {
  if (!isOpen) return null;

  const sizes = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl'
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-center justify-center p-4">
        <div className="fixed inset-0 bg-slate-900/45 backdrop-blur-sm transition-opacity" onClick={onClose} />
        
        <div className={cn('relative w-full rounded-2xl border border-slate-200 bg-white shadow-2xl', sizes[size])}>
          <div className="flex items-center justify-between border-b border-slate-200 p-4">
            <h3 className="text-lg font-bold text-slate-900">{title}</h3>
            <button
              onClick={onClose}
              className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>
          <div className="p-6">{children}</div>
        </div>
      </div>
    </div>
  );
};
