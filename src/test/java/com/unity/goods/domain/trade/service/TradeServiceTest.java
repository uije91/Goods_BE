package com.unity.goods.domain.trade.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.RESERVATION;
import static com.unity.goods.domain.goods.type.GoodsStatus.SALE;
import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.type.GoodsStatus;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeRequest;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeResponse;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto.PointTradeHistoryResponse;
import com.unity.goods.domain.trade.dto.StarRateDto.StarRateRequest;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.domain.trade.type.TradePurpose;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

  @InjectMocks
  private TradeService tradeService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private TradeRepository tradeRepository;

  @Mock
  private GoodsRepository goodsRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("거래 후 잔액 값 확인 테스트")
  void afterTradePointCheckTest() throws Exception {
    // given
    Member buyer = Member.builder()
        .id(1L)
        .email("test@email.com")
        .tradePassword("123456")
        .balance(5000L)
        .build();

    PointTradeRequest pointTradeRequest = PointTradeRequest.builder()
        .sellerId(2L)
        .goodsId(1L)
        .price("1000")
        .tradePassword("123456")
        .build();

    Member seller = Member.builder()
        .id(2L)
        .tradePassword("123456")
        .balance(0L)
        .build();

    Goods goods = Goods.builder()
        .id(1L)
        .member(seller)
        .price(1000L)
        .goodsStatus(SALE)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(buyer);

    given(memberRepository.findByEmail(any(String.class))).willReturn(Optional.of(buyer));
    given(memberRepository.findById(any(Long.class))).willReturn(Optional.of(seller));
    given(goodsRepository.findById(any(Long.class))).willReturn(Optional.of(goods));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    // when
    PointTradeResponse pointTradeResponse = tradeService.transferPoint(userDetails, pointTradeRequest);

    ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
    verify(tradeRepository, times(2)).save(captor.capture());
    List<Trade> capturedTrades = captor.getAllValues();

    // then
    assertEquals("4000", pointTradeResponse.getRemainPoint());
    assertTrue(capturedTrades.stream().anyMatch(
        t -> t.getBalanceAfterTrade() == 4000L && t.getTradePurpose() == TradePurpose.BUY));
    assertTrue(capturedTrades.stream().anyMatch(
        t -> t.getBalanceAfterTrade() == 1000L && t.getTradePurpose() == TradePurpose.SELL));
  }

  @Test
  @DisplayName("거래 내역 최신순 조회 테스트")
  void getPointUsageHistoryTest() {
    // given
    Member member = Member.builder()
        .email("test@email.com")
        .id(1L)
        .build();

    given(memberRepository.findByEmail(any(String.class))).willReturn(Optional.of(member));

    Trade trade1 = Trade.builder()
        .goods(Goods.builder().id(1L).build())
        .tradePoint(1000L)
        .tradedAt(LocalDateTime.now().minusDays(1))
        .member(member)
        .tradePurpose(TradePurpose.SELL)
        .build();

    Trade trade2 = Trade.builder()
        .goods(Goods.builder().id(2L).build())
        .tradePoint(2000L)
        .tradedAt(LocalDateTime.now().minusDays(2))
        .member(member)
        .tradePurpose(TradePurpose.BUY)
        .build();

    Trade trade3 = Trade.builder()
        .goods(Goods.builder().id(3L).build())
        .tradePoint(10000L)
        .tradedAt(LocalDateTime.now().minusDays(3))
        .member(member)
        .tradePurpose(TradePurpose.SELL)
        .build();

    List<Trade> trades = List.of(trade1, trade2, trade3);
    Pageable pageable = PageRequest.of(0, 10, Sort.by("tradedAt").descending());
    Page<Trade> expectedPage = new PageImpl<>(trades, pageable, 3);

    given(tradeRepository.findAllByMember(member, pageable)).willReturn(expectedPage);

    // when
    Page<PointTradeHistoryResponse> actualPage = tradeService.getPointUsageHistory(
        new UserDetailsImpl(member), 0, 10);

    // then
    assertEquals(actualPage.getTotalElements(), 3);
    assertEquals(actualPage.getContent().get(0).getTradePoint(), "1000");
    assertEquals(actualPage.getContent().get(1).getTradePoint(), "2000");
    assertEquals(actualPage.getContent().get(2).getTradePoint(), "10000");
  }

  @Test
  @DisplayName("구매자 별점 반영 및 최종 별점 산출 기능 테스트")
  @Transactional
  public void rateStarTest() {
    // given
    Member seller = Member.builder()
        .build();

    Goods goods1 = Goods.builder()
        .id(1L)
        .member(seller)
        .goodsStatus(SOLDOUT)
        .star(4.0)
        .build();

    Goods goods2 = Goods.builder()
        .id(2L)
        .member(seller)
        .goodsStatus(SOLDOUT)
        .star(5.0)
        .build();

    Goods goods3 = Goods.builder()
        .id(3L)
        .member(seller)
        .goodsStatus(SOLDOUT)
        .star(3.5)
        .build();

    Goods tradeGoods = Goods.builder()
        .id(4L)
        .member(seller)
        .goodsStatus(RESERVATION)
        .build();

    StarRateRequest starRateRequest = StarRateRequest.builder()
        .star(4.5)
        .build();

    given(goodsRepository.findById(any(Long.class))).willReturn(Optional.of(tradeGoods));
    given(goodsRepository.findAllByMemberAndGoodsStatus(any(Member.class), any(GoodsStatus.class)))
        .willReturn(List.of(goods1, goods2, goods3));

    // when
    tradeService.rateStar(tradeGoods.getId(), starRateRequest);

    // then
    double expectedStar = (4.0 + 5.0 + 3.5 + 4.5) / 4; // 예상 평균 계산
    assertEquals(expectedStar, seller.getStar(), 0.01, "판매자 별점이 올바르게 업데이트되어야 합니다.");
  }

}