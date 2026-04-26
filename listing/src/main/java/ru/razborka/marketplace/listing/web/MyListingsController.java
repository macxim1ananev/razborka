package ru.razborka.marketplace.listing.web;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.listing.service.ListingService;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;

@RestController
@RequestMapping("/api/users/me")
public class MyListingsController {

    private final ListingService listingService;

    public MyListingsController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings")
    public Page<ListingPreviewDto> myListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return listingService.myListings(page, size);
    }
}
