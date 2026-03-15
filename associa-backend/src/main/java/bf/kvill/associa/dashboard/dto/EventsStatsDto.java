package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventsStatsDto {
    private Long totalUpcoming;
    private Long totalPast;
    private List<Object> upcoming; // Placeholder until Event module is created

    public EventsStatsDto() {
    }

    public EventsStatsDto(Long totalUpcoming, Long totalPast, List<Object> upcoming) {
        this.totalUpcoming = totalUpcoming;
        this.totalPast = totalPast;
        this.upcoming = upcoming;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getTotalUpcoming() {
        return totalUpcoming;
    }

    public void setTotalUpcoming(Long totalUpcoming) {
        this.totalUpcoming = totalUpcoming;
    }

    public Long getTotalPast() {
        return totalPast;
    }

    public void setTotalPast(Long totalPast) {
        this.totalPast = totalPast;
    }

    public List<Object> getUpcoming() {
        return upcoming;
    }

    public void setUpcoming(List<Object> upcoming) {
        this.upcoming = upcoming;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static EventsStatsDtoBuilder builder() {
        return new EventsStatsDtoBuilder();
    }

    public static class EventsStatsDtoBuilder {
        private Long totalUpcoming;
        private Long totalPast;
        private List<Object> upcoming;

        EventsStatsDtoBuilder() {
        }

        public EventsStatsDtoBuilder totalUpcoming(Long totalUpcoming) {
            this.totalUpcoming = totalUpcoming;
            return this;
        }

        public EventsStatsDtoBuilder totalPast(Long totalPast) {
            this.totalPast = totalPast;
            return this;
        }

        public EventsStatsDtoBuilder upcoming(List<Object> upcoming) {
            this.upcoming = upcoming;
            return this;
        }

        public EventsStatsDto build() {
            EventsStatsDto dto = new EventsStatsDto();
            dto.setTotalUpcoming(totalUpcoming);
            dto.setTotalPast(totalPast);
            dto.setUpcoming(upcoming);
            return dto;
        }
    }
}
