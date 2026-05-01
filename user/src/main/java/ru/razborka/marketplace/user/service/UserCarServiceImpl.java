package ru.razborka.marketplace.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.domain.UserCar;
import ru.razborka.marketplace.user.repository.UserCarRepository;
import ru.razborka.marketplace.user.repository.UserRepository;
import ru.razborka.marketplace.user.web.dto.UpsertUserCarRequest;
import ru.razborka.marketplace.user.web.dto.UserCarDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserCarServiceImpl implements UserCarService {

    private final UserCarRepository userCarRepository;
    private final UserRepository userRepository;

    public UserCarServiceImpl(UserCarRepository userCarRepository, UserRepository userRepository) {
        this.userCarRepository = userCarRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCarDto> myCars() {
        long userId = SecurityUtils.requireUserId();
        return userCarRepository.findByUserIdOrderByActiveDescUpdatedAtDescCreatedAtDesc(userId)
                .stream()
                .map(UserCarServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserCarDto createCar(UpsertUserCarRequest request) {
        long userId = SecurityUtils.requireUserId();
        UserCar car = new UserCar();
        car.setUser(userRepository.getReferenceById(userId));
        applyRequest(car, request);
        car.setCreatedAt(Instant.now());
        car.setUpdatedAt(Instant.now());
        boolean noActive = userCarRepository.findByUserIdAndActiveTrue(userId).isEmpty();
        car.setActive(noActive);
        car = userCarRepository.save(car);
        return toDto(car);
    }

    @Override
    @Transactional
    public UserCarDto updateCar(Long carId, UpsertUserCarRequest request) {
        long userId = SecurityUtils.requireUserId();
        UserCar car = userCarRepository.findByIdAndUserId(carId, userId)
                .orElseThrow(() -> new NotFoundException("Автомобиль не найден"));
        applyRequest(car, request);
        car.setUpdatedAt(Instant.now());
        car = userCarRepository.save(car);
        return toDto(car);
    }

    @Override
    @Transactional
    public void deleteCar(Long carId) {
        long userId = SecurityUtils.requireUserId();
        UserCar car = userCarRepository.findByIdAndUserId(carId, userId)
                .orElseThrow(() -> new NotFoundException("Автомобиль не найден"));
        boolean wasActive = car.isActive();
        userCarRepository.delete(car);
        if (wasActive) {
            userCarRepository.findByUserIdOrderByActiveDescUpdatedAtDescCreatedAtDesc(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setActive(true);
                        next.setUpdatedAt(Instant.now());
                        userCarRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional
    public UserCarDto setActiveCar(Long carId) {
        long userId = SecurityUtils.requireUserId();
        UserCar car = userCarRepository.findByIdAndUserId(carId, userId)
                .orElseThrow(() -> new NotFoundException("Автомобиль не найден"));
        userCarRepository.resetActiveForUser(userId);
        car.setActive(true);
        car.setUpdatedAt(Instant.now());
        car = userCarRepository.save(car);
        return toDto(car);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCar> activeCarForUser(Long userId) {
        return userCarRepository.findByUserIdAndActiveTrue(userId);
    }

    private static void applyRequest(UserCar car, UpsertUserCarRequest request) {
        car.setDisplayName(request.displayName().trim());
        car.setBrand(request.brand().trim());
        car.setModel(request.model().trim());
        car.setGeneration(normalizeNullable(request.generation()));
        car.setYear(request.year());
        car.setEngineVolume(request.engineVolume());
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static UserCarDto toDto(UserCar car) {
        return new UserCarDto(
                car.getId(),
                car.getDisplayName(),
                car.getBrand(),
                car.getModel(),
                car.getGeneration(),
                car.getYear(),
                car.getEngineVolume(),
                car.isActive(),
                car.getCreatedAt(),
                car.getUpdatedAt()
        );
    }
}
