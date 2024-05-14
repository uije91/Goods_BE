package com.unity.goods.domain.trade.service;

import static com.unity.goods.domain.trade.type.TradePurpose.BUY;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.trade.dto.PurchasedListDto.PurchasedListResponse;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

  private final TradeRepository tradeRepository;

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
}