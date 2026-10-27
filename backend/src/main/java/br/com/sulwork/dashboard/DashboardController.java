package br.com.sulwork.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard")
    @Operation(summary = "Indicadores gerais: eventos abertos, cobertura média, obrigatórios pendentes e próximos eventos")
    public Dashboard dashboard() {
        return dashboardService.montar();
    }
}
