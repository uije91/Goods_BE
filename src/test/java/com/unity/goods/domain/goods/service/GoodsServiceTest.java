package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.GoodsImage;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsImageRepository;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.S3Service;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {

  @InjectMocks
  private GoodsService goodsService;

  @Mock
  private GoodsRepository goodsRepository;

  @Mock
  private GoodsImageRepository goodsImageRepository;

  @Mock
  private S3Service s3Service;


  @Test
  @DisplayName("회원정보 수정 성공 테스트입니다.")
  void updateGoodsInfoSuccess() {
    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(ACTIVE)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.png")
        .build();

    Goods goods = Goods.builder()
        .id(1L)
        .goodsName("테스트상품")
        .price(20000)
        .description("테스트 상품입니다.")
        .address("테스트 건물 앞")
        .lat(37.564)
        .lng(127.001)
        .member(member)
        .goodsImageList(new ArrayList<>())
        .build();

    GoodsImage goodsImage1 = GoodsImage.builder()
        .goodsImageUrl("http://amazonS3/testGoods1.png")
        .goods(goods)
        .build();

    goods.getGoodsImageList().add(goodsImage1);

    List<MultipartFile> goodsImages = new ArrayList<>();
    MockMultipartFile multipartFile1 = new MockMultipartFile
        ("newFile", "new.png", "image/png",
            "new file".getBytes(StandardCharsets.UTF_8));

    goodsImages.add(multipartFile1);

    UpdateGoodsInfoRequest updateGoodsInfoRequest = UpdateGoodsInfoRequest.builder()
        .goodsName("새로운 테스트상품")
        .price("10000")
        .description("테스트 상품의 새로운 설명 입니다.")
        .lat(11.564)
        .lng(27.001)
        .goodsImages(goodsImages)
        .detailLocation("새로운 테스트 건물 앞")
        .build();

    given(goodsRepository.findById(1L))
        .willReturn(Optional.of(goods));

    given(s3Service.uploadFile(any(MultipartFile.class), anyString()))
        .willReturn("http://amazonS3/new.png");

    GoodsImage image = GoodsImage.builder()
        .goodsImageUrl("http://amazonS3/new.png")
        .goods(goods)
        .build();

    given(goodsImageRepository.save(any(GoodsImage.class)))
        .willReturn(image);

    //when
    UpdateGoodsInfoResponse updateGoodsInfoResponse =
        goodsService.updateGoodsInfo(1L, new UserDetailsImpl(member), updateGoodsInfoRequest);

    //then
    verify(goodsRepository, times(1)).findById(1L);
    verify(s3Service, times(1)).uploadFile(any(MultipartFile.class), anyString());
    verify(goodsImageRepository, times(1)).save(any(GoodsImage.class));

    assertEquals("test", updateGoodsInfoResponse.getSellerName());
    assertEquals("새로운 테스트상품", updateGoodsInfoResponse.getGoodsName());
    assertEquals("10000", updateGoodsInfoResponse.getPrice());
    assertEquals("테스트 상품의 새로운 설명 입니다.", updateGoodsInfoResponse.getDescription());
    assertEquals("새로운 테스트 건물 앞", updateGoodsInfoResponse.getDetailLocation());
  }

  @Test
  @DisplayName("상품 이미지 개수 초과 테스트")
  void exceedImagesTest() {
    //given

    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(ACTIVE)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.png")
        .build();

    Goods goods = Goods.builder()
        .id(1L)
        .goodsName("테스트상품")
        .goodsImageList(new ArrayList<>())
        .member(member)
        .build();

    for (int i = 0; i < 10; i++) {
      goods.getGoodsImageList().add(new GoodsImage(1L + i, "test" + i, goods));
    }

    given(goodsRepository.findById(1L))
        .willReturn(Optional.of(goods));

    List<MultipartFile> goodsImages = new ArrayList<>();
    MockMultipartFile multipartFile1 = new MockMultipartFile
        ("newFile", "new.png", "image/png",
            "new file".getBytes(StandardCharsets.UTF_8));

    goodsImages.add(multipartFile1);

    UpdateGoodsInfoRequest updateGoodsInfoRequest = UpdateGoodsInfoRequest.builder()
        .goodsName("새로운 테스트상품")
        .goodsImages(goodsImages)
        .build();

    //when

    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> goodsService.updateGoodsInfo(1L, new UserDetailsImpl(member),
            updateGoodsInfoRequest));

    //then
    assertEquals(MAX_IMAGE_LIMIT_EXCEEDED, goodsException.getErrorCode());
  }


}