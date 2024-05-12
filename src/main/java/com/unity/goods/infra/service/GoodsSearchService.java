package com.unity.goods.infra.service;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.infra.document.GoodsDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoodsSearchService {

  private final ElasticsearchOperations elasticsearchOperations;

  public void saveGoods(Goods goods, String thumbnailUrl) {
      elasticsearchOperations.save(GoodsDocument.fromGoods(goods, thumbnailUrl));
  }

  public Page<GoodsDocument> search(String keyword, Pageable pageable) {

    Query searchQuery = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.queryStringQuery("*" + keyword + "*")
            .field("goodsName")
            .field("description")
            .field("goodsStatus")
            .field("address"))
        .withPageable(pageable)
        .build();
    List<Query> queries = new ArrayList<>();
    queries.add(searchQuery);

    List<SearchHits<GoodsDocument>> searchHitsList = elasticsearchOperations.multiSearch(queries,
        GoodsDocument.class);

    List<GoodsDocument> goodsDocuments = new ArrayList<>();
    long total = 0;

    // 모든 SearchHits 처리
    for (SearchHits<GoodsDocument> searchHits : searchHitsList) {
      total += searchHits.getTotalHits(); // 총 검색 결과 수 업데이트
      searchHits.getSearchHits().forEach(searchHit -> goodsDocuments.add(searchHit.getContent()));
    }

    return new PageImpl<>(goodsDocuments, pageable, total);
  }
}
