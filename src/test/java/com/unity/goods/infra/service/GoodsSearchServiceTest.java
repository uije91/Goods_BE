package com.unity.goods.infra.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unity.goods.infra.document.GoodsDocument;
import com.unity.goods.infra.dto.SearchedGoods;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;
import org.springframework.data.elasticsearch.core.query.Query;

@ExtendWith(MockitoExtension.class)
class GoodsSearchServiceTest {

  @Mock
  private ElasticsearchOperations elasticsearchOperations;

  @InjectMocks
  private GoodsSearchService goodsSearchService;

  @Test
  void testSearchGoods() {
    // 검색 키워드
    String keyword = "텀블러";
    Pageable pageable = PageRequest.of(0, 10);

    // 목 데이터 설정
    GoodsDocument mockDocument1 = new GoodsDocument();
    mockDocument1.setGoodsName("담요");
    mockDocument1.setDescription("거의 안 씀");

    GoodsDocument mockDocument2 = new GoodsDocument();
    mockDocument2.setGoodsName("텀블러");
    mockDocument2.setDescription("신상 한 번도 안 씀!~");

    GoodsDocument mockDocument3 = new GoodsDocument();
    mockDocument3.setGoodsName("스타벅스 텀블러");
    mockDocument3.setDescription("2022ss 버전");

    SearchHit<GoodsDocument> searchHit1 = mock(SearchHit.class);
    SearchHit<GoodsDocument> searchHit2 = mock(SearchHit.class);
    when(searchHit1.getContent()).thenReturn(mockDocument1);
    when(searchHit2.getContent()).thenReturn(mockDocument2);

    List<SearchHit<GoodsDocument>> hits = Arrays.asList(searchHit1, searchHit2);
    SearchHits<GoodsDocument> searchHits = new SearchHitsImpl<>(
        2L, TotalHitsRelation.EQUAL_TO, 1.0f, null, hits, null);

    // ElasticsearchOperations 모킹
    when(elasticsearchOperations.multiSearch(any(List.class), eq(GoodsDocument.class)))
        .thenReturn(List.of(searchHits));

    // 검색 실행
    Page<SearchedGoods> results = goodsSearchService.search(keyword, pageable);

    // 결과 검증
    assertNotNull(results);
    assertEquals(2, results.getTotalElements());
    assertTrue(results.getContent().stream().anyMatch(doc -> doc.getGoodsName().contains("텀블러")));
  }

  @Test
  @DisplayName("내 위치 반경 2km 내 상품 조회 + 인기 매물 순서로")
  public void testFindNearbyGoods() {
    GoodsDocument goods1 = GoodsDocument.builder()
        .id(1L)
        .location(new GeoPoint(37.56464351273438, 126.97715880918653))
        .address("서울 시청역 1번 출구")
        .likes(10L)
        .build();
    GoodsDocument goods2 = GoodsDocument.builder()
        .id(2L)
        .location(new GeoPoint(37.5610041760916, 126.9810533728523))
        .address("서울 중구 신세계 백화점")
        .likes(20L)
        .build();
    GoodsDocument goods3 = GoodsDocument.builder()
        .id(3L)
        .location(new GeoPoint(37.558388822601664, 126.80191630346101))
        .address("경기도 김포 국제 공항")
        .likes(35L)
        .build();

    // 서울 시청 바로 옆
    double myPlaceLat = 37.56565278110061;
    double myPlaceLng = 126.97796214146766;

    SearchHit<GoodsDocument> searchHit1 = mock(SearchHit.class);
    SearchHit<GoodsDocument> searchHit2 = mock(SearchHit.class);
    when(searchHit1.getContent()).thenReturn(goods2);
    when(searchHit2.getContent()).thenReturn(goods1);

    List<SearchHit<GoodsDocument>> hits = Arrays.asList(searchHit1, searchHit2);
    SearchHits<GoodsDocument> searchHits = new SearchHitsImpl<>(
        2L, TotalHitsRelation.EQUAL_TO, 1.0f, null, hits, null);

    when(elasticsearchOperations.search(any(Query.class), eq(GoodsDocument.class))).thenReturn(
        searchHits);

    Page<SearchedGoods> result = goodsSearchService.findByGeoLocationOrderByLikes(
        myPlaceLng, myPlaceLat, PageRequest.of(0, 20));

    // 결과 검증
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.getTotalElements());
    // 반경 2km 내, 인기 상품 : goods2 우선 출력
    assertEquals(goods2.getId(), result.getContent().get(0).getGoodsId());
    assertEquals(goods1.getId(), result.getContent().get(1).getGoodsId());
  }

}