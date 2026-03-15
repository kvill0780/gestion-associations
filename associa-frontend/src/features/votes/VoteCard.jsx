import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { votesService } from '@api/services/votes.service';
import { formatDate } from '@utils/formatters';
import toast from 'react-hot-toast';
import VoteResultsModal from './VoteResultsModal';

const VoteCard = ({ vote, canCastVotes, canManageVotes }) => {
  const [selectedOption, setSelectedOption] = useState(null);
  const [showResults, setShowResults] = useState(false);
  const queryClient = useQueryClient();

  const castVoteMutation = useMutation({
    mutationFn: async (optionId) => votesService.cast(vote.id, optionId),
    onSuccess: () => {
      queryClient.invalidateQueries(['votes']);
      toast.success('Vote enregistré !');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Erreur');
    }
  });

  const publishMutation = useMutation({
    mutationFn: async () => votesService.publish(vote.id),
    onSuccess: () => {
      queryClient.invalidateQueries(['votes']);
      toast.success('Vote publié !');
    }
  });

  const closeMutation = useMutation({
    mutationFn: async () => votesService.close(vote.id),
    onSuccess: () => {
      queryClient.invalidateQueries(['votes']);
      toast.success('Vote clôturé !');
    }
  });

  const getStatusBadge = () => {
    const badges = {
      draft: 'bg-gray-100 text-gray-800',
      active: 'bg-green-100 text-green-800',
      closed: 'bg-red-100 text-red-800'
    };
    return badges[vote.status] || badges.draft;
  };

  return (
    <>
      <Card>
        <div className="p-6">
          <div className="mb-4 flex items-start justify-between">
            <div className="flex-1">
              <div className="mb-2 flex items-center gap-3">
                <h3 className="text-lg font-semibold text-gray-900">{vote.title}</h3>
                <span className={`rounded-full px-2 py-1 text-xs font-medium ${getStatusBadge()}`}>
                  {vote.status === 'draft' ? 'Brouillon' : vote.status === 'active' ? 'Actif' : 'Terminé'}
                </span>
              </div>
              {vote.description ? <p className="mb-4 text-gray-600">{vote.description}</p> : null}
              <div className="flex gap-4 text-sm text-gray-500">
                <span>Quorum: {vote.quorum}%</span>
                <span>Majorité: {vote.majority}%</span>
                <span>
                  Du {formatDate(vote.start_date)} au {formatDate(vote.end_date)}
                </span>
              </div>
              {vote.total_votes !== undefined ? (
                <p className="mt-2 text-sm text-gray-600">{vote.total_votes} vote(s) enregistré(s)</p>
              ) : null}
            </div>
          </div>

          {vote.status === 'active' && !vote.user_has_voted && canCastVotes ? (
            <div className="space-y-3">
              <p className="font-medium text-gray-900">Votre choix :</p>
              {vote.options?.map((option) => (
                <label
                  key={option.id}
                  className="flex cursor-pointer items-center gap-3 rounded-lg border p-3 hover:bg-gray-50"
                >
                  <input
                    type="radio"
                    name={`vote-${vote.id}`}
                    value={option.id}
                    checked={selectedOption === option.id}
                    onChange={() => setSelectedOption(option.id)}
                    className="h-4 w-4 text-primary-600"
                  />
                  <span>{option.option_text}</span>
                </label>
              ))}
              <Button
                onClick={() => castVoteMutation.mutate(selectedOption)}
                disabled={!selectedOption || castVoteMutation.isPending}
                className="w-full"
              >
                Voter
              </Button>
            </div>
          ) : null}

          {vote.user_has_voted && vote.status === 'active' ? (
            <div className="rounded-lg bg-green-50 p-4 text-green-800">
              Vous avez déjà voté pour cette décision
            </div>
          ) : null}

          <div className="mt-4 flex gap-3">
            {vote.status === 'draft' && canManageVotes ? (
              <Button onClick={() => publishMutation.mutate()} size="sm">
                Publier
              </Button>
            ) : null}
            {vote.status === 'active' && canManageVotes ? (
              <Button onClick={() => closeMutation.mutate()} variant="secondary" size="sm">
                Clôturer
              </Button>
            ) : null}
            <Button onClick={() => setShowResults(true)} variant="secondary" size="sm">
              Voir les résultats
            </Button>
          </div>
        </div>
      </Card>

      <VoteResultsModal isOpen={showResults} onClose={() => setShowResults(false)} voteId={vote.id} />
    </>
  );
};

export default VoteCard;
