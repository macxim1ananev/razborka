package ru.razborka.marketplace.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.razborka.marketplace.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramId(Long telegramId);
}
