package bf.kvill.associa.security.userdetails;

import bf.kvill.associa.members.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private boolean active;
    private boolean suspended;
    private Long associationId;

    public static CustomUserPrincipal create(User user, Collection<? extends GrantedAuthority> authorities) {
        return CustomUserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .authorities(authorities)
                .active(user.isActiveMember())
                .suspended(user.isSuspended())
                .associationId(user.getAssociation() != null ? user.getAssociation().getId() : null)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !suspended;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Long getAssociationId() {
        return associationId;
    }

    public void setAssociationId(Long associationId) {
        this.associationId = associationId;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static CustomUserPrincipalBuilder builder() {
        return new CustomUserPrincipalBuilder();
    }

    public static class CustomUserPrincipalBuilder {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean active;
        private boolean suspended;
        private Long associationId;

        CustomUserPrincipalBuilder() {
        }

        public CustomUserPrincipalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CustomUserPrincipalBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CustomUserPrincipalBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public CustomUserPrincipalBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public CustomUserPrincipalBuilder password(String password) {
            this.password = password;
            return this;
        }

        public CustomUserPrincipalBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public CustomUserPrincipalBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public CustomUserPrincipalBuilder suspended(boolean suspended) {
            this.suspended = suspended;
            return this;
        }

        public CustomUserPrincipalBuilder associationId(Long associationId) {
            this.associationId = associationId;
            return this;
        }

        public CustomUserPrincipal build() {
            CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal();
            customUserPrincipal.setId(id);
            customUserPrincipal.setEmail(email);
            customUserPrincipal.setFirstName(firstName);
            customUserPrincipal.setLastName(lastName);
            customUserPrincipal.setPassword(password);
            customUserPrincipal.setAuthorities(authorities);
            customUserPrincipal.setActive(active);
            customUserPrincipal.setSuspended(suspended);
            customUserPrincipal.setAssociationId(associationId);
            return customUserPrincipal;
        }
    }
}
