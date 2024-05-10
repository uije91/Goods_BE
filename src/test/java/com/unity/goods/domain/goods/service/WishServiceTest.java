package com.unity.goods.domain.goods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unity.goods.domain.goods.dto.GoodsStatus;
import com.unity.goods.domain.goods.dto.WishlistDto;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Wishlist;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.repository.WishRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.domain.model.BaseEntity;
import com.unity.goods.global.exception.ErrorCode;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

  @InjectMocks
  private WishService wishService;

  @Mock
  private GoodsRepository goodsRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private WishRepository wishRepository;

  private Member member;
  private Goods goods;

  @BeforeEach
  void init() {
    member = Member.builder()
        .id(10L)
        .email("test@test.com")
        .password("test1234")
        .nickname("test")
        .status(Status.ACTIVE)
        .role(Role.USER)
        .phoneNumber("010-1111-1111")
        .build();

    goods = Goods.builder()
        .id(20L)
        .goodsName("테스트상품")
        .price(20000L)
        .description("테스트 상품입니다.")
        .address("테스트 주소")
        .lat(37.564)
        .lng(127.001)
        .member(member)
        .build();
  }

  @Test
  @DisplayName("위시리스트 조회 실패 - 위시리스트가 존재하지 않음")
  void getWishlist_NotFound() {
    //given
    when(wishRepository.findByMemberId(member.getId())).thenReturn(new ArrayList<>());

    //then
    GoodsException exception = assertThrows(GoodsException.class,
        () -> wishService.getWishlist(member.getId()));

    assertEquals(ErrorCode.WISHLIST_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("위시리스트 조회 성공")
  void getWishlist_success() throws NoSuchFieldException, IllegalAccessException {
    //given
    Goods goods1 = Goods.builder()
        .id(1L)
        .goodsName("테스트상품1")
        .thumbnailImageUrl("test_url1")
        .price(20000L)
        .address("테스트 주소1")
        .goodsStatus(GoodsStatus.SALE)
        .member(member)
        .build();

    Goods goods2 = Goods.builder()
        .id(2L)
        .goodsName("테스트상품2")
        .thumbnailImageUrl("test_url2")
        .price(30000L)
        .address("테스트 주소2")
        .goodsStatus(GoodsStatus.RESERVATION)
        .member(member)
        .build();

    // BaseEntity 시간 강제 설정
    LocalDateTime goods1Time = LocalDateTime.of(2024, 4, 9, 23, 30);
    LocalDateTime goods2Time = LocalDateTime.of(2024, 5, 10, 10, 40);
    Field createdField = BaseEntity.class.getDeclaredField("createdAt");
    createdField.setAccessible(true);
    createdField.set(goods1, goods1Time);
    createdField.set(goods2, goods2Time);

    List<Wishlist> wishlist = List.of(
        Wishlist.builder().id(1L).member(member).goods(goods1).build(),
        Wishlist.builder().id(2L).member(member).goods(goods2).build()
    );

    when(wishRepository.findByMemberId(member.getId())).thenReturn(wishlist);
    when(goodsRepository.findById(goods1.getId())).thenReturn(Optional.of(goods1));
    when(goodsRepository.findById(goods2.getId())).thenReturn(Optional.of(goods2));

    //when
    List<WishlistDto> result = wishService.getWishlist(member.getId());

    // then
    assertEquals(2, result.size());
    WishlistDto dto = result.get(0);
    assertEquals("테스트상품1", dto.getGoodsName());
    assertEquals("test_url1", dto.getThumbnailImageUrl());
    assertEquals("테스트 주소1", dto.getAddress());
    assertEquals(20000L, dto.getPrice());
    assertEquals("test", dto.getSellerName());
    assertEquals(GoodsStatus.SALE, dto.getGoodsStatus());
  }


  @Test
  @DisplayName("위시리스트 등록 실패 - 멤버를 찾을 수 없음")
  void addWishlist_MemberNotFound() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.addWishlist(goods.getId(), member.getId()));
    assertEquals(ErrorCode.USER_NOT_FOUND, goodsException.getErrorCode());

  }

  @Test
  @DisplayName("위시리스트 등록 실패 - 상품을 찾을 수 없음")
  void addWishlist_GoodsNotFound() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.empty());

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.addWishlist(goods.getId(), member.getId()));
    assertEquals(ErrorCode.GOODS_NOT_FOUND, goodsException.getErrorCode());
  }

  @Test
  @DisplayName("위시리스트 등록 실패 - 이미 등록된 위시리스트")
  void addWishList_AlreadyInWishlist() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.of(goods));
    when(wishRepository.existsByGoodsAndMember(goods, member)).thenReturn(true);

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.addWishlist(goods.getId(), member.getId()));

    assertEquals(ErrorCode.GOODS_ALREADY_WISHLIST, goodsException.getErrorCode());
  }

  @Test
  @DisplayName("위시리스트 등록 성공")
  void addWishlist_success() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.of(goods));
    when(wishRepository.existsByGoodsAndMember(goods, member)).thenReturn(false);

    //when
    wishService.addWishlist(goods.getId(), member.getId());

    //then
    verify(memberRepository, times(1)).findById(member.getId());
    verify(goodsRepository, times(1)).findById(goods.getId());
    verify(wishRepository, times(1)).existsByGoodsAndMember(goods, member);
    verify(wishRepository, times(1)).save(any(Wishlist.class));
  }

  @Test
  @DisplayName("위시리스트 삭제 실패 - 멤버를 찾을 수 없음")
  void deleteWishlist_MemberNotFound() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.deleteWishlist(goods.getId(), member.getId()));
    assertEquals(ErrorCode.USER_NOT_FOUND, goodsException.getErrorCode());
  }

  @Test
  @DisplayName("위시리스트 삭제 실패 - 상품을 찾을 수 없음")
  void deleteWishlist_GoodsNotFound() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.empty());

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.deleteWishlist(goods.getId(), member.getId()));
    assertEquals(ErrorCode.GOODS_NOT_FOUND, goodsException.getErrorCode());
  }

  @Test
  @DisplayName("위시리스트 삭제 실패 - 위시리스트가 없음")
  void deleteWishlist_WishlistNotFound() {
    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.of(goods));
    when(wishRepository.findByGoodsAndMember(goods, member)).thenReturn(Optional.empty());

    //then
    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> wishService.deleteWishlist(goods.getId(), member.getId()));
    assertEquals(ErrorCode.WISHLIST_NOT_FOUND, goodsException.getErrorCode());

  }

  @Test
  @DisplayName("위시리스트 삭제 성공")
  void deleteWishlist_success() {
    Wishlist wishlist = Wishlist.builder()
        .member(member)
        .goods(goods)
        .build();

    //given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(goodsRepository.findById(goods.getId())).thenReturn(Optional.of(goods));
    when(wishRepository.findByGoodsAndMember(goods, member)).thenReturn(Optional.of(wishlist));

    //when
    wishService.deleteWishlist(goods.getId(), member.getId());

    //then
    verify(memberRepository, times(1)).findById(member.getId());
    verify(goodsRepository, times(1)).findById(goods.getId());
    verify(wishRepository, times(1)).findByGoodsAndMember(goods, member);
    verify(wishRepository, times(1)).delete(wishlist);
  }
}