package ru.razborka.marketplace.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.razborka.marketplace.user.domain.UserCar;

import java.util.List;
import java.util.Optional;

public interface UserCarRepository extends JpaRepository<UserCar, Long> {

    List<UserCar> findByUserIdOrderByActiveDescUpdatedAtDescCreatedAtDesc(Long userId);

    Optional<UserCar> findByUserIdAndActiveTrue(Long userId);

    Optional<UserCar> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE UserCar c SET c.active = false WHERE c.user.id = :userId AND c.active = true")
    int resetActiveForUser(@Param("userId") Long userId);
}
