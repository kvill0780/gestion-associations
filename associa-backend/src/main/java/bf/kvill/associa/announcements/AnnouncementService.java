package bf.kvill.associa.announcements;

import bf.kvill.associa.announcements.dto.AnnouncementPollOptionResponse;
import bf.kvill.associa.announcements.dto.AnnouncementRequest;
import bf.kvill.associa.announcements.dto.AnnouncementResponse;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.exception.BusinessException;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementPollOptionRepository optionRepository;
    private final AnnouncementReactionRepository reactionRepository;
    private final AnnouncementVoteRepository voteRepository;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncements(Long associationId, Long userId) {
        List<Announcement> announcements = announcementRepository.findByAssociationIdOrderByCreatedAtDesc(associationId);
        List<AnnouncementResponse> responses = new ArrayList<>();
        for (Announcement announcement : announcements) {
            responses.add(toResponse(announcement, userId));
        }
        return responses;
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, Long associationId, Long userId) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        verifyActorInAssociation(user, associationId);

        Announcement announcement = Announcement.builder()
                .association(association)
                .createdBy(user)
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .type(request.getType())
                .pollQuestion(request.getPollQuestion())
                .allowMultipleVotes(Boolean.TRUE.equals(request.getAllowMultipleVotes()))
                .build();

        if (announcement.getType() == AnnouncementType.POLL) {
            List<String> options = request.getPollOptions();
            if (options == null || options.size() < 2) {
                throw new BusinessException("Au moins deux options sont requises pour un sondage");
            }
            for (String optionText : options) {
                if (optionText == null || optionText.trim().isEmpty()) {
                    continue;
                }
                announcement.getPollOptions().add(AnnouncementPollOption.builder()
                        .announcement(announcement)
                        .optionText(optionText.trim())
                        .build());
            }
        }

        Announcement saved = announcementRepository.save(announcement);

        auditService.log(
                "CREATE_ANNOUNCEMENT",
                "Announcement",
                saved.getId(),
                user,
                Map.of("title", saved.getTitle(), "type", saved.getType().name()));

        return toResponse(saved, userId);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long associationId, Long userId) {
        Announcement announcement = findByIdAndAssociation(announcementId, associationId);
        announcementRepository.delete(announcement);

        auditService.log(
                "DELETE_ANNOUNCEMENT",
                "Announcement",
                announcementId,
                userId,
                Map.of("title", announcement.getTitle()));
    }

    @Transactional
    public void react(Long announcementId, Long associationId, Long userId, AnnouncementReactionType type) {
        Announcement announcement = findByIdAndAssociation(announcementId, associationId);

        reactionRepository.findByAnnouncementIdAndUserId(announcementId, userId)
                .ifPresent(reactionRepository::delete);

        AnnouncementReaction reaction = AnnouncementReaction.builder()
                .announcement(announcement)
                .user(userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", userId)))
                .type(type)
                .build();

        reactionRepository.save(reaction);
    }

    @Transactional
    public void unreact(Long announcementId, Long associationId, Long userId) {
        findByIdAndAssociation(announcementId, associationId);
        reactionRepository.deleteByAnnouncementIdAndUserId(announcementId, userId);
    }

    @Transactional
    public void vote(Long announcementId, Long associationId, Long userId, Long optionId) {
        Announcement announcement = findByIdAndAssociation(announcementId, associationId);
        if (announcement.getType() != AnnouncementType.POLL) {
            throw new BusinessException("Cette annonce n'est pas un sondage");
        }

        AnnouncementPollOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("PollOption", optionId));

        if (!option.getAnnouncement().getId().equals(announcementId)) {
            throw new BusinessException("Option invalide pour ce sondage");
        }

        long existingVotes = voteRepository.countByOption_Announcement_IdAndUserId(announcementId, userId);
        if (!Boolean.TRUE.equals(announcement.getAllowMultipleVotes()) && existingVotes > 0) {
            throw new BusinessException("Vous avez déjà voté pour ce sondage");
        }

        if (voteRepository.existsByOptionIdAndUserId(optionId, userId)) {
            throw new BusinessException("Vote déjà enregistré pour cette option");
        }

        AnnouncementVote vote = AnnouncementVote.builder()
                .option(option)
                .user(userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", userId)))
                .build();

        voteRepository.save(vote);
    }

    @Transactional(readOnly = true)
    public Announcement findByIdAndAssociation(Long announcementId, Long associationId) {
        return announcementRepository.findByIdAndAssociationId(announcementId, associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));
    }

    private AnnouncementResponse toResponse(Announcement announcement, Long userId) {
        long likes = reactionRepository.countByAnnouncementIdAndType(announcement.getId(), AnnouncementReactionType.LIKE);
        long dislikes = reactionRepository.countByAnnouncementIdAndType(announcement.getId(), AnnouncementReactionType.DISLIKE);

        String userReaction = reactionRepository.findByAnnouncementIdAndUserId(announcement.getId(), userId)
                .map(reaction -> reaction.getType().name().toLowerCase())
                .orElse(null);

        List<AnnouncementPollOptionResponse> options = null;
        if (announcement.getType() == AnnouncementType.POLL) {
            options = optionRepository.findByAnnouncementId(announcement.getId())
                    .stream()
                    .map(option -> AnnouncementPollOptionResponse.builder()
                            .id(option.getId())
                            .optionText(option.getOptionText())
                            .votesCount(voteRepository.countByOptionId(option.getId()))
                            .userVoted(voteRepository.existsByOptionIdAndUserId(option.getId(), userId))
                            .build())
                    .toList();
        }

        String authorName = announcement.getCreatedBy() != null
                ? announcement.getCreatedBy().getFullName()
                : null;

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .priority(announcement.getPriority())
                .type(announcement.getType())
                .pollQuestion(announcement.getPollQuestion())
                .allowMultipleVotes(announcement.getAllowMultipleVotes())
                .likesCount(likes)
                .dislikesCount(dislikes)
                .userReaction(userReaction)
                .authorName(authorName)
                .pollOptions(options)
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
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
