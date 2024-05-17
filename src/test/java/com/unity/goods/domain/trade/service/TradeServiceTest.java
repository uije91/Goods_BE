package com.unity.goods.domain.trade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto.PointTradeHistoryResponse;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.domain.trade.type.TradePurpose;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

  @InjectMocks
  private TradeService tradeService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private TradeRepository tradeRepository;

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

}