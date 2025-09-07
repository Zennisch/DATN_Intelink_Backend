package intelink.dto.object.threat;

public record ThreatEntry(String url) {
    public ThreatEntry {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }
    }
}
