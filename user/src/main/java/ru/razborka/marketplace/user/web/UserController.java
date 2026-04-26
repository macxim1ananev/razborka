package ru.razborka.marketplace.user.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.razborka.marketplace.user.service.UserProfileService;
import ru.razborka.marketplace.user.web.dto.UpdateProfileRequest;
import ru.razborka.marketplace.user.web.dto.UserProfileDto;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfileDto getMe() {
        return userProfileService.getMe();
    }

    @PutMapping
    public UserProfileDto updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateMe(request);
    }
}
