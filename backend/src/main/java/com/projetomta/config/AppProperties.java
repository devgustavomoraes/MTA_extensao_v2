package com.projetomta.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Security security = new Security();
    private Cors cors = new Cors();
    private Seed seed = new Seed();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    @Getter
    @Setter
    public static class Security {
        private int loginMaxAttempts = 5;
        private int loginLockDurationMin = 30;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter
    @Setter
    public static class Seed {
        private List<AdminAccount> admins = new ArrayList<>();

        private String adminEmail;
        private String adminPassword;

    }
}
