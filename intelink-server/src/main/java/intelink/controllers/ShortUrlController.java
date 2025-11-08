package intelink.controllers;

import intelink.services.ShortUrlService;
import intelink.services.UserService;
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
