package intelink.modules.redirect.controllers;

import intelink.modules.redirect.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{shortCode}/device")
    public ResponseEntity<?> getDeviceStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{shortCode}/geopgraphy")
    public ResponseEntity<?> getGeographyStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(null);
    }

}
