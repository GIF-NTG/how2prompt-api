package com.example.how2prompt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "how2prompt.auth")
public class AuthProperties {

    /**
     * Google OAuth client ID (audience) dùng để verify id_token từ GIS.
     */
    private String googleClientId = "";

    /**
     * Cookie refresh_token: secure=true bắt buộc trên HTTPS (prod).
     * Local HTTP có thể set AUTH_COOKIE_SECURE=false.
     */
    private boolean cookieSecure = true;

    private String cookieSameSite = "Strict";

    private String cookiePath = "/api/v1/auth";

    private String cookieName = "refresh_token";

    /**
     * Origin frontend SPA (CORS + credentials). Rỗng = không bật CORS custom.
     */
    private List<String> corsAllowedOrigins = new ArrayList<>();
}
