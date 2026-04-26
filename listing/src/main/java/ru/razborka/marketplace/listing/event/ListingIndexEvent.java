package ru.razborka.marketplace.listing.event;

public record ListingIndexEvent(long listingId, ListingIndexAction action) {

    public enum ListingIndexAction {
        UPSERT,
        DELETE
    }
}
