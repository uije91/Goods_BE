package com.unity.goods.domain.trade.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.domain.trade.type.TradePurpose.BUY;
import static com.unity.goods.domain.trade.type.TradePurpose.SELL;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD;
import static com.unity.goods.global.exception.ErrorCode.FCM_TOKEN_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.INSUFFICIENT_AMOUNT;
import static com.unity.goods.global.exception.ErrorCode.OUT_RANGED_COST;
import static com.unity.goods.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.unity.goods.global.exception.ErrorCode.RATE_ALREADY_REGISTERED;
import static com.unity.goods.global.exception.ErrorCode.SELLER_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.UNIDENTIFIED_TRADE;
import static com.unity.goods.global.exception.ErrorCode.UNMATCHED_PRICE;
import static com.unity.goods.global.exception.ErrorCode.UNMATCHED_SELLER;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.PaymentStatus;
import com.unity.goods.domain.notification.service.FcmService;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeRequest;
import com.unity.goods.domain.trade.dto.PointTradeDto.PointTradeResponse;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto;
import com.unity.goods.domain.trade.dto.PointTradeHistoryDto.PointTradeHistoryResponse;
import com.unity.goods.domain.trade.dto.PurchasedListDto.PurchasedListResponse;
import com.unity.goods.domain.trade.dto.StarRateDto.StarRateRequest;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.exception.TradeException;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.domain.trade.type.TradePurpose;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.GoodsSearchService;
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
  private final GoodsSearchService goodsSearchService;
  private final FcmService fcmService;

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
              .goodsStatus(goods.getGoodsStatus().getDescription())
              .tradedBefore(getTradedBeforeSeconds(trade.getTradedAt()))
              .build();
        }).collect(Collectors.toList());

    return new PageImpl<>(purchasedList, pageable, purchasedPage.getTotalElements());
  }

  private static long getTradedBeforeSeconds(LocalDateTime tradedDateTime) {
    return Duration.between(tradedDateTime, LocalDateTime.now()).getSeconds();
  }

  @Transactional
  public PointTradeResponse transferPoint(UserDetailsImpl member,
      PointTradeRequest pointTradeRequest) throws Exception {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    validateTrade(authenticatedUser, pointTradeRequest);

    // 판매자 존재 확인
    Member goodsSeller = memberRepository.findById(pointTradeRequest.getSellerId())
        .orElseThrow(() -> new TradeException(SELLER_NOT_FOUND));

    Goods goods = goodsRepository.findById(pointTradeRequest.getGoodsId())
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    validateTradeGoods(goodsSeller, goods, pointTradeRequest);

    // 거래
    authenticatedUser.setBalance(
        authenticatedUser.getBalance() - Long.parseLong(pointTradeRequest.getPrice()));
    Trade buyer = createTrade(goodsSeller, goods, pointTradeRequest.getPrice(), BUY);
    tradeRepository.save(buyer);
    memberRepository.save(authenticatedUser);

    goodsSeller.setBalance(goodsSeller.getBalance() + Long.parseLong(pointTradeRequest.getPrice()));
    Trade seller = createTrade(goodsSeller, goods, pointTradeRequest.getPrice(), SELL);
    tradeRepository.save(seller);
    memberRepository.save(goodsSeller);

    goods.setGoodsStatus(SOLDOUT);

    if(authenticatedUser.getFcmToken() == null || goodsSeller.getFcmToken() == null){
      throw new MemberException(FCM_TOKEN_NOT_FOUND);
    }
    fcmService.sendTradeCompleteNotification(authenticatedUser.getFcmToken());
    fcmService.sendTradeCompleteNotification(goodsSeller.getFcmToken());

    fcmService.sendPointReceivedNotification(authenticatedUser.getFcmToken());
    fcmService.sendPointReceivedNotification(goodsSeller.getFcmToken());

    goodsRepository.save(goods);
    goodsSearchService.deleteGoodsDocument("keywords", String.valueOf(goods.getId()));

    return PointTradeResponse.builder()
        .paymentStatus(PaymentStatus.SUCCESS.getDescription())
        .tradePoint(pointTradeRequest.getPrice())
        .remainPoint(String.valueOf(authenticatedUser.getBalance()))
        .build();
  }

  private Trade createTrade(Member member, Goods goods, String tradePoint, TradePurpose tradePurpose) {
    return Trade.builder()
        .tradePoint(Long.parseLong(tradePoint))
        .tradePurpose(tradePurpose)
        .member(member)
        .goods(goods)
        .balanceAfterTrade(member.getBalance())
        .build();
  }

  private void validateTradeGoods(Member goodsSeller, Goods goods,
      PointTradeRequest pointTradeRequest) {
    // 상품 판매자 일치 확인
    if (!Objects.equals(goodsSeller.getId(), goods.getMember().getId())) {
      throw new GoodsException(UNMATCHED_SELLER);
    }

    // 거래 금액 일치 확인
    if (goods.getPrice() != Long.parseLong(pointTradeRequest.getPrice())) {
      throw new TradeException(UNMATCHED_PRICE);
    }

    // 상품 이미 거래 완료된 SOLD OUT 상태인지 확인
    if (goods.getGoodsStatus().equals(SOLDOUT)) {
      throw new TradeException(ALREADY_SOLD);
    }
  }

  private void validateTrade(Member authenticatedUser, PointTradeRequest pointTradeRequest) {
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
  }

  public Page<PointTradeHistoryResponse> getPointUsageHistory(UserDetailsImpl member, int page,
      int size) {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, size, Sort.by("tradedAt").descending());
    Page<Trade> usageHistory = tradeRepository.findAllByMember(authenticatedUser, pageable);

    return usageHistory.map(PointTradeHistoryDto::fromTrade);
  }

  @Transactional
  public void rateStar(Long goodsId, StarRateRequest starRateRequest) {

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    // 거래 완료된 상품에 별점 매길 수 있음. (거래 완료 == 판매자가 입금 확인 완료)
    if(goods.getGoodsStatus() != SOLDOUT){
      throw new TradeException(UNIDENTIFIED_TRADE);
    }

    // 상품 별점 등록 여부
    if (goods.getStar() != 0.0) {
      throw new GoodsException(RATE_ALREADY_REGISTERED);
    }

    goods.setStar(starRateRequest.getStar());
    Member seller = goods.getMember();

    // SOLD OUT된 상품들 + 현재 별점 매긴 상품들의 최종 평점 계산
    List<Goods> allByMemberAndGoodsStatus = goodsRepository.findAllByMemberAndGoodsStatus(seller,
        SOLDOUT);
    double totalRate = allByMemberAndGoodsStatus.stream().mapToDouble(Goods::getStar).sum();
    seller.setStar(totalRate / allByMemberAndGoodsStatus.size());

  }
}