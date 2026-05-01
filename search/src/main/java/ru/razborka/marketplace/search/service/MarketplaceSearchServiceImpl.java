package ru.razborka.marketplace.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import ru.razborka.marketplace.common.security.SecurityUtils;
import ru.razborka.marketplace.listing.media.ListingMediaDeliveryService;
import ru.razborka.marketplace.search.document.ListingSearchDocument;
import ru.razborka.marketplace.search.web.dto.FacetBucketDto;
import ru.razborka.marketplace.search.web.dto.SearchHitDto;
import ru.razborka.marketplace.search.web.dto.SearchResponseDto;
import ru.razborka.marketplace.user.domain.UserCar;
import ru.razborka.marketplace.user.repository.UserCarRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketplaceSearchServiceImpl implements MarketplaceSearchService {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceSearchService.class);

    private final ElasticsearchOperations operations;
    private final ListingMediaDeliveryService mediaDeliveryService;
    private final UserCarRepository userCarRepository;

    public MarketplaceSearchServiceImpl(
            ElasticsearchOperations operations,
            ListingMediaDeliveryService mediaDeliveryService,
            UserCarRepository userCarRepository
    ) {
        this.operations = operations;
        this.mediaDeliveryService = mediaDeliveryService;
        this.userCarRepository = userCarRepository;
    }

    @Override
    @Cacheable(
            value = "searchResults",
            key = "#params.cacheKey()",
            unless = "#result == null",
            condition = "T(ru.razborka.marketplace.common.security.SecurityUtils).currentUserId().isEmpty()"
    )
    public SearchResponseDto search(SearchParams params) {
        SearchParams effective = applyActiveCarDefaults(params);
        Pageable pageable = PageRequest.of(effective.page(), effective.size());
        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.must(m -> m.term(t -> t.field("status").value("active")));

        if (effective.categoryId() != null) {
            bool.must(m -> m.term(t -> t.field("categoryId").value(effective.categoryId())));
        }
        if (effective.brand() != null && !effective.brand().isBlank()) {
            bool.must(m -> m.term(t -> t.field("brands").value(effective.brand())));
        }
        if (effective.model() != null && !effective.model().isBlank()) {
            bool.must(m -> m.term(t -> t.field("models").value(effective.model())));
        }
        if (effective.generation() != null && !effective.generation().isBlank()) {
            bool.must(m -> m.term(t -> t.field("generations").value(effective.generation())));
        }
        if (effective.vin() != null && !effective.vin().isBlank()) {
            bool.must(m -> m.term(t -> t.field("vin").value(effective.vin().trim().toUpperCase())));
        }
        if (effective.catalogBlock() != null && !effective.catalogBlock().isBlank()) {
            bool.must(m -> m.term(t -> t.field("catalogBlock").value(effective.catalogBlock())));
        }
        if (effective.yearFrom() != null) {
            bool.must(m -> m.range(r -> r.field("compatYearToMax").gte(JsonData.of(effective.yearFrom()))));
        }
        if (effective.yearTo() != null) {
            bool.must(m -> m.range(r -> r.field("compatYearFromMin").lte(JsonData.of(effective.yearTo()))));
        }
        if (effective.engineVolume() != null) {
            double v = effective.engineVolume();
            bool.must(m -> m.exists(e -> e.field("compatEngineVolMin")));
            bool.must(m -> m.exists(e -> e.field("compatEngineVolMax")));
            bool.must(m -> m.range(r -> r.field("compatEngineVolMin").lte(JsonData.of(v))));
            bool.must(m -> m.range(r -> r.field("compatEngineVolMax").gte(JsonData.of(v))));
        }
        if (effective.partCondition() != null && !effective.partCondition().isBlank()) {
            bool.must(m -> m.term(t -> t.field("partCondition").value(effective.partCondition())));
        }
        if (effective.originalReplica() != null && !effective.originalReplica().isBlank()) {
            bool.must(m -> m.term(t -> t.field("originalReplica").value(effective.originalReplica())));
        }
        if (effective.priceMin() != null) {
            bool.must(m -> m.range(r -> r.field("price").gte(JsonData.of(effective.priceMin().doubleValue()))));
        }
        if (effective.priceMax() != null) {
            bool.must(m -> m.range(r -> r.field("price").lte(JsonData.of(effective.priceMax().doubleValue()))));
        }
        if (effective.city() != null && !effective.city().isBlank()) {
            bool.must(m -> m.term(t -> t.field("sellerCity").value(effective.city())));
        }
        if (effective.q() != null && !effective.q().isBlank()) {
            bool.must(m -> m.match(mm -> mm.field("title").query(effective.q())));
        }

        Query root = Query.of(q -> q.bool(bool.build()));

        NativeQuery query = NativeQuery.builder()
                .withQuery(root)
                .withPageable(pageable)
                .withAggregation("categoryId", Aggregation.of(a -> a.terms(t -> t.field("categoryId").size(300))))
                .withAggregation("brands", Aggregation.of(a -> a.terms(t -> t.field("brands").size(100))))
                .withAggregation("models", Aggregation.of(a -> a.terms(t -> t.field("models").size(100))))
                .withAggregation("generations", Aggregation.of(a -> a.terms(t -> t.field("generations").size(120))))
                .withAggregation("catalogBlock", Aggregation.of(a -> a.terms(t -> t.field("catalogBlock").size(20))))
                .withAggregation("partCondition", Aggregation.of(a -> a.terms(t -> t.field("partCondition").size(20))))
                .withAggregation("originalReplica", Aggregation.of(a -> a.terms(t -> t.field("originalReplica").size(20))))
                .withAggregation("sellerCity", Aggregation.of(a -> a.terms(t -> t.field("sellerCity").size(100))))
                .withAggregation("priceRanges", Aggregation.of(a -> a.histogram(h -> h.field("price").interval(5000.0))))
                .build();

        SearchHits<ListingSearchDocument> hits;
        try {
            hits = operations.search(query, ListingSearchDocument.class);
        } catch (Exception ex) {
            log.warn("Search with facets failed, fallback to hits-only: {}", ex.getMessage());
            try {
                NativeQuery fallbackQuery = NativeQuery.builder()
                        .withQuery(root)
                        .withPageable(pageable)
                        .build();
                hits = operations.search(fallbackQuery, ListingSearchDocument.class);
            } catch (Exception fallbackEx) {
                log.error("Search fallback failed: {}", fallbackEx.getMessage());
                return new SearchResponseDto(List.of(), 0, effective.page(), effective.size(), Map.of());
            }
        }
        List<SearchHitDto> content = new ArrayList<>();
        hits.forEach(h -> {
            ListingSearchDocument d = h.getContent();
            content.add(new SearchHitDto(
                    d.getId(),
                    d.getTitle(),
                    BigDecimal.valueOf(d.getPrice()).setScale(2, RoundingMode.HALF_UP),
                    mediaDeliveryService.toPublicUrl(Optional.ofNullable(d.getFirstPhotoUrl()).orElse("")),
                    Optional.ofNullable(d.getSellerCity()).orElse(""),
                    d.getVehicleYear(),
                    d.getMileageKm(),
                    Optional.ofNullable(d.getPartCondition()).orElse(""),
                    Optional.ofNullable(d.getOriginalReplica()).orElse(""),
                    d.getCategoryId()
            ));
        });

        Map<String, List<FacetBucketDto>> facets = new HashMap<>();
        var container = hits.getAggregations();
        if (container instanceof ElasticsearchAggregations eas) {
            putLongTerms(eas, "categoryId", facets);
            putStringTerms(eas, "brands", facets);
            putStringTerms(eas, "models", facets);
            putStringTerms(eas, "generations", facets);
            putStringTerms(eas, "catalogBlock", facets);
            putStringTerms(eas, "partCondition", facets);
            putStringTerms(eas, "originalReplica", facets);
            putStringTerms(eas, "sellerCity", facets);
            putHistogram(eas, "priceRanges", facets);
        }

        return new SearchResponseDto(
                content,
                hits.getTotalHits(),
                effective.page(),
                effective.size(),
                facets
        );
    }

    private SearchParams applyActiveCarDefaults(SearchParams params) {
        var userId = SecurityUtils.currentUserId();
        if (userId.isEmpty()) {
            return params;
        }
        var activeCar = userCarRepository.findByUserIdAndActiveTrue(userId.get());
        if (activeCar.isEmpty()) {
            return params;
        }
        UserCar car = activeCar.get();
        String brand = hasText(params.brand()) ? params.brand() : car.getBrand();
        String model = hasText(params.model()) ? params.model() : car.getModel();
        String generation = hasText(params.generation()) ? params.generation() : car.getGeneration();
        Integer yearFrom = params.yearFrom() != null ? params.yearFrom() : car.getYear();
        Integer yearTo = params.yearTo() != null ? params.yearTo() : car.getYear();
        Double engineVolume = params.engineVolume();
        if (engineVolume == null && car.getEngineVolume() != null) {
            engineVolume = car.getEngineVolume().doubleValue();
        }
        return new SearchParams(
                params.categoryId(),
                brand,
                model,
                generation,
                params.vin(),
                params.catalogBlock(),
                yearFrom,
                yearTo,
                engineVolume,
                params.partCondition(),
                params.originalReplica(),
                params.priceMin(),
                params.priceMax(),
                params.city(),
                params.q(),
                params.page(),
                params.size()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void putLongTerms(ElasticsearchAggregations eas, String name, Map<String, List<FacetBucketDto>> facets) {
        ElasticsearchAggregation agg = eas.get(name);
        if (agg == null) {
            return;
        }
        Aggregate aggregate = agg.aggregation().getAggregate();
        List<FacetBucketDto> buckets = new ArrayList<>();
        if (aggregate.isLterms()) {
            for (LongTermsBucket b : aggregate.lterms().buckets().array()) {
                buckets.add(new FacetBucketDto(String.valueOf(b.key()), b.docCount()));
            }
        } else if (aggregate.isSterms()) {
            for (StringTermsBucket b : aggregate.sterms().buckets().array()) {
                buckets.add(new FacetBucketDto(termsKeyToString(b), b.docCount()));
            }
        }
        if (!buckets.isEmpty()) {
            facets.put(name, buckets);
        }
    }

    private static void putStringTerms(ElasticsearchAggregations eas, String name, Map<String, List<FacetBucketDto>> facets) {
        ElasticsearchAggregation agg = eas.get(name);
        if (agg == null) {
            return;
        }
        Aggregate aggregate = agg.aggregation().getAggregate();
        if (!aggregate.isSterms()) {
            return;
        }
        List<FacetBucketDto> buckets = new ArrayList<>();
        for (StringTermsBucket b : aggregate.sterms().buckets().array()) {
            buckets.add(new FacetBucketDto(termsKeyToString(b), b.docCount()));
        }
        facets.put(name, buckets);
    }

    private static String termsKeyToString(StringTermsBucket b) {
        FieldValue fv = b.key();
        if (fv.isString()) {
            return fv.stringValue();
        }
        if (fv.isLong()) {
            return String.valueOf(fv.longValue());
        }
        if (fv.isDouble()) {
            return String.valueOf(fv.doubleValue());
        }
        return String.valueOf(fv);
    }

    private static void putHistogram(ElasticsearchAggregations eas, String name, Map<String, List<FacetBucketDto>> facets) {
        ElasticsearchAggregation agg = eas.get(name);
        if (agg == null) {
            return;
        }
        Aggregate aggregate = agg.aggregation().getAggregate();
        if (!aggregate.isHistogram()) {
            return;
        }
        List<FacetBucketDto> buckets = new ArrayList<>();
        for (var b : aggregate.histogram().buckets().array()) {
            buckets.add(new FacetBucketDto(String.valueOf(b.key()), b.docCount()));
        }
        facets.put(name, buckets);
    }
}
