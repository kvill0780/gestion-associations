package bf.kvill.associa.members.user.dto;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.shared.enums.MembershipStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class MemberResponseDto {
    Long id;
    String email;
    String firstName;
    String lastName;
    String fullName;
    String whatsapp;
    String interests;
    String profilePicturePath;
    MembershipStatus membershipStatus;
    LocalDate membershipDate;
    Long associationId;
    String associationName;
    Boolean isSuperAdmin;
    Boolean emailVerified;
    List<String> roles;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
