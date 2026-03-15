package bf.kvill.associa.members.user.mapper;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.dto.MemberResponseDto;
import bf.kvill.associa.members.user.dto.MemberSummaryDto;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    public MemberResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return MemberResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .whatsapp(user.getWhatsapp())
                .interests(user.getInterests())
                .profilePicturePath(user.getProfilePicturePath())
                .membershipStatus(user.getMembershipStatus())
                .membershipDate(user.getMembershipDate())
                .associationId(extractAssociationId(user))
                .associationName(extractAssociationName(user))
                .isSuperAdmin(user.getIsSuperAdmin())
                .emailVerified(user.isEmailVerified())
                .roles(extractRoleNames(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private List<String> extractRoleNames(User user) {
        if (user == null || !PERSISTENCE_UTIL.isLoaded(user, "userRoles") || user.getUserRoles() == null) {
            return List.of();
        }
        return user.getUserRoles().stream()
                                .filter(userRole -> userRole.isCurrentlyValid())
                                .map(userRole -> userRole.getRole())
                                .filter(role -> role != null)
                                .map(role -> role.getName())
                                .distinct()
                                .toList();
    }

    private Long extractAssociationId(User user) {
        if (user == null || !PERSISTENCE_UTIL.isLoaded(user, "association") || user.getAssociation() == null) {
            return null;
        }
        return user.getAssociation().getId();
    }

    private String extractAssociationName(User user) {
        if (user == null || !PERSISTENCE_UTIL.isLoaded(user, "association") || user.getAssociation() == null) {
            return null;
        }
        return user.getAssociation().getName();
    }

    public MemberSummaryDto toSummaryDto(User user) {
        if (user == null) {
            return null;
        }

        return MemberSummaryDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .membershipStatus(user.getMembershipStatus())
                .membershipDate(user.getMembershipDate())
                .profilePicturePath(user.getProfilePicturePath())
                .build();
    }

}
