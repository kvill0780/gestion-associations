package bf.kvill.associa.system.audit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuditStatsDto {
    Long totalLogs;
    Long logsLast24h;
    Long logsLast7days;
    Long criticalLogs;
    Long errorLogs;
    Long warningLogs;
}