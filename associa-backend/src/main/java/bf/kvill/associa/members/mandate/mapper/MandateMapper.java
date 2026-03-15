package bf.kvill.associa.members.mandate.mapper;

import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.members.mandate.dto.*;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import org.springframework.stereotype.Component;

@Component
public class MandateMapper {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    public MandateResponseDto toResponseDto(Mandate mandate) {
        if (mandate == null) return null;

        return MandateResponseDto.builder()
                .id(mandate.getId())
                .userId(extractUserId(mandate))
                .userFullName(extractUserFullName(mandate))
                .postId(extractPostId(mandate))
                .postName(extractPostName(mandate))
                .associationId(extractAssociationId(mandate))
                .associationName(extractAssociationName(mandate))
                .startDate(mandate.getStartDate())
                .endDate(mandate.getEndDate())
                .active(mandate.getActive())
                .notes(mandate.getNotes())
                .assignedById(mandate.getAssignedById())
                .createdAt(mandate.getCreatedAt())
                .build();
    }

    public MandateSummaryDto toSummaryDto(Mandate mandate) {
        if (mandate == null) return null;

        return MandateSummaryDto.builder()
                .id(mandate.getId())
                .userFullName(extractUserFullName(mandate))
                .postName(extractPostName(mandate))
                .startDate(mandate.getStartDate())
                .endDate(mandate.getEndDate())
                .active(mandate.getActive())
                .build();
    }

    private Long extractUserId(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "user") || mandate.getUser() == null) {
            return null;
        }
        return mandate.getUser().getId();
    }

    private String extractUserFullName(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "user") || mandate.getUser() == null) {
            return null;
        }
        return mandate.getUser().getFullName();
    }

    private Long extractPostId(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "post") || mandate.getPost() == null) {
            return null;
        }
        return mandate.getPost().getId();
    }

    private String extractPostName(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "post") || mandate.getPost() == null) {
            return null;
        }
        return mandate.getPost().getName();
    }

    private Long extractAssociationId(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "association") || mandate.getAssociation() == null) {
            return null;
        }
        return mandate.getAssociation().getId();
    }

    private String extractAssociationName(Mandate mandate) {
        if (mandate == null || !PERSISTENCE_UTIL.isLoaded(mandate, "association") || mandate.getAssociation() == null) {
            return null;
        }
        return mandate.getAssociation().getName();
    }
}
