package bf.kvill.associa.system.association.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SlugAvailabilityDto {
    String slug;
    Boolean available;
}