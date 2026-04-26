package ru.razborka.marketplace.listing.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.common.exception.ForbiddenException;
import ru.razborka.marketplace.common.exception.NotFoundException;
import ru.razborka.marketplace.listing.domain.Category;
import ru.razborka.marketplace.listing.domain.Listing;
import ru.razborka.marketplace.listing.domain.ListingCompatibility;
import ru.razborka.marketplace.listing.domain.ListingPhoto;
import ru.razborka.marketplace.listing.domain.ListingStatus;
import ru.razborka.marketplace.listing.event.ListingIndexEvent;
import ru.razborka.marketplace.listing.event.ListingIndexEvent.ListingIndexAction;
import ru.razborka.marketplace.listing.repository.CategoryRepository;
import ru.razborka.marketplace.listing.repository.ListingRepository;
import ru.razborka.marketplace.listing.media.ListingMediaDeliveryService;
import ru.razborka.marketplace.listing.storage.StorageService;
import ru.razborka.marketplace.listing.web.dto.CompatibilityDto;
import ru.razborka.marketplace.listing.web.dto.CompatibilityWriteDto;
import ru.razborka.marketplace.listing.web.dto.CreateListingRequest;
import ru.razborka.marketplace.listing.web.dto.ListingDetailDto;
import ru.razborka.marketplace.listing.web.dto.ListingPreviewDto;
import ru.razborka.marketplace.listing.web.dto.SellerContactDto;
import ru.razborka.marketplace.user.domain.User;
import ru.razborka.marketplace.user.repository.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ListingMediaDeliveryService mediaDeliveryService;
    private final ApplicationEventPublisher eventPublisher;

    public ListingService(
            ListingRepository listingRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            StorageService storageService,
            ListingMediaDeliveryService mediaDeliveryService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.mediaDeliveryService = mediaDeliveryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "listingPreviews", key = "#page + '-' + #size", unless = "#result == null", condition = "#page < 5")
    public Page<ListingPreviewDto> listActivePreviewsForPublic(int page, int size) {
        Pageable p = PageRequest.of(page, size);
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.active, p).map(this::toPreviewDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "listingDetails", key = "#id", unless = "#result == null")
    public ListingDetailDto getDetail(Long id) {
        Listing l = listingRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        return toDetail(l);
    }

    @Transactional(readOnly = true)
    public Page<ListingPreviewDto> myListings(int page, int size) {
        long uid = SecurityUtils.requireUserId();
        Pageable p = PageRequest.of(page, size);
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(uid, p).map(this::toPreviewDto);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listingPreviews", allEntries = true),
            @CacheEvict(value = "listingDetails", allEntries = true),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public ListingDetailDto create(CreateListingRequest req) {
        long uid = SecurityUtils.requireUserId();
        User seller = userRepository.getReferenceById(uid);
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        Listing l = new Listing();
        l.setSeller(seller);
        l.setCategory(category);
        l.setTitle(req.title());
        l.setDescription(req.description());
        l.setPrice(req.price());
        l.setCondition(req.condition());
        l.setOriginalReplica(req.originalReplica());
        String normalizedVin = normalizeVin(req.vin());
        l.setVin(normalizedVin.isEmpty() ? null : normalizedVin);
        l.setCatalogBlock(req.catalogBlock());
        l.setMileageKm(req.mileageKm());
        l.setVehicleYear(req.vehicleYear());
        l.setStatus(ListingStatus.active);
        l.setUpdatedAt(Instant.now());
        applyCompatibility(l, req.compatibility());
        l = listingRepository.save(l);
        eventPublisher.publishEvent(new ListingIndexEvent(l.getId(), ListingIndexAction.UPSERT));
        return toDetail(listingRepository.findDetailById(l.getId()).orElse(l));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listingPreviews", allEntries = true),
            @CacheEvict(value = "listingDetails", key = "#id"),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public ListingDetailDto update(Long id, CreateListingRequest req) {
        long uid = SecurityUtils.requireUserId();
        Listing l = listingRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        if (!l.getSeller().getId().equals(uid)) {
            throw new ForbiddenException("Нельзя редактировать чужое объявление");
        }
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        l.setCategory(category);
        l.setTitle(req.title());
        l.setDescription(req.description());
        l.setPrice(req.price());
        l.setCondition(req.condition());
        l.setOriginalReplica(req.originalReplica());
        String normalizedVin = normalizeVin(req.vin());
        l.setVin(normalizedVin.isEmpty() ? null : normalizedVin);
        l.setCatalogBlock(req.catalogBlock());
        l.setMileageKm(req.mileageKm());
        l.setVehicleYear(req.vehicleYear());
        l.setUpdatedAt(Instant.now());
        l.getCompatibility().clear();
        applyCompatibility(l, req.compatibility());
        l = listingRepository.save(l);
        eventPublisher.publishEvent(new ListingIndexEvent(l.getId(), ListingIndexAction.UPSERT));
        return toDetail(listingRepository.findDetailById(l.getId()).orElse(l));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listingPreviews", allEntries = true),
            @CacheEvict(value = "listingDetails", key = "#id"),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public void deleteOrArchive(Long id, boolean markSold) {
        long uid = SecurityUtils.requireUserId();
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        if (!l.getSeller().getId().equals(uid)) {
            throw new ForbiddenException("Нельзя удалить чужое объявление");
        }
        if (markSold) {
            l.setStatus(ListingStatus.sold);
        } else {
            l.setStatus(ListingStatus.archived);
        }
        l.setUpdatedAt(Instant.now());
        listingRepository.save(l);
        eventPublisher.publishEvent(new ListingIndexEvent(l.getId(), ListingIndexAction.DELETE));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listingPreviews", allEntries = true),
            @CacheEvict(value = "listingDetails", key = "#listingId"),
            @CacheEvict(value = "searchResults", allEntries = true)
    })
    public void addPhoto(Long listingId, MultipartFile file) throws IOException {
        long uid = SecurityUtils.requireUserId();
        Listing l = listingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        if (!l.getSeller().getId().equals(uid)) {
            throw new ForbiddenException("Нельзя добавить фото к чужому объявлению");
        }
        String url = storageService.store(file, "listings/" + listingId);
        ListingPhoto ph = new ListingPhoto();
        ph.setListing(l);
        ph.setPhotoUrl(url);
        int next = l.getPhotos().stream()
                .map(ListingPhoto::getSortOrder)
                .filter(o -> o != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        ph.setSortOrder(next);
        l.getPhotos().add(ph);
        l.setUpdatedAt(Instant.now());
        listingRepository.save(l);
        eventPublisher.publishEvent(new ListingIndexEvent(l.getId(), ListingIndexAction.UPSERT));
    }

    private void applyCompatibility(Listing l, List<CompatibilityWriteDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (CompatibilityWriteDto r : rows) {
            ListingCompatibility c = new ListingCompatibility();
            c.setListing(l);
            c.setBrand(r.brand());
            c.setModel(r.model());
            c.setGeneration(r.generation());
            c.setYearFrom(r.yearFrom());
            c.setYearTo(r.yearTo());
            c.setEngineVolume(r.engineVolume());
            l.getCompatibility().add(c);
        }
    }

    public ListingPreviewDto toPreviewDto(Listing l) {
        String first = l.getPhotos().stream()
                .sorted(Comparator.comparing((ListingPhoto p) -> Optional.ofNullable(p.getSortOrder()).orElse(0))
                        .thenComparing(ListingPhoto::getId, Comparator.nullsLast(Long::compareTo)))
                .map(ListingPhoto::getPhotoUrl)
                .findFirst()
                .orElse("");
        String city = Optional.ofNullable(l.getSeller().getCity()).orElse("");
        return new ListingPreviewDto(
                l.getId(),
                l.getTitle(),
                l.getPrice(),
                mediaDeliveryService.toPublicUrl(first),
                city,
                l.getVehicleYear(),
                l.getMileageKm()
        );
    }

    private ListingDetailDto toDetail(Listing l) {
        List<String> urls = l.getPhotos().stream()
                .sorted(Comparator.comparing((ListingPhoto p) -> Optional.ofNullable(p.getSortOrder()).orElse(0))
                        .thenComparing(ListingPhoto::getId))
                .map(ListingPhoto::getPhotoUrl)
                .map(mediaDeliveryService::toPublicUrl)
                .toList();
        List<CompatibilityDto> comp = l.getCompatibility().stream()
                .map(c -> new CompatibilityDto(c.getId(), c.getBrand(), c.getModel(), c.getGeneration(), c.getYearFrom(), c.getYearTo(), c.getEngineVolume()))
                .toList();
        User s = l.getSeller();
        SellerContactDto seller = new SellerContactDto(
                s.getId(),
                Optional.ofNullable(s.getUsername()).orElse(""),
                Optional.ofNullable(s.getFirstName()).orElse(""),
                Optional.ofNullable(s.getCity()).orElse("")
        );
        List<Listing> more = listingRepository.findTop4BySellerIdAndIdNotAndStatusOrderByCreatedAtDesc(
                s.getId(), l.getId(), ListingStatus.active
        );
        List<ListingPreviewDto> moreDtos = new ArrayList<>();
        for (Listing m : more) {
            moreDtos.add(toPreviewDto(m));
        }
        return new ListingDetailDto(
                l.getId(),
                l.getTitle(),
                Optional.ofNullable(l.getDescription()).orElse(""),
                l.getPrice(),
                Optional.ofNullable(l.getCondition()).orElse(""),
                Optional.ofNullable(l.getOriginalReplica()).orElse(""),
                Optional.ofNullable(l.getVin()).orElse(""),
                l.getCatalogBlock(),
                l.getStatus().name(),
                l.getVehicleYear(),
                l.getMileageKm(),
                l.getCategory().getId(),
                l.getCategory().getName(),
                urls,
                comp,
                seller,
                moreDtos,
                l.getCreatedAt(),
                l.getUpdatedAt()
        );
    }

    private static String normalizeVin(String vin) {
        if (vin == null || vin.isBlank()) {
            return "";
        }
        return vin.trim().toUpperCase();
    }
}
