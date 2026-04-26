package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.listing.domain.VehicleModel;

import java.util.List;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findAllByOrderByNameAsc();
}
