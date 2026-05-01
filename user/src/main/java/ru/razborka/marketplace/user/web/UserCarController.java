package ru.razborka.marketplace.user.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.user.service.UserCarService;
import ru.razborka.marketplace.user.web.dto.UpsertUserCarRequest;
import ru.razborka.marketplace.user.web.dto.UserCarDto;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/cars")
public class UserCarController {

    private final UserCarService userCarService;

    public UserCarController(UserCarService userCarService) {
        this.userCarService = userCarService;
    }

    @GetMapping
    public List<UserCarDto> myCars() {
        return userCarService.myCars();
    }

    @PostMapping
    public UserCarDto create(@Valid @RequestBody UpsertUserCarRequest request) {
        return userCarService.createCar(request);
    }

    @PutMapping("/{carId}")
    public UserCarDto update(@PathVariable Long carId, @Valid @RequestBody UpsertUserCarRequest request) {
        return userCarService.updateCar(carId, request);
    }

    @DeleteMapping("/{carId}")
    public void delete(@PathVariable Long carId) {
        userCarService.deleteCar(carId);
    }

    @PostMapping("/{carId}/activate")
    public UserCarDto activate(@PathVariable Long carId) {
        return userCarService.setActiveCar(carId);
    }
}
