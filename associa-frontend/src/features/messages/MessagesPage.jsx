import { useState } from 'react';
import { ChatBubbleLeftRightIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Spinner } from '@components/common/feedback/Spinner';
import { usePermissions } from '@hooks/usePermissions';
import { useConversations } from '@hooks/useMessages';
import { formatRelativeTime } from '@utils/formatters';
import { getInitials } from '@utils/helpers';
import ConversationView from './ConversationView';

const MessagesPage = () => {
  const { data: conversations, isLoading } = useConversations();
  const { can } = usePermissions();
  const canSendMessages = can('messages.send');

  const [selectedUser, setSelectedUser] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  if (isLoading) return <Spinner size="lg" />;

  const conversationsList = conversations?.data || [];

  const filteredConversations = conversationsList.filter((conv) =>
    conv.user?.name?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-6">
      <div className="w-80 flex-shrink-0">
        <Card className="h-full">
          <div className="flex items-center justify-between border-b p-4">
            <h2 className="text-lg font-bold text-gray-900">Messages</h2>
            <ChatBubbleLeftRightIcon className="h-6 w-6 text-gray-400" />
          </div>

          <div className="border-b p-4">
            <div className="relative">
              <MagnifyingGlassIcon className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Rechercher..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full rounded-lg border border-gray-300 py-2 pl-10 pr-4 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>
          </div>

          <div className="overflow-y-auto" style={{ maxHeight: 'calc(100vh - 20rem)' }}>
            {filteredConversations.length === 0 ? (
              <div className="p-8 text-center text-gray-500">
                <ChatBubbleLeftRightIcon className="mx-auto mb-2 h-12 w-12 text-gray-300" />
                <p>Aucune conversation</p>
              </div>
            ) : (
              filteredConversations.map((conv) => (
                <button
                  key={conv.user.id}
                  onClick={() => setSelectedUser(conv.user)}
                  className={`w-full border-b p-4 text-left transition hover:bg-gray-50 ${
                    selectedUser?.id === conv.user.id ? 'bg-primary-50' : ''
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-primary-600 font-semibold text-white">
                      {getInitials(conv.user.name)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center justify-between">
                        <p className="truncate font-medium text-gray-900">{conv.user.name}</p>
                        {conv.unread_count > 0 ? (
                          <span className="ml-2 flex h-5 w-5 items-center justify-center rounded-full bg-primary-600 text-xs text-white">
                            {conv.unread_count}
                          </span>
                        ) : null}
                      </div>
                      <p className="truncate text-sm text-gray-500">{conv.last_message.content}</p>
                      <p className="mt-1 text-xs text-gray-400">{formatRelativeTime(conv.last_message.created_at)}</p>
                    </div>
                  </div>
                </button>
              ))
            )}
          </div>
        </Card>
      </div>

      <div className="flex-1">
        {selectedUser ? (
          <ConversationView user={selectedUser} canSendMessages={canSendMessages} />
        ) : (
          <Card className="flex h-full items-center justify-center">
            <div className="text-center text-gray-500">
              <ChatBubbleLeftRightIcon className="mx-auto mb-4 h-16 w-16 text-gray-300" />
              <p className="text-lg">Sélectionnez une conversation</p>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default MessagesPage;
