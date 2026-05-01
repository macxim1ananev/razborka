package ru.razborka.marketplace.search.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.listing.domain.ListingPhoto;
import ru.razborka.marketplace.listing.repository.ListingRepository;
import ru.razborka.marketplace.search.document.ListingSearchDocument;
import ru.razborka.marketplace.search.repository.ListingSearchRepository;

import java.util.Comparator;
import java.util.Optional;

@Service
public class ListingIndexServiceImpl implements ListingIndexService {
    private static final Logger log = LoggerFactory.getLogger(ListingIndexService.class);

    private final ListingRepository listingRepository;
    private final ListingSearchRepository listingSearchRepository;

    public ListingIndexServiceImpl(ListingRepository listingRepository, ListingSearchRepository listingSearchRepository) {
        this.listingRepository = listingRepository;
        this.listingSearchRepository = listingSearchRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @CacheEvict(value = "searchResults", allEntries = true)
    public void upsert(Long listingId) {
        var opt = listingRepository.findDetailById(listingId);
        if (opt.isEmpty()) {
            listingSearchRepository.deleteById(listingId);
            return;
        }
        var l = opt.get();
        if (!"active".equals(l.getStatus().name())) {
            listingSearchRepository.deleteById(listingId);
            return;
        }
        String first = l.getPhotos().stream()
                .sorted(Comparator.comparing((ListingPhoto p) -> Optional.ofNullable(p.getSortOrder()).orElse(0))
                        .thenComparing(ListingPhoto::getId))
                .map(ListingPhoto::getPhotoUrl)
                .findFirst()
                .orElse("");
        ListingSearchDocument doc = ListingSearchDocument.fromListing(l, first);
        executeWithRetry(() -> listingSearchRepository.save(doc), listingId, "upsert");
    }

    @Override
    @CacheEvict(value = "searchResults", allEntries = true)
    public void delete(Long listingId) {
        executeWithRetry(() -> listingSearchRepository.deleteById(listingId), listingId, "delete");
    }

    private void executeWithRetry(Runnable action, Long listingId, String operation) {
        int maxAttempts = 3;
        long backoffMs = 300L;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                action.run();
                return;
            } catch (Exception ex) {
                if (attempt == maxAttempts) {
                    log.error("Search index {} failed for listing {} after {} attempts", operation, listingId, maxAttempts, ex);
                    return;
                }
                try {
                    Thread.sleep(backoffMs * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
