import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const normalizeLower = (value) => {
  if (value === null || value === undefined) return value;
  return String(value).toLowerCase();
};

const toFrontendVoteOption = (option) => {
  if (!option) return option;
  return {
    id: option.id,
    option_text: option.optionText ?? option.option_text ?? option.text ?? option.label ?? ''
  };
};

const toFrontendVote = (vote) => {
  if (!vote) return vote;

  return {
    id: vote.id,
    title: vote.title,
    description: vote.description,
    status: normalizeLower(vote.status),
    quorum: vote.quorum,
    majority: vote.majority,
    start_date: vote.startDate ?? vote.start_date,
    end_date: vote.endDate ?? vote.end_date,
    type: normalizeLower(vote.type),
    total_votes: vote.totalVotes ?? vote.total_votes ?? vote.votesCount,
    user_has_voted: vote.userHasVoted ?? vote.user_has_voted ?? false,
    options: (vote.options ?? vote.voteOptions ?? []).map(toFrontendVoteOption)
  };
};

const toBackendVotePayload = (voteData = {}) => ({
  title: voteData.title,
  description: voteData.description,
  quorum: voteData.quorum,
  majority: voteData.majority,
  startDate: voteData.startDate ?? voteData.start_date,
  endDate: voteData.endDate ?? voteData.end_date,
  type: voteData.type ? String(voteData.type).toUpperCase() : undefined,
  options: voteData.options
});

const toFrontendResults = (results) => {
  if (!results) return results;
  const vote = results.vote || results;

  return {
    participation_rate: results.participationRate ?? results.participation_rate ?? 0,
    total_votes: results.totalVotes ?? results.total_votes ?? 0,
    total_members: results.totalMembers ?? results.total_members ?? 0,
    quorum_reached: results.quorumReached ?? results.quorum_reached ?? false,
    vote: {
      quorum: vote.quorum,
      majority: vote.majority
    },
    options: (results.options ?? []).map((option) => ({
      id: option.id,
      text: option.text ?? option.option_text ?? option.optionText ?? '',
      votes: option.votes ?? option.votes_count ?? 0,
      percentage: option.percentage ?? option.percent ?? 0
    }))
  };
};

const normalizeListResponse = (data) => {
  const payload = data?.data ?? data;
  const items = Array.isArray(payload) ? payload : [];
  return items.map(toFrontendVote);
};

export const votesService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.VOTES.BASE);
    return normalizeListResponse(data);
  },

  create: async (voteData) => {
    const payload = toBackendVotePayload(voteData);
    const { data } = await apiClient.post(API_ENDPOINTS.VOTES.BASE, payload);
    const response = data?.data ?? data;
    return toFrontendVote(response);
  },

  cast: async (voteId, optionId) => {
    await apiClient.post(API_ENDPOINTS.VOTES.CAST(voteId), { optionId });
    return { success: true };
  },

  publish: async (voteId) => {
    await apiClient.post(API_ENDPOINTS.VOTES.PUBLISH(voteId));
    return { success: true };
  },

  close: async (voteId) => {
    await apiClient.post(API_ENDPOINTS.VOTES.CLOSE(voteId));
    return { success: true };
  },

  getResults: async (voteId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.VOTES.RESULTS(voteId));
    const response = data?.data ?? data;
    return toFrontendResults(response);
  }
};
