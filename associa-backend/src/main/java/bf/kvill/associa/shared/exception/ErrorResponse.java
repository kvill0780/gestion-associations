package bf.kvill.associa.shared.exception;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class ErrorResponse {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    Map<String, String> details;
}