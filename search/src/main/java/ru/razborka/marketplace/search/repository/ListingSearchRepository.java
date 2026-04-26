package ru.razborka.marketplace.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.razborka.marketplace.search.document.ListingSearchDocument;

public interface ListingSearchRepository extends ElasticsearchRepository<ListingSearchDocument, Long> {
}
