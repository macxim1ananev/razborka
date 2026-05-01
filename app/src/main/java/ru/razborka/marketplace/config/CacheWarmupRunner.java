package ru.razborka.marketplace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.razborka.marketplace.listing.service.ListingService;
import ru.razborka.marketplace.search.service.MarketplaceSearchService;

@Component
public class CacheWarmupRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupRunner.class);

    private final ListingService listingService;
    private final MarketplaceSearchService marketplaceSearchService;

    public CacheWarmupRunner(
            ListingService listingService,
            MarketplaceSearchService marketplaceSearchService
    ) {
        this.listingService = listingService;
        this.marketplaceSearchService = marketplaceSearchService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        try {
            listingService.listActivePreviewsForPublic(0, 20);
            listingService.listActivePreviewsForPublic(1, 20);
            marketplaceSearchService.search(new MarketplaceSearchService.SearchParams(
                    null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, 0, 20
            ));
            marketplaceSearchService.search(new MarketplaceSearchService.SearchParams(
                    null, "kia", null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, 0, 20
            ));
        } catch (Exception ex) {
            log.warn("Cache warmup skipped: {}", ex.getMessage());
        }
    }
}
