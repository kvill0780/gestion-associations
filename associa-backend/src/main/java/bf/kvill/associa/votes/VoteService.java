package bf.kvill.associa.votes;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.exception.BusinessException;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.votes.dto.CreateVoteRequest;
import bf.kvill.associa.votes.dto.VoteOptionResponse;
import bf.kvill.associa.votes.dto.VoteResponse;
import bf.kvill.associa.votes.dto.VoteResultsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteOptionRepository optionRepository;
    private final VoteBallotRepository ballotRepository;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<VoteResponse> getVotes(Long associationId, Long userId) {
        List<Vote> votes = voteRepository.findByAssociationIdOrderByCreatedAtDesc(associationId);
        List<VoteResponse> responses = new ArrayList<>();
        for (Vote vote : votes) {
            responses.add(toResponse(vote, userId));
        }
        return responses;
    }

    @Transactional
    public VoteResponse createVote(CreateVoteRequest request, Long associationId, Long userId) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        verifyActorInAssociation(user, associationId);

        Vote vote = Vote.builder()
                .association(association)
                .createdBy(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .quorum(request.getQuorum())
                .majority(request.getMajority())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .status(VoteStatus.DRAFT)
                .build();

        if (request.getOptions() == null || request.getOptions().size() < 2) {
            throw new BusinessException("Au moins deux options sont requises");
        }

        for (String option : request.getOptions()) {
            if (option == null || option.trim().isEmpty()) {
                continue;
            }
            vote.getOptions().add(VoteOption.builder()
                    .vote(vote)
                    .optionText(option.trim())
                    .build());
        }

        Vote saved = voteRepository.save(vote);

        auditService.log(
                "CREATE_VOTE",
                "Vote",
                saved.getId(),
                user,
                Map.of("title", saved.getTitle()));

        return toResponse(saved, userId);
    }

    @Transactional
    public void publishVote(Long voteId, Long associationId, Long userId) {
        Vote vote = findByIdAndAssociation(voteId, associationId);
        if (vote.getStatus() != VoteStatus.DRAFT) {
            return;
        }
        vote.setStatus(VoteStatus.ACTIVE);
        vote.setPublishedAt(LocalDateTime.now());
        voteRepository.save(vote);

        auditService.log("PUBLISH_VOTE", "Vote", voteId, userId, Map.of("title", vote.getTitle()));
    }

    @Transactional
    public void closeVote(Long voteId, Long associationId, Long userId) {
        Vote vote = findByIdAndAssociation(voteId, associationId);
        if (vote.getStatus() == VoteStatus.CLOSED) {
            return;
        }
        vote.setStatus(VoteStatus.CLOSED);
        vote.setClosedAt(LocalDateTime.now());
        voteRepository.save(vote);

        auditService.log("CLOSE_VOTE", "Vote", voteId, userId, Map.of("title", vote.getTitle()));
    }

    @Transactional
    public void castVote(Long voteId, Long associationId, Long userId, Long optionId) {
        Vote vote = findByIdAndAssociation(voteId, associationId);
        if (vote.getStatus() != VoteStatus.ACTIVE) {
            throw new BusinessException("Le vote n'est pas actif");
        }

        LocalDateTime now = LocalDateTime.now();
        if (vote.getStartDate() != null && now.isBefore(vote.getStartDate())) {
            throw new BusinessException("Le vote n'a pas encore démarré");
        }
        if (vote.getEndDate() != null && now.isAfter(vote.getEndDate())) {
            throw new BusinessException("Le vote est terminé");
        }

        VoteOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("VoteOption", optionId));

        if (!option.getVote().getId().equals(voteId)) {
            throw new BusinessException("Option invalide pour ce vote");
        }

        if (vote.getType() == VoteType.SIMPLE && ballotRepository.existsByVoteIdAndUserId(voteId, userId)) {
            throw new BusinessException("Vous avez déjà voté");
        }

        if (vote.getType() == VoteType.MULTIPLE
                && ballotRepository.existsByVoteIdAndUserIdAndOptionId(voteId, userId, optionId)) {
            throw new BusinessException("Vote déjà enregistré pour cette option");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        verifyActorInAssociation(user, associationId);

        VoteBallot ballot = VoteBallot.builder()
                .vote(vote)
                .option(option)
                .user(user)
                .build();
        ballotRepository.save(ballot);
    }

    @Transactional(readOnly = true)
    public VoteResultsResponse getResults(Long voteId, Long associationId) {
        Vote vote = findByIdAndAssociation(voteId, associationId);
        long totalVotes = ballotRepository.countByVoteId(voteId);
        long totalMembers = userRepository.countByAssociationIdAndMembershipStatus(
                associationId,
                MembershipStatus.ACTIVE);

        double participationRate = totalMembers > 0
                ? Math.round((totalVotes * 100.0 / totalMembers) * 100.0) / 100.0
                : 0.0;

        boolean quorumReached = participationRate >= (vote.getQuorum() != null ? vote.getQuorum() : 0);

        List<VoteResultsResponse.OptionResult> optionResults = optionRepository.findByVoteId(voteId)
                .stream()
                .map(option -> {
                    long votes = ballotRepository.countByOptionId(option.getId());
                    double percentage = totalVotes > 0
                            ? Math.round((votes * 100.0 / totalVotes) * 100.0) / 100.0
                            : 0.0;
                    return VoteResultsResponse.OptionResult.builder()
                            .id(option.getId())
                            .text(option.getOptionText())
                            .votes(votes)
                            .percentage(percentage)
                            .build();
                })
                .toList();

        return VoteResultsResponse.builder()
                .participationRate(participationRate)
                .totalVotes(totalVotes)
                .totalMembers(totalMembers)
                .quorumReached(quorumReached)
                .vote(VoteResultsResponse.VoteSummary.builder()
                        .quorum(vote.getQuorum())
                        .majority(vote.getMajority())
                        .build())
                .options(optionResults)
                .build();
    }

    @Transactional(readOnly = true)
    public Vote findByIdAndAssociation(Long voteId, Long associationId) {
        return voteRepository.findByIdAndAssociationId(voteId, associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vote", voteId));
    }

    private VoteResponse toResponse(Vote vote, Long userId) {
        List<VoteOptionResponse> options = vote.getOptions()
                .stream()
                .map(option -> VoteOptionResponse.builder()
                        .id(option.getId())
                        .optionText(option.getOptionText())
                        .build())
                .toList();

        return VoteResponse.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .description(vote.getDescription())
                .status(vote.getStatus())
                .quorum(vote.getQuorum())
                .majority(vote.getMajority())
                .startDate(vote.getStartDate())
                .endDate(vote.getEndDate())
                .type(vote.getType())
                .totalVotes(ballotRepository.countByVoteId(vote.getId()))
                .userHasVoted(ballotRepository.existsByVoteIdAndUserId(vote.getId(), userId))
                .options(options)
                .build();
    }

    private void verifyActorInAssociation(User actor, Long associationId) {
        if (actor.isSuperAdmin()) {
            return;
        }
        Long actorAssociationId = actor.getAssociation() != null ? actor.getAssociation().getId() : null;
        if (actorAssociationId == null || !actorAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }
}
