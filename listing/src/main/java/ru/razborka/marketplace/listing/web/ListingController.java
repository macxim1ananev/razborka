package ru.razborka.marketplace.listing.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.razborka.marketplace.listing.service.ListingService;
import ru.razborka.marketplace.listing.web.dto.CreateListingRequest;
import ru.razborka.marketplace.listing.web.dto.ListingDetailDto;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;

import java.io.IOException;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public Page<ListingPreviewDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return listingService.listActivePreviewsForPublic(page, size);
    }

    @GetMapping("/{id}")
    public ListingDetailDto get(@PathVariable Long id) {
        return listingService.getDetail(id);
    }

    @PostMapping
    public ListingDetailDto create(@Valid @RequestBody CreateListingRequest request) {
        return listingService.create(request);
    }

    @PutMapping("/{id}")
    public ListingDetailDto update(@PathVariable Long id, @Valid @RequestBody CreateListingRequest request) {
        return listingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean sold
    ) {
        listingService.deleteOrArchive(id, sold);
    }

    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public void uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        listingService.addPhoto(id, file);
    }
}
