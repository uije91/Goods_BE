package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Image;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.repository.ImageRepository;
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
  private ImageRepository imageRepository;

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
        .price(20000L)
        .description("테스트 상품입니다.")
        .address("서울시 강남구 테스트 건물 앞")
        .lat(37.564)
        .lng(127.001)
        .member(member)
        .imageList(new ArrayList<>())
        .build();

    Image goodsImage1 = Image.builder()
        .imageUrl("http://amazonS3/testGoods1.png")
        .goods(goods)
        .build();

    goods.getImageList().add(goodsImage1);

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
        .goodsImageFiles(goodsImages)
        .address("서울시 강남구")
        .userDefinedLocation("새로운 테스트 건물 앞")
        .build();

    given(goodsRepository.findById(1L))
        .willReturn(Optional.of(goods));

    given(s3Service.uploadFile(any(MultipartFile.class), anyString()))
        .willReturn("http://amazonS3/new.png");

    Image image = Image.builder()
        .imageUrl("http://amazonS3/new.png")
        .goods(goods)
        .build();

    given(imageRepository.save(any(Image.class)))
        .willReturn(image);

    //when
    UpdateGoodsInfoResponse updateGoodsInfoResponse =
        goodsService.updateGoodsInfo(1L, new UserDetailsImpl(member), updateGoodsInfoRequest);

    //then
    verify(goodsRepository, times(1)).findById(1L);
    verify(s3Service, times(1)).uploadFile(any(MultipartFile.class), anyString());
    verify(imageRepository, times(1)).save(any(Image.class));

    assertEquals("test", updateGoodsInfoResponse.getSellerName());
    assertEquals("새로운 테스트상품", updateGoodsInfoResponse.getGoodsName());
    assertEquals("10000", updateGoodsInfoResponse.getPrice());
    assertEquals("테스트 상품의 새로운 설명 입니다.", updateGoodsInfoResponse.getDescription());
    assertEquals("서울시 강남구 새로운 테스트 건물 앞", updateGoodsInfoResponse.getAddress());
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
        .imageList(new ArrayList<>())
        .member(member)
        .build();

    for (int i = 0; i < 10; i++) {
      goods.getImageList().add(new Image(1L + i, "test" + i, goods));
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
        .price("10000")
        .description("테스트 상품의 새로운 설명 입니다.")
        .lat(11.564)
        .lng(27.001)
        .goodsImageFiles(goodsImages)
        .address("서울시 강남구")
        .userDefinedLocation("새로운 테스트 건물 앞")
        .build();

    //when

    GoodsException goodsException = assertThrows(GoodsException.class,
        () -> goodsService.updateGoodsInfo(1L, new UserDetailsImpl(member),
            updateGoodsInfoRequest));

    //then
    assertEquals(MAX_IMAGE_LIMIT_EXCEEDED, goodsException.getErrorCode());
  }

  @Test
  @DisplayName("상품 삭제 테스트")
  void deleteGoodsTest() {

    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(ACTIVE)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.png")
        .build();

    Image image = Image.builder()
        .id(1L)
        .imageUrl("http://amazonS3/test.png")
        .build();

    List<Image> goodsImageList = new ArrayList<>();
    goodsImageList.add(image);

    Goods goods = Goods.builder()
        .id(1L)
        .goodsName("테스트상품")
        .imageList(goodsImageList)
        .member(member)
        .build();

    given(goodsRepository.findById(1L))
        .willReturn(Optional.of(goods));

    doNothing().when(imageRepository).deleteById(1L);
    doNothing().when(s3Service).deleteFile("http://amazonS3/test.png");

    //when
    goodsService.deleteGoods(new UserDetailsImpl(member), 1L);

    //then
    verify(goodsRepository, times(1)).findById(1L);
    verify(s3Service, times(1)).deleteFile("http://amazonS3/test.png");
    verify(imageRepository, times(1)).deleteById(1L);
    verify(goodsRepository, times(1)).deleteById(1L);
  }


}