package ru.razborka.marketplace.listing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.razborka.marketplace.listing.media.ListingMediaDeliveryProperties;

@Configuration
@EnableConfigurationProperties(ListingMediaDeliveryProperties.class)
public class ListingMediaDeliveryConfig {
}
