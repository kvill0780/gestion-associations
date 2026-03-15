import { useState } from 'react';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/outline';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isSameDay, addMonths, subMonths } from 'date-fns';
import { fr } from 'date-fns/locale';

export const EventCalendar = ({ events = [] }) => {
  const [currentMonth, setCurrentMonth] = useState(new Date());

  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(currentMonth);
  const days = eachDayOfInterval({ start: monthStart, end: monthEnd });

  const getEventsForDay = (day) => {
    return events.filter((event) => isSameDay(new Date(event.start_date), day));
  };

  return (
    <div className="rounded-lg border bg-white">
      <div className="flex items-center justify-between border-b px-4 py-3">
        <button
          onClick={() => setCurrentMonth(subMonths(currentMonth, 1))}
          className="rounded-lg p-2 hover:bg-gray-100"
        >
          <ChevronLeftIcon className="h-5 w-5" />
        </button>
        <h3 className="text-lg font-semibold text-gray-900">
          {format(currentMonth, 'MMMM yyyy', { locale: fr })}
        </h3>
        <button
          onClick={() => setCurrentMonth(addMonths(currentMonth, 1))}
          className="rounded-lg p-2 hover:bg-gray-100"
        >
          <ChevronRightIcon className="h-5 w-5" />
        </button>
      </div>

      <div className="grid grid-cols-7 gap-px bg-gray-200">
        {['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'].map((day) => (
          <div key={day} className="bg-gray-50 px-2 py-2 text-center text-xs font-medium text-gray-700">
            {day}
          </div>
        ))}

        {days.map((day, idx) => {
          const dayEvents = getEventsForDay(day);
          const isToday = isSameDay(day, new Date());

          return (
            <div
              key={idx}
              className={`min-h-24 bg-white p-2 ${
                !isSameMonth(day, currentMonth) ? 'bg-gray-50 text-gray-400' : ''
              }`}
            >
              <div
                className={`mb-1 text-sm font-medium ${
                  isToday ? 'flex h-6 w-6 items-center justify-center rounded-full bg-primary-600 text-white' : ''
                }`}
              >
                {format(day, 'd')}
              </div>
              <div className="space-y-1">
                {dayEvents.slice(0, 2).map((event) => (
                  <div
                    key={event.id}
                    className="truncate rounded bg-blue-100 px-1 py-0.5 text-xs text-blue-700"
                    title={event.title}
                  >
                    {event.title}
                  </div>
                ))}
                {dayEvents.length > 2 && (
                  <div className="text-xs text-gray-500">+{dayEvents.length - 2}</div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
