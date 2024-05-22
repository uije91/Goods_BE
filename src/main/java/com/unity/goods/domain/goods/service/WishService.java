package com.unity.goods.domain.goods.service;

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
import com.unity.goods.infra.service.GoodsSearchService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
  private final GoodsSearchService goodsSearchService;


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

    // elastic search의 좋아요 수 증가
    goodsSearchService.updateGoodsLikes(goodsId, 1);
    wishRepository.save(wishList);
  }

  public void deleteWishlist(Long goodsId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GoodsException(USER_NOT_FOUND));

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    Wishlist wishlist = wishRepository.findByGoodsAndMember(goods, member)
        .orElseThrow(() -> new GoodsException(WISHLIST_NOT_FOUND));

    // elastic search의 좋아요 수 감소
    goodsSearchService.updateGoodsLikes(goodsId, -1);
    wishRepository.delete(wishlist);
  }


  public Page<WishlistDto> getWishlist(Long memberId, Pageable pageable) {
    Page<Wishlist> wishlists = wishRepository.findByMemberId(memberId, pageable);

    List<WishlistDto> goodsInWishlist = wishlists.getContent().stream()
        .map(item -> {
          Optional<Goods> optionalGoods = goodsRepository.findById(item.getId());
          return optionalGoods.map(this::getWishlistDto).orElse(null);
        })
        .filter(Objects::nonNull).toList();

    return new PageImpl<>(goodsInWishlist, pageable, wishlists.getTotalElements());
  }

  private WishlistDto getWishlistDto(Goods goods) {
    // 대표이미지 가져오기
    String image = null;
    List<Image> images = imageRepository.findByGoodsId(goods.getId());
    if (images != null && !images.isEmpty()) {
      image = imageRepository.findByGoodsId(goods.getId()).get(0).getImageUrl();
    }

    // 날짜 계산 최근 -> 갱신일로 부터 몇일 지났는지 체크
    long uploadBefore = Duration.between(goods.getCreatedAt(), LocalDateTime.now()).getSeconds();

    return WishlistDto.builder()
        .goodsId(goods.getId())
        .goodsName(goods.getGoodsName())
        .imageUrl(image)
        .address(goods.getAddress())
        .price(String.valueOf(goods.getPrice()))
        .sellerName(goods.getMember().getNickname())
        .goodsStatus(goods.getGoodsStatus().getDescription())
        .uploadBefore(uploadBefore)
        .build();
  }

}
