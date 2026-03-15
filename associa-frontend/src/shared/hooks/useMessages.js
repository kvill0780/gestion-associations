import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { messagesService } from '@api/services/messages.service';
import { toast } from 'react-hot-toast';

export const useConversations = () => {
  return useQuery({
    queryKey: ['conversations'],
    queryFn: messagesService.getConversations
  });
};

export const useConversation = (userId) => {
  return useQuery({
    queryKey: ['conversation', userId],
    queryFn: () => messagesService.getConversation(userId),
    enabled: !!userId
  });
};

export const useSendMessage = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: messagesService.sendMessage,
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries(['conversations']);
      const userId = variables?.receiverId ?? variables?.receiver_id;
      if (userId) {
        queryClient.invalidateQueries(['conversation', userId]);
      } else {
        queryClient.invalidateQueries(['conversation']);
      }
    },
    onError: () => {
      toast.error('Erreur lors de l\'envoi du message');
    }
  });
};

export const useUnreadCount = () => {
  return useQuery({
    queryKey: ['unread-messages'],
    queryFn: messagesService.getUnreadCount,
    refetchInterval: 30000 // Refresh every 30 seconds
  });
};
