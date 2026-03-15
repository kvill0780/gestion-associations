import { Badge } from '@components/common/data/Badge';
import { cn } from '@utils/helpers';

export const SectionHeader = ({
  title,
  subtitle,
  count,
  countVariant = 'default',
  actions,
  className
}) => {
  return (
    <div className={cn('mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between', className)}>
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <h3 className="truncate text-lg font-bold text-slate-900">{title}</h3>
          {count !== undefined && <Badge variant={countVariant}>{count}</Badge>}
        </div>
        {subtitle && <p className="mt-1 text-sm font-medium text-slate-500">{subtitle}</p>}
      </div>

      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
};
