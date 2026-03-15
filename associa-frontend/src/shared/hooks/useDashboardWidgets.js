import { useMemo } from 'react';
import { FEATURES } from '@config/app.config';
import { usePermissions } from '@hooks/usePermissions';
import { DASHBOARD_WIDGETS, DASHBOARD_WIDGET_LIMITS } from '@features/dashboard/widgets.config';

const DEFAULT_ZONE = 'core';

const getZoneLimit = (zone) => {
  if (!DASHBOARD_WIDGET_LIMITS || DASHBOARD_WIDGET_LIMITS[zone] === undefined) {
    return Number.POSITIVE_INFINITY;
  }
  return DASHBOARD_WIDGET_LIMITS[zone];
};

export const useDashboardWidgets = () => {
  const { canAny, canAll } = usePermissions();

  return useMemo(() => {
    const allowedWidgets = DASHBOARD_WIDGETS.filter((widget) => {
      if (widget.feature && !FEATURES[widget.feature]) {
        return false;
      }

      if (!widget.permissions || widget.permissions.length === 0) {
        return true;
      }

      if (widget.requireAll) {
        return canAll(widget.permissions);
      }

      return canAny(widget.permissions);
    });

    const sortedWidgets = [...allowedWidgets].sort((a, b) => a.priority - b.priority);
    const zones = {
      hero: [],
      core: [],
      secondary: []
    };

    sortedWidgets.forEach((widget) => {
      const preferred = widget.zones?.preferred || DEFAULT_ZONE;
      const fallbacks = widget.zones?.fallback || [];
      const candidates = [preferred, ...fallbacks, 'secondary'];

      for (const zone of candidates) {
        if (!zones[zone]) continue;
        if (zones[zone].length < getZoneLimit(zone)) {
          zones[zone].push(widget);
          return;
        }
      }

      zones.secondary.push(widget);
    });

    return {
      all: allowedWidgets,
      hero: zones.hero,
      core: zones.core,
      secondary: zones.secondary,
      totalCount: allowedWidgets.length
    };
  }, [canAny, canAll]);
};
