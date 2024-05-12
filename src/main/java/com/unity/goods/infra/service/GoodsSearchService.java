package com.unity.goods.infra.service;

import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.infra.document.GoodsDocument;
import com.unity.goods.infra.dto.SearchedGoods;
import com.unity.goods.infra.repository.GoodsSearchRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoodsSearchService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final GoodsSearchRepository goodsSearchRepository;

  private static final double SEARCH_DISTANCE = 2.0;

  public void saveGoods(Goods goods, String thumbnailUrl) {
    elasticsearchOperations.save(GoodsDocument.fromGoods(goods, thumbnailUrl));
  }

  public Page<SearchedGoods> search(String keyword, Pageable pageable) {

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

    List<SearchedGoods> searchedGoods = new ArrayList<>();
    long total = 0;

    // 모든 SearchHits 처리
    for (SearchHits<GoodsDocument> searchHits : searchHitsList) {
      total += searchHits.getTotalHits(); // 총 검색 결과 수 업데이트
      searchHits.getSearchHits().forEach(
          searchHit -> searchedGoods.add(SearchedGoods.fromGoodsDocument(searchHit.getContent())));
    }

    return new PageImpl<>(searchedGoods, pageable, total);
  }

  public Page<SearchedGoods> findByGeoLocationOrderByLikes(double lng, double lat, Pageable pageable) {

    GeoDistanceQueryBuilder queryBuilder = QueryBuilders.geoDistanceQuery("location")
        .point(lng, lat)
        .distance(SEARCH_DISTANCE, DistanceUnit.KILOMETERS);

    SortBuilder<?> sortBuilder = SortBuilders.fieldSort("likes").order(SortOrder.DESC);

    Query searchQuery = new NativeSearchQueryBuilder()
        .withQuery(queryBuilder)
        .withSort(sortBuilder)
        .withPageable(pageable)
        .build();

    SearchHits<GoodsDocument> searchHits = elasticsearchOperations.search(searchQuery,
        GoodsDocument.class);

    List<SearchedGoods> searchedGoods = new ArrayList<>();
    searchHits.getSearchHits().forEach(
        searchHit -> searchedGoods.add(SearchedGoods.fromGoodsDocument(searchHit.getContent())));

    return new PageImpl<>(searchedGoods, pageable, searchHits.getTotalHits());
  }

  public void updateGoodsLikes(Long goodsId, int change) {
    GoodsDocument document = goodsSearchRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    document.setLikes(document.getLikes() + change);
    Document updateDocument = elasticsearchOperations.getElasticsearchConverter()
        .mapObject(document);

    elasticsearchOperations.update(UpdateQuery.builder(document.getId().toString())
        .withDocument(updateDocument)
        .withDocAsUpsert(true)
        .build(), IndexCoordinates.of("keyword"));
  }
}
