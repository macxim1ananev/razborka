package ru.razborka.marketplace.search.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.razborka.marketplace.listing.event.ListingIndexEvent;
import ru.razborka.marketplace.listing.event.ListingIndexEvent.ListingIndexAction;
import ru.razborka.marketplace.search.service.ListingIndexService;

@Component
public class ListingIndexListener {

    private final ListingIndexService listingIndexService;

    public ListingIndexListener(ListingIndexService listingIndexService) {
        this.listingIndexService = listingIndexService;
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onListingIndex(ListingIndexEvent event) {
        if (event.action() == ListingIndexAction.DELETE) {
            listingIndexService.delete(event.listingId());
        } else {
            listingIndexService.upsert(event.listingId());
        }
    }
}
