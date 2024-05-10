package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.global.exception.ErrorCode.GOODS_ALREADY_WISHLIST;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.IMPOSSIBLE_TO_WISHLIST_MY_GOODS;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.WISHLIST_NOT_FOUND;

import com.unity.goods.domain.goods.dto.WishlistDto;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Image;
import com.unity.goods.domain.goods.entity.Wishlist;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.repository.ImageRepository;
import com.unity.goods.domain.goods.repository.WishRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

  private final GoodsRepository goodsRepository;
  private final MemberRepository memberRepository;
  private final WishRepository wishRepository;
  private final ImageRepository imageRepository;


  public void addWishlist(Long goodsId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GoodsException(USER_NOT_FOUND));

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    if (goods.getMember().getId().equals(memberId)) {
      throw new GoodsException(IMPOSSIBLE_TO_WISHLIST_MY_GOODS);
    }

    if (wishRepository.existsByGoodsAndMember(goods, member)) {
      log.error("[WishService] {} 회원의 wishList에 {} Goods 가 이미 존재합니다.",
          member.getNickname(), goods.getGoodsName());
      throw new GoodsException(GOODS_ALREADY_WISHLIST);
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


  public Page<WishlistDto> getWishlist(Long memberId, Pageable pageable) {
    List<WishlistDto> goodsInWishlist = new ArrayList<>();

    Page<Wishlist> wishlists = wishRepository.findByMemberId(memberId,pageable);
    wishlists.getContent().forEach(item -> {
      Long goodsId = item.getId();
      Optional<Goods> optionalGoods = goodsRepository.findById(goodsId);

      optionalGoods.ifPresent(goods -> {
        if (goods.getGoodsStatus() == SOLDOUT) {
          log.info("[WishService] : Delete Wishlist for goodsId {}", goodsId);
          deleteWishlist(goodsId, memberId);
        } else {
          String image = null;
          List<Image> images = imageRepository.findByGoodsId(goodsId);
          if (images != null && !images.isEmpty()) {
            image = imageRepository.findByGoodsId(goodsId).get(0).getImageUrl();
          }

          WishlistDto wishlistDto = WishlistDto.builder()
              .imageUrl(image)
              .goodsName(goods.getGoodsName())
              .address(goods.getAddress())
              .price(goods.getPrice())
              .sellerName(goods.getMember().getNickname())
              .goodsStatus(goods.getGoodsStatus())
              .uploadBefore(calculateTimeAgo(goods.getCreatedAt(), goods.getUpdatedAt()))
              .build();
          goodsInWishlist.add(wishlistDto);
        }
      });
    });

    return new PageImpl<>(goodsInWishlist, pageable, wishlists.getTotalElements());
  }

  private Long calculateTimeAgo(LocalDateTime createdAt, LocalDateTime updatedAt) {
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    LocalDateTime referenceTime = (updatedAt != null) ? updatedAt : createdAt;

    Duration duration = Duration.between(referenceTime, now);
    return duration.getSeconds();
  }
}
