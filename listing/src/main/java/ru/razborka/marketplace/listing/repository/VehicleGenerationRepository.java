package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.listing.domain.VehicleGeneration;

import java.util.List;

public interface VehicleGenerationRepository extends JpaRepository<VehicleGeneration, Long> {

    List<VehicleGeneration> findAllByOrderByYearFromAscNameAsc();
}
