package com.unity.goods.domain.trade.service;

import static com.unity.goods.domain.trade.type.TradePurpose.BUY;
import static com.unity.goods.domain.trade.type.TradePurpose.SELL;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.INSUFFICIENT_AMOUNT;
import static com.unity.goods.global.exception.ErrorCode.OUT_RANGED_COST;
import static com.unity.goods.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.unity.goods.global.exception.ErrorCode.SELLER_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.UNMATCHED_PRICE;
import static com.unity.goods.global.exception.ErrorCode.UNMATCHED_SELLER;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.type.GoodsStatus;
import com.unity.goods.domain.goods.type.PaymentStatus;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeRequest;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeResponse;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto.PointTradeHistoryResponse;
import com.unity.goods.domain.trade.dto.PurchasedListDto.PurchasedListResponse;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.exception.TradeException;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {

  private final TradeRepository tradeRepository;
  private final MemberRepository memberRepository;
  private final GoodsRepository goodsRepository;
  private final PasswordEncoder passwordEncoder;

  public Page<PurchasedListResponse> getPurchasedList(UserDetailsImpl member, int page, int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("tradedAt").descending());
    Page<Trade> purchasedPage = tradeRepository.findByMemberIdAndTradePurpose(
        member.getId(), BUY, pageable);

    List<PurchasedListResponse> purchasedList = purchasedPage.getContent().stream()
        .map(trade -> {
          Goods goods = trade.getGoods();
          String imageUrl =
              goods.getImageList().isEmpty() ? null : goods.getImageList().get(0).getImageUrl();

          return PurchasedListResponse.builder()
              .memberId(member.getId())
              .goodsId(goods.getId())
              .sellerName(goods.getMember().getNickname())
              .goodsName(goods.getGoodsName())
              .price(String.valueOf(goods.getPrice()))
              .goodsThumbnail(imageUrl)
              .goodsStatus(goods.getGoodsStatus())
              .tradedBefore(getTradedBeforeSeconds(trade.getTradedAt()))
              .build();
        }).collect(Collectors.toList());

    return new PageImpl<>(purchasedList, pageable, purchasedPage.getTotalElements());
  }

  private static long getTradedBeforeSeconds(LocalDateTime tradedDateTime) {
    return Duration.between(tradedDateTime, LocalDateTime.now()).getSeconds();
  }

  @Transactional
  public PointTradeResponse tradePoint(UserDetailsImpl member,
      PointTradeRequest pointTradeRequest) {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 결제 금액이 너무 큰 경우(천만원 이상) 처리
    if (Long.parseLong(pointTradeRequest.getPrice()) > 10_000_000L) {
      throw new TradeException(OUT_RANGED_COST);
    }

    // 결제 비밀번호 확인
    if (!passwordEncoder.matches(pointTradeRequest.getTradePassword(),
        authenticatedUser.getTradePassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    // 구매자 잔액 확인
    if (authenticatedUser.getBalance() < Long.parseLong(pointTradeRequest.getPrice())) {
      throw new TradeException(INSUFFICIENT_AMOUNT);
    }

    // 판매자 존재 확인
    Member goodsSeller = memberRepository.findById(pointTradeRequest.getSellerId())
        .orElseThrow(() -> new TradeException(SELLER_NOT_FOUND));

    Goods goods = goodsRepository.findById(pointTradeRequest.getGoodsId())
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    // 상품 판매자 일치 확인
    if (!Objects.equals(goodsSeller.getId(), goods.getMember().getId())) {
      throw new GoodsException(UNMATCHED_SELLER);
    }

    // 거래 금액 일치 확인
    if (goods.getPrice() != Long.parseLong(pointTradeRequest.getPrice())) {
      throw new TradeException(UNMATCHED_PRICE);
    }

    // 상품 이미 거래 완료된 SOLD OUT 상태인지 확인
    if (goods.getGoodsStatus().equals(GoodsStatus.SOLDOUT)) {
      throw new TradeException(ALREADY_SOLD);
    }

    // 거래
    authenticatedUser.setBalance(
        authenticatedUser.getBalance() - Long.parseLong(pointTradeRequest.getPrice()));
    Trade buyer = Trade.builder()
        .tradePoint(Long.parseLong(pointTradeRequest.getPrice()))
        .tradePurpose(BUY)
        .member(authenticatedUser)
        .goods(goods)
        .balanceAfterTrade(authenticatedUser.getBalance())
        .build();
    tradeRepository.save(buyer);
    memberRepository.save(authenticatedUser);

    goodsSeller.setBalance(goodsSeller.getBalance() + Long.parseLong(pointTradeRequest.getPrice()));
    Trade seller = Trade.builder()
        .tradePoint(Long.parseLong(pointTradeRequest.getPrice()))
        .tradePurpose(SELL)
        .member(goodsSeller)
        .goods(goods)
        .balanceAfterTrade(goodsSeller.getBalance())
        .build();
    tradeRepository.save(seller);
    memberRepository.save(goodsSeller);

    return PointTradeResponse.builder()
        .paymentStatus(String.valueOf(PaymentStatus.SUCCESS))
        .tradePoint(pointTradeRequest.getPrice())
        .remainPoint(String.valueOf(authenticatedUser.getBalance()))
        .build();
  }

  public Page<PointTradeHistoryResponse> getPointUsageHistory(UserDetailsImpl member, int page,
      int size) {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, size, Sort.by("tradedAt").descending());
    Page<Trade> usageHistory = tradeRepository.findAllByMember(authenticatedUser, pageable);

    return usageHistory.map(PointTradeHistoryDto::fromTrade);
  }
}