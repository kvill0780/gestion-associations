import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PlusIcon, CheckCircleIcon, ClockIcon } from '@heroicons/react/24/outline';
import { Button } from '@components/common/forms/Button';
import { Card } from '@components/common/data/Card';
import { usePermissions } from '@hooks/usePermissions';
import { votesService } from '@api/services/votes.service';
import CreateVoteModal from './CreateVoteModal';
import VoteCard from './VoteCard';

const VotesPage = () => {
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [filter, setFilter] = useState('all');
  const { can } = usePermissions();

  const canCreateVotes = can('votes.create');
  const canManageVotes = can('votes.manage');
  const canCastVotes = can('votes.cast');

  const { data: votes, isLoading } = useQuery({
    queryKey: ['votes'],
    queryFn: votesService.getAll
  });

  const filteredVotes = votes?.filter((vote) => {
    if (filter === 'all') return true;
    return vote.status === filter;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Votes & Décisions</h2>
          <p className="text-gray-600">Gérer les votes et décisions importantes</p>
        </div>
        {canCreateVotes ? (
          <Button onClick={() => setCreateModalOpen(true)}>
            <PlusIcon className="mr-2 h-5 w-5" />
            Nouveau vote
          </Button>
        ) : null}
      </div>

      <div className="flex gap-4">
        <Button variant={filter === 'all' ? 'primary' : 'secondary'} onClick={() => setFilter('all')}>
          Tous
        </Button>
        <Button
          variant={filter === 'active' ? 'primary' : 'secondary'}
          onClick={() => setFilter('active')}
        >
          <ClockIcon className="mr-2 h-5 w-5" />
          Actifs
        </Button>
        <Button
          variant={filter === 'closed' ? 'primary' : 'secondary'}
          onClick={() => setFilter('closed')}
        >
          <CheckCircleIcon className="mr-2 h-5 w-5" />
          Terminés
        </Button>
      </div>

      {isLoading ? (
        <div>Chargement...</div>
      ) : filteredVotes?.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-gray-500">Aucun vote disponible</p>
        </Card>
      ) : (
        <div className="grid gap-6">
          {filteredVotes?.map((vote) => (
            <VoteCard
              key={vote.id}
              vote={vote}
              canCastVotes={canCastVotes}
              canManageVotes={canManageVotes}
            />
          ))}
        </div>
      )}

      <CreateVoteModal isOpen={createModalOpen} onClose={() => setCreateModalOpen(false)} />
    </div>
  );
};

export default VotesPage;
