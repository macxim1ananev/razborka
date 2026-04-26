package ru.razborka.marketplace.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.repository.UserRepository;
import ru.razborka.marketplace.user.web.dto.UpdateProfileRequest;
import ru.razborka.marketplace.user.web.dto.UserProfileDto;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getMe() {
        long uid = SecurityUtils.requireUserId();
        User u = userRepository.findById(uid).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return toDto(u);
    }

    @Transactional
    public UserProfileDto updateMe(UpdateProfileRequest req) {
        long uid = SecurityUtils.requireUserId();
        User u = userRepository.findById(uid).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (req.phone() != null) {
            u.setPhone(req.phone());
        }
        if (req.city() != null) {
            u.setCity(req.city());
        }
        if (req.bio() != null) {
            u.setBio(req.bio());
        }
        u.setLastOnline(Instant.now());
        u = userRepository.save(u);
        return toDto(u);
    }

    private static UserProfileDto toDto(User u) {
        return new UserProfileDto(
                u.getId(),
                u.getTelegramId(),
                Optional.ofNullable(u.getUsername()).orElse(""),
                Optional.ofNullable(u.getFirstName()).orElse(""),
                Optional.ofNullable(u.getPhone()).orElse(""),
                Optional.ofNullable(u.getAvatarUrl()).orElse(""),
                Optional.ofNullable(u.getCity()).orElse(""),
                Optional.ofNullable(u.getBio()).orElse(""),
                u.getStatus().name(),
                u.getCreatedAt(),
                u.getLastOnline()
        );
    }
}
