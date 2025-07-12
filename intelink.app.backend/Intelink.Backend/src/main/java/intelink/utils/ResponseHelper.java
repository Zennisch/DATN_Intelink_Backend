package intelink.utils;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseHelper {

    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
