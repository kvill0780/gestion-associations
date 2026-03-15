import { useQuery } from '@tanstack/react-query';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { votesService } from '@api/services/votes.service';

const VoteResultsModal = ({ isOpen, onClose, voteId }) => {
  const { data: results, isLoading } = useQuery({
    queryKey: ['vote-results', voteId],
    queryFn: () => votesService.getResults(voteId),
    enabled: isOpen && !!voteId
  });

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Résultats du vote">
      {isLoading ? (
        <Spinner />
      ) : (
        <div className="space-y-6">
          <div className="grid grid-cols-3 gap-4 p-4 bg-gray-50 rounded-lg">
            <div>
              <p className="text-sm text-gray-600">Participation</p>
              <p className="text-2xl font-bold text-gray-900">
                {results?.participation_rate}%
              </p>
              <p className="text-xs text-gray-500">
                {results?.total_votes}/{results?.total_members} membres
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Quorum</p>
              <p className="text-2xl font-bold text-gray-900">
                {results?.vote.quorum}%
              </p>
              <p className={`text-xs ${results?.quorum_reached ? 'text-green-600' : 'text-red-600'}`}>
                {results?.quorum_reached ? 'Atteint' : 'Non atteint'}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Majorité requise</p>
              <p className="text-2xl font-bold text-gray-900">
                {results?.vote.majority}%
              </p>
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="font-semibold text-gray-900">Résultats par option</h4>
            {results?.options?.map(option => (
              <div key={option.id} className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="font-medium text-gray-900">{option.text}</span>
                  <span className="text-gray-600">
                    {option.votes} votes ({option.percentage}%)
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-primary-600 h-2 rounded-full transition-all"
                    style={{ width: `${option.percentage}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </Modal>
  );
};

export default VoteResultsModal;
