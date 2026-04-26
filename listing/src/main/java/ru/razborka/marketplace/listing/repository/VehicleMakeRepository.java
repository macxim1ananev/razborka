package ru.razborka.marketplace.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.listing.domain.VehicleMake;

import java.util.List;

public interface VehicleMakeRepository extends JpaRepository<VehicleMake, Long> {

    List<VehicleMake> findAllByOrderByNameAsc();
}
