package intelink.dto.object.threat.request;

public record SafeBrowsingRequest(
        SafeBrowsingClient client,
        ThreatInfo threatInfo
) {
}