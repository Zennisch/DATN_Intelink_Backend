package intelink.models;

import intelink.models.enums.AccessControlType;

public class ShortUrlAccessControl {
    public Long id;
    public ShortUrl shortUrl;
    
    public AccessControlType type;
    public String value;
}
