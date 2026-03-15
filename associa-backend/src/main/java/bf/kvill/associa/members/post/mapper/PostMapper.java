// ==================== modules/members/post/PostMapper.java ====================

package bf.kvill.associa.members.post.mapper;

import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.post.dto.*;
import bf.kvill.associa.members.role.Role;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Post ↔ DTOs
 */
@Component
public class PostMapper {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    // ========== Entity → Response DTO ==========

    public PostResponseDto toResponseDto(Post post) {
        if (post == null) {
            return null;
        }

        Long associationId = extractAssociationId(post);
        String associationName = extractAssociationName(post);

        Integer suggestedRolesCount = null;
        if (isRolesLoaded(post)) {
            suggestedRolesCount = post.getRoles() != null ? post.getRoles().size() : 0;
        }

        Role defaultRole = extractDefaultRole(post);

        return PostResponseDto.builder()
                .id(post.getId())
                .name(post.getName())
                .description(post.getDescription())
                .isExecutive(post.getIsExecutive())
                .maxOccupants(post.getMaxOccupants())
                .displayOrder(post.getDisplayOrder())
                .mandateDurationMonths(post.getMandateDurationMonths())
                .requiresElection(post.getRequiresElection())
                .isActive(post.getIsActive())
                .associationId(associationId)
                .associationName(associationName)
                .suggestedRolesCount(suggestedRolesCount)
                .defaultRoleId(defaultRole != null ? defaultRole.getId() : null)
                .defaultRoleName(defaultRole != null ? defaultRole.getName() : null)
                .defaultRoleSlug(defaultRole != null ? defaultRole.getSlug() : null)
                .canAcceptNewMandate(computeCanAcceptNewMandate(post))
                .fullDescription(post.getFullDescription())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // ========== Entity → Summary DTO ==========

    public PostSummaryDto toSummaryDto(Post post) {
        if (post == null) {
            return null;
        }

        return PostSummaryDto.builder()
                .id(post.getId())
                .name(post.getName())
                .isExecutive(post.getIsExecutive())
                .isActive(post.getIsActive())
                .maxOccupants(post.getMaxOccupants())
                .canAcceptNewMandate(computeCanAcceptNewMandate(post))
                .build();
    }

    // ========== Entity → Detailed DTO (avec rôles suggérés) ==========

    public PostDetailedDto toDetailedDto(Post post) {
        if (post == null) {
            return null;
        }

        Long associationId = extractAssociationId(post);
        String associationName = extractAssociationName(post);

        List<PostDetailedDto.RoleInfo> suggestedRoles = null;
        if (isRolesLoaded(post) && post.getRoles() != null) {
            suggestedRoles = post.getRoles().stream()
                    .map(role -> new PostDetailedDto.RoleInfo(
                            role.getId(),
                            role.getName(),
                            role.getSlug()))
                    .collect(Collectors.toList());
        }

        Role defaultRole = extractDefaultRole(post);
        PostDetailedDto.RoleInfo defaultRoleInfo = defaultRole == null
                ? null
                : new PostDetailedDto.RoleInfo(defaultRole.getId(), defaultRole.getName(), defaultRole.getSlug());

        return PostDetailedDto.builder()
                .id(post.getId())
                .name(post.getName())
                .description(post.getDescription())
                .isExecutive(post.getIsExecutive())
                .maxOccupants(post.getMaxOccupants())
                .displayOrder(post.getDisplayOrder())
                .mandateDurationMonths(post.getMandateDurationMonths())
                .requiresElection(post.getRequiresElection())
                .isActive(post.getIsActive())
                .associationId(associationId)
                .associationName(associationName)
                .defaultRole(defaultRoleInfo)
                .suggestedRoles(suggestedRoles)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private Boolean computeCanAcceptNewMandate(Post post) {
        if (post == null || !post.isActive()) {
            return false;
        }
        if (!post.hasOccupantLimit()) {
            return true;
        }
        if (!Persistence.getPersistenceUtil().isLoaded(post, "mandates")) {
            return null;
        }
        return post.canAcceptNewMandate();
    }

    private boolean isRolesLoaded(Post post) {
        return post != null && PERSISTENCE_UTIL.isLoaded(post, "roles");
    }

    private Long extractAssociationId(Post post) {
        if (post == null || !PERSISTENCE_UTIL.isLoaded(post, "association") || post.getAssociation() == null) {
            return null;
        }
        return post.getAssociation().getId();
    }

    private String extractAssociationName(Post post) {
        if (post == null || !PERSISTENCE_UTIL.isLoaded(post, "association") || post.getAssociation() == null) {
            return null;
        }
        return post.getAssociation().getName();
    }

    private Role extractDefaultRole(Post post) {
        if (post == null || !PERSISTENCE_UTIL.isLoaded(post, "defaultRole") || post.getDefaultRole() == null) {
            return null;
        }
        return post.getDefaultRole();
    }
}
