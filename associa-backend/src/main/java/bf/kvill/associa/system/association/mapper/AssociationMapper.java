package bf.kvill.associa.system.association.mapper;

import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.dto.AssociationResponseDto;
import bf.kvill.associa.system.association.dto.AssociationSummaryDto;
import bf.kvill.associa.system.association.dto.CreateAssociationRequest;
import bf.kvill.associa.system.association.dto.UpdateAssociationRequest;
import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.shared.util.SlugUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper pour Association
 */
@Component
public class AssociationMapper {

    // ========== Entity → Response DTO ==========

    public AssociationResponseDto toResponseDto(Association association) {
        if (association == null) return null;

        return AssociationResponseDto.builder()
                .id(association.getId())
                .name(association.getName())
                .slug(association.getSlug())
                .description(association.getDescription())
                .logoPath(association.getLogoPath())
                .contactEmail(association.getContactEmail())
                .contactPhone(association.getContactPhone())
                .address(association.getAddress())
                .status(association.getStatus())
                .type(association.getType())
                .defaultMembershipFee(association.getDefaultMembershipFee())
                .membershipValidityMonths(association.getMembershipValidityMonths())
                .financeApprovalWorkflow(association.getFinanceApprovalWorkflow())
                .createdById(association.getCreatedBy() != null ? association.getCreatedBy().getId() : null)
                .createdByName(association.getCreatedBy() != null ? association.getCreatedBy().getFullName() : null)
                .createdAt(association.getCreatedAt())
                .updatedAt(association.getUpdatedAt())
                .build();
    }

    // ========== Entity → Summary DTO ==========

    public AssociationSummaryDto toSummaryDto(Association association) {
        if (association == null) return null;

        return AssociationSummaryDto.builder()
                .id(association.getId())
                .name(association.getName())
                .slug(association.getSlug())
                .logoPath(association.getLogoPath())
                .status(association.getStatus())
                .type(association.getType())
                .contactEmail(association.getContactEmail())
                .build();
    }

    // ========== Create Request → Entity ==========

    public Association toEntity(CreateAssociationRequest request, User createdBy) {
        if (request == null) return null;

        return Association.builder()
                .name(request.getName())
                .slug(SlugUtils.generateSlug(request.getName()))
                .description(request.getDescription())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .status(AssociationStatus.ACTIVE)
                .type(request.getType())
                .defaultMembershipFee(request.getDefaultMembershipFee() != null
                        ? request.getDefaultMembershipFee()
                        : BigDecimal.valueOf(10000.00))
                .membershipValidityMonths(request.getMembershipValidityMonths() != null
                        ? request.getMembershipValidityMonths()
                        : 12)
                .financeApprovalWorkflow(request.getFinanceApprovalWorkflow() != null
                        ? request.getFinanceApprovalWorkflow()
                        : false)
                .createdBy(createdBy)
                .build();
    }

    // ========== Update Request → Apply changes ==========

    public void updateEntity(Association association, UpdateAssociationRequest request) {
        if (request.getName() != null) {
            association.setName(request.getName());
            association.setSlug(SlugUtils.generateSlug(request.getName()));
        }
        if (request.getDescription() != null) {
            association.setDescription(request.getDescription());
        }
        if (request.getLogoPath() != null) {
            association.setLogoPath(request.getLogoPath());
        }
        if (request.getContactEmail() != null) {
            association.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            association.setContactPhone(request.getContactPhone());
        }
        if (request.getAddress() != null) {
            association.setAddress(request.getAddress());
        }
        if (request.getDefaultMembershipFee() != null) {
            association.setDefaultMembershipFee(request.getDefaultMembershipFee());
        }
        if (request.getMembershipValidityMonths() != null) {
            association.setMembershipValidityMonths(request.getMembershipValidityMonths());
        }
        if (request.getFinanceApprovalWorkflow() != null) {
            association.setFinanceApprovalWorkflow(request.getFinanceApprovalWorkflow());
        }
    }
}