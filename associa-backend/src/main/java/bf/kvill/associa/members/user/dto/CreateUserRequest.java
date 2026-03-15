package bf.kvill.associa.members.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateUserRequest {
    String email;
    String password;
    String firstName;
    String lastName;
    String whatsapp;
    Long associationId;
}
