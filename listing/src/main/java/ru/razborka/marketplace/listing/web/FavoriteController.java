package ru.razborka.marketplace.listing.web;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.listing.service.FavoriteService;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;

@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/api/users/me/favorites")
    public Page<ListingPreviewDto> myFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return favoriteService.myFavorites(page, size);
    }

    @PostMapping("/api/favorites/{listingId}")
    public void add(@PathVariable Long listingId) {
        favoriteService.add(listingId);
    }

    @DeleteMapping("/api/favorites/{listingId}")
    public void remove(@PathVariable Long listingId) {
        favoriteService.remove(listingId);
    }
}
