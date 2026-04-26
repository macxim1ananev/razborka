package ru.razborka.marketplace.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.razborka.marketplace.auth.jwt.JwtProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, TelegramBotProperties.class})
public class AuthBeansConfig {
}
