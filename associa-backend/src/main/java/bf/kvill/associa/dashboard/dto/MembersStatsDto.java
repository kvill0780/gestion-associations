package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MembersStatsDto {
    private Long total;
    private Long active;
    private Long pending;
    private Long inactive;

    public MembersStatsDto() {
    }

    public MembersStatsDto(Long total, Long active, Long pending, Long inactive) {
        this.total = total;
        this.active = active;
        this.pending = pending;
        this.inactive = inactive;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static MembersStatsDtoBuilder builder() {
        return new MembersStatsDtoBuilder();
    }

    public static class MembersStatsDtoBuilder {
        private Long total;
        private Long active;
        private Long pending;
        private Long inactive;

        MembersStatsDtoBuilder() {
        }

        public MembersStatsDtoBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public MembersStatsDtoBuilder active(Long active) {
            this.active = active;
            return this;
        }

        public MembersStatsDtoBuilder pending(Long pending) {
            this.pending = pending;
            return this;
        }

        public MembersStatsDtoBuilder inactive(Long inactive) {
            this.inactive = inactive;
            return this;
        }

        public MembersStatsDto build() {
            MembersStatsDto dto = new MembersStatsDto();
            dto.setTotal(total);
            dto.setActive(active);
            dto.setPending(pending);
            dto.setInactive(inactive);
            return dto;
        }
    }
}
