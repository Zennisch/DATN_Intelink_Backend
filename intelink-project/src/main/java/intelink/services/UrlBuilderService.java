package intelink.services;

import intelink.models.ShortUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderService {

    @Value("${app.host.backend}")
    private String backendHost;

    public String buildFullUrl(ShortUrl shortUrl) {
        if (shortUrl.getCustomDomain() != null && shortUrl.getCustomDomain().isVerified()) {
            return "https://" + shortUrl.getCustomDomain().getDomain() + "/" + shortUrl.getShortCode();
        }
        return backendHost + "/" + shortUrl.getShortCode();
    }
}
