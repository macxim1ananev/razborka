package ru.razborka.marketplace.listing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.common.exception.BusinessException;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.listing.domain.Favorite;
import ru.razborka.marketplace.listing.domain.Listing;
import ru.razborka.marketplace.listing.domain.ListingStatus;
import ru.razborka.marketplace.listing.repository.FavoriteRepository;
import ru.razborka.marketplace.listing.repository.ListingRepository;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.repository.UserRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingService listingService;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            ListingService listingService
    ) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.listingService = listingService;
    }

    @Transactional(readOnly = true)
    public Page<ListingPreviewDto> myFavorites(int page, int size) {
        long uid = SecurityUtils.requireUserId();
        Pageable p = PageRequest.of(page, size);
        return favoriteRepository.findByIdUserIdOrderByCreatedAtDesc(uid, p)
                .map(f -> listingService.toPreviewDto(f.getListing()));
    }

    @Transactional
    public void add(Long listingId) {
        long uid = SecurityUtils.requireUserId();
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        if (listing.getStatus() != ListingStatus.active) {
            throw new BusinessException("FAVORITE", "Можно добавить только активное объявление");
        }
        if (favoriteRepository.existsByIdUserIdAndIdListingId(uid, listingId)) {
            return;
        }
        User user = userRepository.getReferenceById(uid);
        Favorite f = new Favorite();
        f.setUser(user);
        f.setListing(listing);
        favoriteRepository.save(f);
    }

    @Transactional
    public void remove(Long listingId) {
        long uid = SecurityUtils.requireUserId();
        if (!favoriteRepository.existsByIdUserIdAndIdListingId(uid, listingId)) {
            return;
        }
        Favorite.FavoriteId id = new Favorite.FavoriteId();
        id.setUserId(uid);
        id.setListingId(listingId);
        favoriteRepository.deleteById(id);
    }
}
