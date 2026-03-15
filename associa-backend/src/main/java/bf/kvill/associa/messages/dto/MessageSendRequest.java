package bf.kvill.associa.messages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendRequest {
    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    @NotBlank(message = "Le message est obligatoire")
    private String content;
}
