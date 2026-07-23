package com.example.how2prompt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "how2prompt.jwt")
public class JwtProperties {

    private String privateKeyPem;

    private String publicKeyPem;

    private String issuer = "how2prompt";

    private Duration accessTokenTtl = Duration.ofMinutes(15);

    private Duration refreshTokenTtl = Duration.ofDays(30);
}
