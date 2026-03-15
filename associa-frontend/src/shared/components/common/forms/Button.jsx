import { cn } from '@utils/helpers';

export const Button = ({ children, variant = 'primary', size = 'md', className, disabled, ...props }) => {
  const variants = {
    primary:
      'bg-primary-600 text-white shadow-sm hover:bg-primary-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-200',
    secondary:
      'border border-slate-200 bg-white text-slate-700 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-200',
    danger:
      'bg-red-600 text-white shadow-sm hover:bg-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-200',
    success:
      'bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-200'
  };
  const sizes = {
    sm: 'px-3 py-2 text-xs',
    md: 'px-4 py-2.5 text-sm',
    lg: 'px-5 py-3 text-base'
  };

  return (
    <button
      className={cn(
        'rounded-xl font-semibold transition disabled:cursor-not-allowed disabled:opacity-50',
        variants[variant],
        sizes[size] || sizes.md,
        className
      )}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
};
