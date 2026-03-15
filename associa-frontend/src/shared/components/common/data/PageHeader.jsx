import { cn } from '@utils/helpers';

export const PageHeader = ({ title, subtitle, icon, actions, className }) => {
  return (
    <header
      className={cn(
        'flex flex-col gap-4 border-b border-slate-200 pb-4 sm:flex-row sm:items-start sm:justify-between',
        className
      )}
    >
      <div className="min-w-0">
        <div className="flex items-center gap-3">
          {icon && (
            <span className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-slate-100 text-xl">
              {icon}
            </span>
          )}
          <h2 className="truncate text-2xl font-extrabold tracking-tight text-slate-900">{title}</h2>
        </div>
        {subtitle && <p className="mt-1 text-sm font-medium text-slate-500">{subtitle}</p>}
      </div>

      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </header>
  );
};
