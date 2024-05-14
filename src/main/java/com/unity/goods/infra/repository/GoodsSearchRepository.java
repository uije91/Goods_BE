package com.unity.goods.infra.repository;

import com.unity.goods.infra.document.GoodsDocument;
import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, Long> {
  Optional<GoodsDocument> findById(Long id);
}
