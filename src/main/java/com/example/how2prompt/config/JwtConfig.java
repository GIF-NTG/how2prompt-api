package com.example.how2prompt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public KeyPair jwtKeyPair(JwtProperties props) throws Exception {
        if (StringUtils.hasText(props.getPrivateKeyPem()) && StringUtils.hasText(props.getPublicKeyPem())) {
            return new KeyPair(
                    readPublicKey(props.getPublicKeyPem()),
                    readPrivateKey(props.getPrivateKeyPem())
            );
        }
        log.warn("how2prompt.jwt.private-key-pem / public-key-pem chưa được cấu hình -> " +
                "tự sinh cặp khoá RSA-2048 TẠM THỜI (chỉ hợp lệ cho tới lần restart app kế " +
                "tiếp). CHỈ chấp nhận ở profile local, không dùng ở dev/prod.");
        return generateEphemeralKeyPair();
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(KeyPair jwtKeyPair) {
        return (RSAPrivateKey) jwtKeyPair.getPrivate();
    }

    @Bean
    public RSAPublicKey jwtPublicKey(KeyPair jwtKeyPair) {
        return (RSAPublicKey) jwtKeyPair.getPublic();
    }

    private RSAPrivateKey readPrivateKey(String pem) throws Exception {
        byte[] der = Base64.getDecoder().decode(stripPem(pem));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private RSAPublicKey readPublicKey(String pem) throws Exception {
        byte[] der = Base64.getDecoder().decode(stripPem(pem));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
    }

    private String stripPem(String pem) {
        return pem
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }

    private KeyPair generateEphemeralKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
}
