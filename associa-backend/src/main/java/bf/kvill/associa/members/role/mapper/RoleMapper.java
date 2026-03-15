package bf.kvill.associa.members.role.mapper;

import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.dto.RoleResponseDto;
import bf.kvill.associa.members.role.dto.RoleSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponseDto toResponseDto(Role role) {
        if (role == null) return null;

        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .slug(role.getSlug())
                .description(role.getDescription())
                .type(role.getType())
                .permissions(role.getPermissions())
                .isTemplate(role.getIsTemplate())
                .displayOrder(role.getDisplayOrder())
                .associationId(role.getAssociation() != null ? role.getAssociation().getId() : null)
                .createdAt(role.getCreatedAt())
                .build();
    }

    public RoleSummaryDto toSummaryDto(Role role) {
        if (role == null) return null;

        return RoleSummaryDto.builder()
                .id(role.getId())
                .name(role.getName())
                .slug(role.getSlug())
                .type(role.getType())
                .build();
    }
}