package ru.razborka.marketplace.listing.service;

import ru.razborka.marketplace.listing.web.dto.VehicleMakeDto;

import java.util.List;

public interface VehicleCatalogService {
    List<VehicleMakeDto> catalog();
}
