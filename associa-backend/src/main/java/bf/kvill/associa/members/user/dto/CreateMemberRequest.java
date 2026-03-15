package bf.kvill.associa.members.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email  invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$", message = "Numéro invalide")
    private String whatsapp;

    private String interests;

    @NotNull(message = "L'association est obligatoire")
    private Long associationId;
}
