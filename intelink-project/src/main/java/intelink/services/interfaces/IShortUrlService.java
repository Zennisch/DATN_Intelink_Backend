package intelink.services.interfaces;

import intelink.dto.request.CreateShortUrlRequest;
import intelink.models.ShortUrl;
import intelink.models.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public interface IShortUrlService {

    ShortUrl create(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException;

}
