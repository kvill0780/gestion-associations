import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { contributionsService } from '@api/services/contributions.service';
import toast from 'react-hot-toast';

export const useContributions = (filters = {}) => {
  return useQuery({
    queryKey: ['contributions', filters],
    queryFn: () => contributionsService.getAll(filters),
    enabled: Boolean(filters?.year)
  });
};

export const useContributionStats = (filters = {}) => {
  return useQuery({
    queryKey: ['contributionStats', filters],
    queryFn: () => contributionsService.getStats(filters),
    enabled: Boolean(filters?.year)
  });
};

export const useCreateContribution = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: contributionsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contributions'] });
      queryClient.invalidateQueries({ queryKey: ['contributionStats'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Cotisation créée');
    },
    onError: () => {
      toast.error('Erreur lors de la création');
    }
  });
};

export const useGenerateContributions = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: contributionsService.generate,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contributions'] });
      queryClient.invalidateQueries({ queryKey: ['contributionStats'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Cotisations générées');
    },
    onError: () => {
      toast.error('Erreur lors de la génération');
    }
  });
};

export const useRecordContributionPayment = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ contributionId, payload }) =>
      contributionsService.recordPayment({ contributionId, payload }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contributions'] });
      queryClient.invalidateQueries({ queryKey: ['contributionStats'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Paiement enregistré');
    },
    onError: () => {
      toast.error('Erreur lors de l\'enregistrement');
    }
  });
};
