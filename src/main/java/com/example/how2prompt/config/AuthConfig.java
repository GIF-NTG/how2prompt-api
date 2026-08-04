package com.example.how2prompt.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Configuration
@EnableConfigurationProperties({AuthProperties.class, MailProperties.class})
public class AuthConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(AuthProperties authProperties) {
        if (!StringUtils.hasText(authProperties.getGoogleClientId())) {
            return null;
        }
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(authProperties.getGoogleClientId()))
                .build();
    }
}
