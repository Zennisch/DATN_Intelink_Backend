package intelink.modules.api.controllers;

import intelink.dto.api.ApiKeyResponse;
import intelink.dto.api.CreateApiKeyRequest;
import intelink.models.User;
import intelink.modules.api.services.ApiKeyService;
import intelink.modules.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateApiKeyRequest request) {
        User user = authService.getCurrentUser(userDetails);
        return ResponseEntity.ok(apiKeyService.createApiKey(user, request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails);
        return ResponseEntity.ok(apiKeyService.getApiKeys(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        User user = authService.getCurrentUser(userDetails);
        apiKeyService.deleteApiKey(user, id);
        return ResponseEntity.noContent().build();
    }
}
