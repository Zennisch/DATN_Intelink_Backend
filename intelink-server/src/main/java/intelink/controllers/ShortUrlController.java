package intelink.controllers;

import intelink.modules.url.ShortUrlService;
import intelink.modules.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/url")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

}
