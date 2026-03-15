import { cn } from '@utils/helpers';

export const RequiredLabel = ({ children, required = false, className, ...props }) => {
  return (
    <label className={cn('mb-1 block text-sm font-medium text-gray-700', className)} {...props}>
      {children}
      {required ? <span className="ml-1 text-red-500">*</span> : null}
    </label>
  );
};
