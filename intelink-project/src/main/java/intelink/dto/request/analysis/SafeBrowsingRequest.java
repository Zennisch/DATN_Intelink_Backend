package intelink.dto.request.analysis;

public record SafeBrowsingRequest(
        SafeBrowsingClient client,
        ThreatInfo threatInfo
) {
}