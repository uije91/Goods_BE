package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.dto.GoodsStatus.SOLD_OUT;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.WISHLIST_NOT_FOUND;

import com.unity.goods.domain.goods.dto.GoodsStatus;
import com.unity.goods.domain.goods.dto.WishlistDto;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Wishlist;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.repository.WishRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

  private final GoodsRepository goodsRepository;
  private final MemberRepository memberRepository;
  private final WishRepository wishRepository;


  public void addWishlist(Long goodsId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GoodsException(USER_NOT_FOUND));

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    if (wishRepository.existsByGoodsAndMember(goods, member)) {
      log.error("[WishService] {} 회원의 wishList에 {} Goods 가 이미 존재합니다.", member.getNickname(), goods.getGoodsName());
      throw new GoodsException(ErrorCode.GOODS_ALREADY_WISHLIST);
    }

    Wishlist wishList = Wishlist.builder()
        .member(member)
        .goods(goods)
        .build();

    wishRepository.save(wishList);
  }

  public void deleteWishlist(Long goodsId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GoodsException(USER_NOT_FOUND));

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    Wishlist wishlist = wishRepository.findByGoodsAndMember(goods, member)
        .orElseThrow(() -> new GoodsException(WISHLIST_NOT_FOUND));

    wishRepository.delete(wishlist);
  }

  public List<WishlistDto> getWishlist(Long id) {

    List<WishlistDto> goodsInWishlist = new ArrayList<>();

    List<Wishlist> wishlist = wishRepository.findByMemberId(id);
    for (Wishlist item : wishlist) {
      Long goodsId = item.getId();

      Optional<Goods> optionalGoods = goodsRepository.findById(goodsId);
      if (optionalGoods.isEmpty() || optionalGoods.get().getGoodsStatus() == SOLD_OUT) {
        log.info("[WishService] : Delete Wishlist for goodsId {}", goodsId);
        // TODO: 구현여부 결정
        //  1.조회시 SOLD_OUT 제품은 WishList 에서 삭제
        //  2.예약으로 삭제
        //deleteWishlist(goodsId, id);
      } else {
        Goods goods = optionalGoods.get();

        WishlistDto wishlistDto = WishlistDto.builder()
            .thumbnailImageUrl(goods.getThumbnailImageUrl())
            .goodsName(goods.getGoodsName())
            .address(goods.getAddress())
            .price(goods.getPrice())
            // TODO: <당근의 경우 like count 포함 - 필요시 추가 구현> / 밑부분은 넣어야할지(당근에 없는 부분)
            .sellerName(goods.getMember().getNickname())
            .goodsStatus(goods.getGoodsStatus())
            .uploadBefore(calculateTimeAgo(goods.getCreatedAt(), goods.getUpdatedAt()))
            .build();

        goodsInWishlist.add(wishlistDto);
      }
    }

    return goodsInWishlist;
  }

  private Long calculateTimeAgo(LocalDateTime createdAt, LocalDateTime updatedAt) {
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    LocalDateTime referenceTime = (updatedAt != null) ? updatedAt : createdAt;

    Duration duration = Duration.between(referenceTime, now);
    return duration.getSeconds();
  }
}
