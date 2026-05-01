package ru.razborka.marketplace.listing.service;

import org.springframework.data.domain.Page;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;

public interface FavoriteService {
    Page<ListingPreviewDto> myFavorites(int page, int size);

    void add(Long listingId);

    void remove(Long listingId);
}
