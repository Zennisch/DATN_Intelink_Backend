package intelink.controllers;

import intelink.dto.request.api_key.CreateApiKeyRequest;
import intelink.dto.response.api_key.ApiKeyResponse;
import intelink.models.User;
import intelink.services.ApiKeyService;
import intelink.services.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final IUserService userService;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal User user) {
        List<ApiKeyResponse> keys = apiKeyService.listByUser(user);
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return apiKeyService.getById(id, user)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "API key not found"
                )));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateApiKeyRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        ApiKeyResponse created = apiKeyService.create(user, req);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CreateApiKeyRequest req, @AuthenticationPrincipal User user) {
        return apiKeyService.update(id, user, req)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "API key not found or not owned by user"
                )));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        boolean deleted = apiKeyService.delete(id, user);
        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "API key deleted successfully"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "API key not found or not owned by user"
            ));
        }
    }
}