package ru.razborka.marketplace.listing.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.listing.service.VehicleCatalogService;
import ru.razborka.marketplace.listing.web.dto.VehicleMakeDto;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleCatalogController {

    private final VehicleCatalogService vehicleCatalogService;

    public VehicleCatalogController(VehicleCatalogService vehicleCatalogService) {
        this.vehicleCatalogService = vehicleCatalogService;
    }

    @GetMapping("/catalog")
    public List<VehicleMakeDto> catalog() {
        return vehicleCatalogService.catalog();
    }
}
