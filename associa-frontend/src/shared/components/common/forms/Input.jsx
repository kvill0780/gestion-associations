import { forwardRef } from 'react';
import { cn } from '@utils/helpers';

export const Input = forwardRef(
  ({ label, error, className, required = false, showRequiredMark, ...props }, ref) => {
    const shouldShowRequiredMark = showRequiredMark ?? required;

    return (
      <div className="w-full">
        {label && (
          <label className="mb-1.5 block text-sm font-semibold text-slate-700">
            {label}
            {shouldShowRequiredMark ? <span className="ml-1 text-red-500">*</span> : null}
          </label>
        )}
        <input
          ref={ref}
          required={required}
          className={cn(
            'w-full rounded-xl border bg-white px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2',
            error
              ? 'border-red-300 focus:border-red-500 focus:ring-red-200'
              : 'border-slate-200 focus:border-primary-500 focus:ring-primary-200',
            className
          )}
          {...props}
        />
        {error ? <p className="mt-1 text-sm font-medium text-red-600">{error}</p> : null}
      </div>
    );
  }
);

Input.displayName = 'Input';
