package bf.kvill.associa.dashboard;

import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.dashboard.dto.DashboardDto;
import bf.kvill.associa.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Statistiques agrégées pour le tableau de bord de l'association")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Tableau de bord", description = "Retourne les stats membres, finances, événements et documents de l'association du membre connecté")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getId();
        Long associationId = principal.getAssociationId();

        DashboardDto dashboard = dashboardService.buildDashboard(userId, associationId);

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard chargé", dashboard));
    }
}
