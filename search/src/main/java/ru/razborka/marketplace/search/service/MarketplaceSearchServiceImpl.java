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
import ru.razborka.marketplace.listing.media.ListingMediaDeliveryService;
import ru.razborka.marketplace.search.document.ListingSearchDocument;
import ru.razborka.marketplace.search.web.dto.FacetBucketDto;
import ru.razborka.marketplace.search.web.dto.SearchHitDto;
import ru.razborka.marketplace.search.web.dto.SearchResponseDto;

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

    public MarketplaceSearchServiceImpl(
            ElasticsearchOperations operations,
            ListingMediaDeliveryService mediaDeliveryService
    ) {
        this.operations = operations;
        this.mediaDeliveryService = mediaDeliveryService;
    }

    @Override
    @Cacheable(value = "searchResults", key = "#params.cacheKey()", unless = "#result == null")
    public SearchResponseDto search(SearchParams params) {
        Pageable pageable = PageRequest.of(params.page(), params.size());
        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.must(m -> m.term(t -> t.field("status").value("active")));

        if (params.categoryId() != null) {
            bool.must(m -> m.term(t -> t.field("categoryId").value(params.categoryId())));
        }
        if (params.brand() != null && !params.brand().isBlank()) {
            bool.must(m -> m.term(t -> t.field("brands").value(params.brand())));
        }
        if (params.model() != null && !params.model().isBlank()) {
            bool.must(m -> m.term(t -> t.field("models").value(params.model())));
        }
        if (params.generation() != null && !params.generation().isBlank()) {
            bool.must(m -> m.term(t -> t.field("generations").value(params.generation())));
        }
        if (params.vin() != null && !params.vin().isBlank()) {
            bool.must(m -> m.term(t -> t.field("vin").value(params.vin().trim().toUpperCase())));
        }
        if (params.catalogBlock() != null && !params.catalogBlock().isBlank()) {
            bool.must(m -> m.term(t -> t.field("catalogBlock").value(params.catalogBlock())));
        }
        if (params.yearFrom() != null) {
            bool.must(m -> m.range(r -> r.field("compatYearToMax").gte(JsonData.of(params.yearFrom()))));
        }
        if (params.yearTo() != null) {
            bool.must(m -> m.range(r -> r.field("compatYearFromMin").lte(JsonData.of(params.yearTo()))));
        }
        if (params.engineVolume() != null) {
            double v = params.engineVolume();
            bool.must(m -> m.exists(e -> e.field("compatEngineVolMin")));
            bool.must(m -> m.exists(e -> e.field("compatEngineVolMax")));
            bool.must(m -> m.range(r -> r.field("compatEngineVolMin").lte(JsonData.of(v))));
            bool.must(m -> m.range(r -> r.field("compatEngineVolMax").gte(JsonData.of(v))));
        }
        if (params.partCondition() != null && !params.partCondition().isBlank()) {
            bool.must(m -> m.term(t -> t.field("partCondition").value(params.partCondition())));
        }
        if (params.originalReplica() != null && !params.originalReplica().isBlank()) {
            bool.must(m -> m.term(t -> t.field("originalReplica").value(params.originalReplica())));
        }
        if (params.priceMin() != null) {
            bool.must(m -> m.range(r -> r.field("price").gte(JsonData.of(params.priceMin().doubleValue()))));
        }
        if (params.priceMax() != null) {
            bool.must(m -> m.range(r -> r.field("price").lte(JsonData.of(params.priceMax().doubleValue()))));
        }
        if (params.city() != null && !params.city().isBlank()) {
            bool.must(m -> m.term(t -> t.field("sellerCity").value(params.city())));
        }
        if (params.q() != null && !params.q().isBlank()) {
            bool.must(m -> m.match(mm -> mm.field("title").query(params.q())));
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
                return new SearchResponseDto(List.of(), 0, params.page(), params.size(), Map.of());
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
                params.page(),
                params.size(),
                facets
        );
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
