package com.unity.goods.infra.service;

import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;

import com.unity.goods.domain.goods.dto.ClusterDto;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.infra.document.GoodsDocument;
import com.unity.goods.infra.dto.SearchDto.SearchedGoods;
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

  public SearchHits<GoodsDocument> findByGeoLocationOrderByLikes(double lng, double lat, Pageable pageable) {

    GeoDistanceQueryBuilder queryBuilder = QueryBuilders.geoDistanceQuery("location")
        .point(lng, lat)
        .distance(SEARCH_DISTANCE, DistanceUnit.KILOMETERS);

    SortBuilder<?> sortBuilder = SortBuilders.fieldSort("likes").order(SortOrder.DESC);

    Query searchQuery = new NativeSearchQueryBuilder()
        .withQuery(queryBuilder)
        .withSort(sortBuilder)
        .withPageable(pageable)
        .build();

    return elasticsearchOperations.search(searchQuery,
        GoodsDocument.class);
  }

  public void updateGoodsLikes(Long goodsId, int change) {
    GoodsDocument document = goodsSearchRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    document.setLikes(document.getLikes() + change);

    // Elasticsearch 문서 변환
    Document esDocument = Document.create();
    esDocument.put("likes", document.getLikes());

    // 업데이트 쿼리 생성
    UpdateQuery updateQuery = UpdateQuery.builder(document.getId().toString())
        .withDocument(esDocument)
        .withDocAsUpsert(true)
        .build();

    // Elasticsearch 업데이트
    elasticsearchOperations.update(updateQuery, IndexCoordinates.of("keywords"));
  }

  public void deleteGoodsDocument(String index, String goodsId) {
    IndexCoordinates indexCoordinates = IndexCoordinates.of(index);
    elasticsearchOperations.delete(goodsId, indexCoordinates);
  }

  public SearchHits<GoodsDocument> findByGeoLocationOrderByLikes(ClusterDto clusterDto,
      Pageable pageable) {

    Query searchQuery = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.geoDistanceQuery("location")
            .point(clusterDto.getBaseLat(), clusterDto.getBaseLng())
            .distance(calculateDistance(clusterDto), DistanceUnit.KILOMETERS))
        .withSort(SortBuilders.fieldSort("likes").order(SortOrder.DESC))
        .withPageable(pageable)
        .build();

    return elasticsearchOperations.search(searchQuery, GoodsDocument.class);
  }

  private double calculateDistance(ClusterDto clusterDto) {
    final double EARTH_RADIUS = 6371.0;
    double lat1 = clusterDto.getNeLat();
    double lng1 = clusterDto.getNeLng();
    double lat2 = clusterDto.getSwLat();
    double lng2 = clusterDto.getSwLng();

    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c / 2;
  }

}
