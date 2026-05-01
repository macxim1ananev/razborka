package ru.razborka.marketplace.search.service;

public interface ListingIndexService {
    void upsert(Long listingId);

    void delete(Long listingId);
}
