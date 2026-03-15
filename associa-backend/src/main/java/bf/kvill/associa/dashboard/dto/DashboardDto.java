package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardDto {
    private MembersStatsDto members;
    private FinancesStatsDto finances;
    private EventsStatsDto events;
    private DocumentsStatsDto documents;
    private List<ActivityDto> recentActivities;

    public DashboardDto() {
    }

    public DashboardDto(MembersStatsDto members, FinancesStatsDto finances, EventsStatsDto events,
            DocumentsStatsDto documents, List<ActivityDto> recentActivities) {
        this.members = members;
        this.finances = finances;
        this.events = events;
        this.documents = documents;
        this.recentActivities = recentActivities;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public MembersStatsDto getMembers() {
        return members;
    }

    public void setMembers(MembersStatsDto members) {
        this.members = members;
    }

    public FinancesStatsDto getFinances() {
        return finances;
    }

    public void setFinances(FinancesStatsDto finances) {
        this.finances = finances;
    }

    public EventsStatsDto getEvents() {
        return events;
    }

    public void setEvents(EventsStatsDto events) {
        this.events = events;
    }

    public DocumentsStatsDto getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentsStatsDto documents) {
        this.documents = documents;
    }

    public List<ActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<ActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static DashboardDtoBuilder builder() {
        return new DashboardDtoBuilder();
    }

    public static class DashboardDtoBuilder {
        private MembersStatsDto members;
        private FinancesStatsDto finances;
        private EventsStatsDto events;
        private DocumentsStatsDto documents;
        private List<ActivityDto> recentActivities;

        DashboardDtoBuilder() {
        }

        public DashboardDtoBuilder members(MembersStatsDto members) {
            this.members = members;
            return this;
        }

        public DashboardDtoBuilder finances(FinancesStatsDto finances) {
            this.finances = finances;
            return this;
        }

        public DashboardDtoBuilder events(EventsStatsDto events) {
            this.events = events;
            return this;
        }

        public DashboardDtoBuilder documents(DocumentsStatsDto documents) {
            this.documents = documents;
            return this;
        }

        public DashboardDtoBuilder recentActivities(List<ActivityDto> recentActivities) {
            this.recentActivities = recentActivities;
            return this;
        }

        public DashboardDto build() {
            DashboardDto dashboardDto = new DashboardDto();
            dashboardDto.setMembers(members);
            dashboardDto.setFinances(finances);
            dashboardDto.setEvents(events);
            dashboardDto.setDocuments(documents);
            dashboardDto.setRecentActivities(recentActivities);
            return dashboardDto;
        }
    }
}
