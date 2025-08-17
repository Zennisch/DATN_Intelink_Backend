package intelink.dto.helper.threat.request;

public record SafeBrowsingRequest(
        SafeBrowsingClient client,
        ThreatInfo threatInfo
) {
}