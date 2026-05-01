package ru.razborka.marketplace.user.service;

import ru.razborka.marketplace.user.web.dto.UpdateProfileRequest;
import ru.razborka.marketplace.user.web.dto.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getMe();

    UserProfileDto updateMe(UpdateProfileRequest req);
}
