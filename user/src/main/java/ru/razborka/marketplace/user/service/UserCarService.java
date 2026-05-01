package ru.razborka.marketplace.user.service;

import ru.razborka.marketplace.user.domain.UserCar;
import ru.razborka.marketplace.user.web.dto.UpsertUserCarRequest;
import ru.razborka.marketplace.user.web.dto.UserCarDto;

import java.util.List;
import java.util.Optional;

public interface UserCarService {
    List<UserCarDto> myCars();

    UserCarDto createCar(UpsertUserCarRequest request);

    UserCarDto updateCar(Long carId, UpsertUserCarRequest request);

    void deleteCar(Long carId);

    UserCarDto setActiveCar(Long carId);

    Optional<UserCar> activeCarForUser(Long userId);
}
