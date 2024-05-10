package com.unity.goods.infra.repository;

import com.unity.goods.infra.document.GoodsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GoodsSearchRepository extends ElasticsearchRepository<GoodsDocument, Long> {
}
