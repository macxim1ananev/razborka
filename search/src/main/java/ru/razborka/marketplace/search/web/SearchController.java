package ru.razborka.marketplace.search.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.search.service.MarketplaceSearchService;
import ru.razborka.marketplace.search.service.MarketplaceSearchService.SearchParams;
import ru.razborka.marketplace.search.web.dto.SearchResponseDto;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final MarketplaceSearchService marketplaceSearchService;

    public SearchController(MarketplaceSearchService marketplaceSearchService) {
        this.marketplaceSearchService = marketplaceSearchService;
    }

    @GetMapping
    public SearchResponseDto search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String generation,
            @RequestParam(required = false) String vin,
            @RequestParam(required = false) String catalogBlock,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Double engineVolume,
            @RequestParam(required = false) String partCondition,
            @RequestParam(required = false) String originalReplica,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchParams params = new SearchParams(
                categoryId,
                brand,
                model,
                generation,
                vin,
                catalogBlock,
                yearFrom,
                yearTo,
                engineVolume,
                partCondition,
                originalReplica,
                priceMin,
                priceMax,
                city,
                q,
                page,
                size
        );
        return marketplaceSearchService.search(params);
    }
}
