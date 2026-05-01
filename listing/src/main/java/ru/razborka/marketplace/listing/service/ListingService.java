package ru.razborka.marketplace.listing.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import ru.razborka.marketplace.listing.domain.Listing;
import ru.razborka.marketplace.listing.web.dto.CreateListingRequest;
import ru.razborka.marketplace.listing.web.dto.ListingDetailDto;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;

import java.io.IOException;

public interface ListingService {
    Page<ListingPreviewDto> listActivePreviewsForPublic(int page, int size);

    ListingDetailDto getDetail(Long id);

    Page<ListingPreviewDto> myListings(int page, int size);

    ListingDetailDto create(CreateListingRequest req);

    ListingDetailDto update(Long id, CreateListingRequest req);

    void deleteOrArchive(Long id, boolean markSold);

    void addPhoto(Long listingId, MultipartFile file) throws IOException;

    ListingPreviewDto toPreviewDto(Listing listing);
}
