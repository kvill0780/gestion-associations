import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '@api/services/dashboard.service';

export const useDashboard = () => {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardService.getDashboard
  });
};
