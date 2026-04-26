package ru.razborka.marketplace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.razborka.marketplace.listing.domain.ListingStatus;
import ru.razborka.marketplace.listing.repository.ListingRepository;
import ru.razborka.marketplace.search.service.ListingIndexService;

@Component
public class SearchReindexRunner {

    private static final Logger log = LoggerFactory.getLogger(SearchReindexRunner.class);

    private final ListingRepository listingRepository;
    private final ListingIndexService listingIndexService;

    public SearchReindexRunner(ListingRepository listingRepository, ListingIndexService listingIndexService) {
        this.listingRepository = listingRepository;
        this.listingIndexService = listingIndexService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reindexActiveListings() {
        try {
            int total = 0;
            for (Long id : listingRepository.findIdsByStatus(ListingStatus.active)) {
                listingIndexService.upsert(id);
                total++;
            }
            log.info("Search index bootstrap completed: {} active listings indexed", total);
        } catch (Exception ex) {
            log.warn("Search index bootstrap skipped: {}", ex.getMessage());
        }
    }
}
