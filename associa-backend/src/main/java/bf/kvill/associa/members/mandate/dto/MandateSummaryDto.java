package bf.kvill.associa.members.mandate.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MandateSummaryDto {
    Long id;
    String userFullName;
    String postName;
    LocalDate startDate;
    LocalDate endDate;
    Boolean active;
}