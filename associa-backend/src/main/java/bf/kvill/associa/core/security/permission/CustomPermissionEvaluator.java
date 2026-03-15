package bf.kvill.associa.core.security.permission;

import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        if (hasSuperAdminAuthority(authentication)) {
            return true;
        }

        Long userId = resolveUserId(authentication.getPrincipal());
        if (userId == null) {
            return false;
        }

        String permissionStr = permission.toString();
        Set<String> permissions = permissionService.getUserPermissions(userId);
        if (!permissions.contains(permissionStr)) {
            return false;
        }

        Long targetAssociationId = resolveAssociationId(targetDomainObject);
        if (targetAssociationId == null) {
            return true;
        }

        Long userAssociationId = resolveUserAssociationId(authentication.getPrincipal());
        return userAssociationId != null && userAssociationId.equals(targetAssociationId);
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {
        return hasPermission(authentication, targetId, permission);
    }

    private boolean hasSuperAdminAuthority(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority != null && "ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long resolveUserId(Object principal) {
        if (principal instanceof CustomUserPrincipal custom) {
            return custom.getId();
        }

        return null;
    }

    private Long resolveUserAssociationId(Object principal) {
        if (principal instanceof CustomUserPrincipal custom) {
            return custom.getAssociationId();
        }
        return null;
    }

    private Long resolveAssociationId(Object targetDomainObject) {
        if (targetDomainObject == null) {
            return null;
        }

        if (targetDomainObject instanceof Long associationId) {
            return associationId;
        }

        try {
            Method getAssociationId = targetDomainObject.getClass().getMethod("getAssociationId");
            Object associationId = getAssociationId.invoke(targetDomainObject);
            if (associationId instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
            // Pas de méthode getAssociationId sur cet objet.
        }

        try {
            Method getAssociation = targetDomainObject.getClass().getMethod("getAssociation");
            Object association = getAssociation.invoke(targetDomainObject);
            if (association == null) {
                return null;
            }
            Method getId = association.getClass().getMethod("getId");
            Object id = getId.invoke(association);
            if (id instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
            // Pas de relation association exploitable.
        }

        return null;
    }
}
