package ru.razborka.marketplace.listing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.listing.domain.VehicleGeneration;
import ru.razborka.marketplace.listing.domain.VehicleMake;
import ru.razborka.marketplace.listing.domain.VehicleModel;
import ru.razborka.marketplace.listing.repository.VehicleGenerationRepository;
import ru.razborka.marketplace.listing.repository.VehicleMakeRepository;
import ru.razborka.marketplace.listing.repository.VehicleModelRepository;
import ru.razborka.marketplace.listing.web.dto.VehicleGenerationDto;
import ru.razborka.marketplace.listing.web.dto.VehicleMakeDto;
import ru.razborka.marketplace.listing.web.dto.VehicleModelDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VehicleCatalogService {

    private final VehicleMakeRepository vehicleMakeRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleGenerationRepository vehicleGenerationRepository;

    public VehicleCatalogService(
            VehicleMakeRepository vehicleMakeRepository,
            VehicleModelRepository vehicleModelRepository,
            VehicleGenerationRepository vehicleGenerationRepository
    ) {
        this.vehicleMakeRepository = vehicleMakeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleGenerationRepository = vehicleGenerationRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleMakeDto> catalog() {
        List<VehicleMake> makes = vehicleMakeRepository.findAllByOrderByNameAsc();
        List<VehicleModel> models = vehicleModelRepository.findAllByOrderByNameAsc();
        List<VehicleGeneration> generations = vehicleGenerationRepository.findAllByOrderByYearFromAscNameAsc();

        Map<Long, List<VehicleModel>> modelsByMakeId = models.stream()
                .collect(Collectors.groupingBy(m -> m.getMake().getId()));
        Map<Long, List<VehicleGeneration>> generationsByModelId = generations.stream()
                .collect(Collectors.groupingBy(g -> g.getModel().getId()));

        List<VehicleMakeDto> result = new ArrayList<>();
        for (VehicleMake make : makes) {
            List<VehicleModelDto> modelDtos = modelsByMakeId.getOrDefault(make.getId(), List.of()).stream()
                    .map(model -> new VehicleModelDto(
                            model.getId(),
                            model.getName(),
                            generationsByModelId.getOrDefault(model.getId(), List.of()).stream()
                                    .map(generation -> new VehicleGenerationDto(
                                            generation.getId(),
                                            generation.getName(),
                                            generation.getYearFrom(),
                                            generation.getYearTo()
                                    ))
                                    .toList()
                    ))
                    .toList();
            result.add(new VehicleMakeDto(make.getId(), make.getName(), modelDtos));
        }
        return result;
    }
}
