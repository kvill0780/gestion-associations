import { useEffect, useMemo, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { PaperAirplaneIcon } from '@heroicons/react/24/solid';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { Spinner } from '@components/common/feedback/Spinner';
import { useConversation, useSendMessage } from '@hooks/useMessages';
import { useAuthStore } from '@store/authStore';
import { formatDateTime } from '@utils/formatters';
import { getInitials } from '@utils/helpers';

const ConversationView = ({ user, canSendMessages = false }) => {
  const { data: conversation, isLoading } = useConversation(user.id);
  const sendMessage = useSendMessage();
  const currentUser = useAuthStore((state) => state.user);
  const messagesEndRef = useRef(null);
  const { register, handleSubmit, reset } = useForm();

  const messages = useMemo(() => conversation?.data || [], [conversation?.data]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const onSubmit = (data) => {
    if (!canSendMessages || !data.content.trim()) return;

    sendMessage.mutate(
      { receiverId: user.id, content: data.content },
      { onSuccess: () => reset() }
    );
  };

  if (isLoading || !currentUser) return <Spinner size="lg" />;

  return (
    <Card className="flex h-full flex-col">
      <div className="flex items-center gap-3 border-b p-4">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-600 font-semibold text-white">
          {getInitials(user.name)}
        </div>
        <div>
          <p className="font-medium text-gray-900">{user.name}</p>
          <p className="text-sm text-gray-500">{user.email}</p>
        </div>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto p-4" style={{ maxHeight: 'calc(100vh - 24rem)' }}>
        {messages.length === 0 ? (
          <div className="flex h-full items-center justify-center text-gray-500">
            <p>Aucun message. Commencez la conversation !</p>
          </div>
        ) : (
          messages.map((message) => {
            const isCurrentUser = message.sender_id === currentUser.id;
            return (
              <div key={message.id} className={`flex ${isCurrentUser ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-xs lg:max-w-md ${isCurrentUser ? 'order-2' : 'order-1'}`}>
                  <div
                    className={`rounded-lg px-4 py-2 ${
                      isCurrentUser ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-900'
                    }`}
                  >
                    <p className="break-words">{message.content}</p>
                  </div>
                  <p className={`mt-1 text-xs text-gray-500 ${isCurrentUser ? 'text-right' : 'text-left'}`}>
                    {formatDateTime(message.created_at)}
                  </p>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {canSendMessages ? (
        <form onSubmit={handleSubmit(onSubmit)} className="border-t p-4">
          <div className="flex gap-2">
            <input
              type="text"
              placeholder="Écrivez votre message..."
              {...register('content')}
              className="flex-1 rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <Button type="submit" disabled={sendMessage.isPending}>
              <PaperAirplaneIcon className="h-5 w-5" />
            </Button>
          </div>
        </form>
      ) : (
        <div className="border-t p-4 text-sm text-gray-500">
          Vous n&apos;avez pas la permission d&apos;envoyer des messages.
        </div>
      )}
    </Card>
  );
};

export default ConversationView;
