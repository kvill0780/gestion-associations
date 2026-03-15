import { cn } from '@utils/helpers';
import { Badge } from './Badge';

export const Card = ({ title, children, actions, badge, className }) => {
  return (
    <div className={cn('rounded-2xl border border-slate-200 bg-white p-6 shadow-sm', className)}>
      {(title || actions) && (
        <div className="mb-5 flex items-center justify-between">
          {title && (
            <div className="flex items-center gap-2">
              <h3 className="text-lg font-bold text-slate-900">{title}</h3>
              {badge !== undefined && badge !== null && (
                <Badge variant="info" className="text-xs">
                  {badge}
                </Badge>
              )}
            </div>
          )}
          {actions && <div>{actions}</div>}
        </div>
      )}
      {children}
    </div>
  );
};
