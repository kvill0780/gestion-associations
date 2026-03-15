package bf.kvill.associa.core.security.permission;

import bf.kvill.associa.core.config.PermissionsConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/core/permissions")
@PreAuthorize("hasPermission(null, 'roles.manage')")
public class PermissionController {

    @GetMapping("/grouped")
    public ResponseEntity<List<PermissionCategoryDto>> getGroupedPermissions() {
        List<PermissionCategoryDto> categories = PermissionsConfig.CATEGORIES.stream()
                .map(cat -> new PermissionCategoryDto(
                        cat.key(),
                        cat.label(),
                        cat.icon(),
                        cat.permissions()
                ))
                .toList();

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    public ResponseEntity<Set<String>> getAllPermissions() {
        return ResponseEntity.ok(PermissionsConfig.ALL_PERMISSIONS);
    }

    public record PermissionCategoryDto(
            String key,
            String label,
            String icon,
            List<String> permissions
    ) {
    }
}
