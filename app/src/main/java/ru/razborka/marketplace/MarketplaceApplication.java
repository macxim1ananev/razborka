package ru.razborka.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "ru.razborka.marketplace")
@EnableJpaRepositories(basePackages = {
        "ru.razborka.marketplace.user.repository",
        "ru.razborka.marketplace.auth.repository",
        "ru.razborka.marketplace.listing.repository"
})
@EnableElasticsearchRepositories(basePackages = "ru.razborka.marketplace.search.repository")
@EnableCaching
@EnableAsync
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }
}
