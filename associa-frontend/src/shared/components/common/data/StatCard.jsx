import { cn } from '@utils/helpers';

export const StatCard = ({ title, value, icon: Icon, trend, color = 'blue' }) => {
  const colors = {
    blue: 'bg-primary-100 text-primary-700',
    green: 'bg-emerald-100 text-emerald-700',
    yellow: 'bg-amber-100 text-amber-700',
    purple: 'bg-indigo-100 text-indigo-700',
    red: 'bg-red-100 text-red-700'
  };

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <p className="text-sm font-semibold text-slate-500">{title}</p>
          <p className="mt-2 text-3xl font-extrabold tracking-tight text-slate-900">{value}</p>
          {trend && (
            <p className={cn('mt-2 text-sm font-semibold', trend > 0 ? 'text-emerald-600' : 'text-red-600')}>
              {trend > 0 ? '↑' : '↓'} {Math.abs(trend)}%
            </p>
          )}
        </div>
        {Icon && (
          <div className={cn('rounded-xl p-3', colors[color])}>
            <Icon className="h-8 w-8" />
          </div>
        )}
      </div>
    </div>
  );
};
