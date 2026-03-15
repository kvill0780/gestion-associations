import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { documentsService } from '@api/services/documents.service';
import toast from 'react-hot-toast';

export const useDocuments = () => {
  return useQuery({
    queryKey: ['documents'],
    queryFn: documentsService.getAll
  });
};

export const useUploadDocument = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: documentsService.upload,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      toast.success('Document téléversé');
    },
    onError: () => {
      toast.error('Erreur lors du téléversement');
    }
  });
};

export const useDeleteDocument = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: documentsService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      toast.success('Document supprimé');
    },
    onError: () => {
      toast.error('Erreur lors de la suppression');
    }
  });
};
