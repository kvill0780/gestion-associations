import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export const FinanceChart = ({ data, type = 'line' }) => {
  const chartData = Array.isArray(data)
    ? data
      .map((point, index) => ({
        month: point?.month || point?.label || point?.period || point?.name || `P${index + 1}`,
        income: Number(point?.income ?? point?.monthlyIncome ?? point?.credit ?? 0),
        expense: Number(point?.expense ?? point?.monthlyExpenses ?? point?.debit ?? 0)
      }))
      .filter((point) => Number.isFinite(point.income) && Number.isFinite(point.expense))
    : [];

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload?.length) {
      return (
        <div className="rounded-lg bg-white p-3 shadow-lg">
          <p className="text-sm font-medium text-gray-900">{payload[0].payload.month}</p>
          {payload.map((entry, index) => (
            <p key={index} className="text-sm" style={{ color: entry.color }}>
              {entry.name}: {entry.value.toLocaleString()} FCFA
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  if (chartData.length < 2) {
    return (
      <div className="flex h-[300px] items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 text-center">
        <div>
          <p className="text-sm font-semibold text-slate-700">Pas assez d&apos;historique</p>
          <p className="mt-1 text-xs text-slate-500">
            Les donnees de serie financiere ne sont pas encore disponibles.
          </p>
        </div>
      </div>
    );
  }

  if (type === 'bar') {
    return (
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" />
          <YAxis />
          <Tooltip content={<CustomTooltip />} />
          <Legend />
          <Bar dataKey="income" fill="#10b981" name="Recettes" />
          <Bar dataKey="expense" fill="#ef4444" name="Dépenses" />
        </BarChart>
      </ResponsiveContainer>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" />
        <YAxis />
        <Tooltip content={<CustomTooltip />} />
        <Legend />
        <Line type="monotone" dataKey="income" stroke="#10b981" strokeWidth={2} name="Recettes" />
        <Line type="monotone" dataKey="expense" stroke="#ef4444" strokeWidth={2} name="Dépenses" />
      </LineChart>
    </ResponsiveContainer>
  );
};
