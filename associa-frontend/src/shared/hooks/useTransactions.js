import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { transactionsService } from '@api/services/transactions.service';
import toast from 'react-hot-toast';

export const useTransactions = () => {
  return useQuery({
    queryKey: ['transactions'],
    queryFn: transactionsService.getAll
  });
};

export const useCreateTransaction = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: transactionsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Transaction créée');
    },
    onError: () => {
      toast.error('Erreur lors de la création');
    }
  });
};

export const useApproveTransaction = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: transactionsService.approve,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Transaction approuvée');
    },
    onError: () => {
      toast.error('Erreur lors de l\'approbation');
    }
  });
};
