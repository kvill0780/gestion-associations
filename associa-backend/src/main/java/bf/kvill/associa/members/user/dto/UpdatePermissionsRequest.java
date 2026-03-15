package bf.kvill.associa.members.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class UpdatePermissionsRequest {
    Map<String, Object> permissions;
}
