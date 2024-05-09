package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD_OUT_GOODS;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static com.unity.goods.global.exception.ErrorCode.MISMATCHED_SELLER;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.goods.dto.GoodsDetailDto.GoodsDetailResponse;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.dto.UpdateGoodsStateDto.UpdateGoodsStateRequest;
import com.unity.goods.domain.goods.dto.UploadGoodsDto.UploadGoodsRequest;
import com.unity.goods.domain.goods.dto.UploadGoodsDto.UploadGoodsResponse;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Image;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.goods.repository.ImageRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

  private final MemberRepository memberRepository;
  private final S3Service s3Service;
  private final ImageRepository imageRepository;
  private final GoodsRepository goodsRepository;

  private final static int MAX_IMAGE_NUM = 10;

  public UploadGoodsResponse uploadGoods(UserDetailsImpl member,
      UploadGoodsRequest uploadGoodsRequest) {

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 이미지 s3 업로드
    List<String> uploadSuccessFiles = new ArrayList<>();
    for (MultipartFile multipartFile : uploadGoodsRequest.getGoodsImageFiles()) {
      uploadSuccessFiles.add(s3Service.uploadFile(multipartFile, member.getUsername()));
    }

    // Goods 생성 및 썸네일 url 설정
    Goods goods = Goods.fromUploadGoodsRequest(uploadGoodsRequest);
    goods.setMember(findMember);
    goodsRepository.save(goods);

    // 이미지 url db에 저장
    for (String uploadSuccessFile : uploadSuccessFiles) {
      Image image = Image.fromImageUrlAndGoods(uploadSuccessFile, goods);
      imageRepository.save(image);
    }

    return UploadGoodsResponse.fromGoods(goods);
  }

  public GoodsDetailResponse getDetailGoods(UserDetailsImpl member, Long id) {

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    Goods goods = goodsRepository.findById(id)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    GoodsDetailResponse goodsDetailResponse = GoodsDetailResponse.fromGoodsAndMember(goods,
        findMember);

    List<String> goodsImages = new ArrayList<>();
    for (Image image : goods.getImageList()) {
      goodsImages.add(image.getImageUrl());
    }
    goodsDetailResponse.setGoodsImages(goodsImages);
    return goodsDetailResponse;
  }

  @Transactional
  public UpdateGoodsInfoResponse updateGoodsInfo(Long goodsId, UserDetailsImpl member,
      UpdateGoodsInfoRequest updateGoodsInfoRequest) {

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    if (!goods.getMember().getEmail().equals(member.getUsername())) {
      throw new GoodsException(MISMATCHED_SELLER);
    }

    if (goods.getGoodsStatus() == SOLDOUT) {
      throw new GoodsException(ALREADY_SOLD_OUT_GOODS);
    }

    if (updateGoodsInfoRequest.getGoodsName() != null) {
      goods.setGoodsName(updateGoodsInfoRequest.getGoodsName());
    }

    if (updateGoodsInfoRequest.getPrice() != null) {
      goods.setPrice(Long.parseLong(updateGoodsInfoRequest.getPrice()));
    }

    if (updateGoodsInfoRequest.getDescription() != null) {
      goods.setDescription(updateGoodsInfoRequest.getDescription());
    }

    if (updateGoodsInfoRequest.getGoodsImages() != null) {
      if (goods.getImageList().size() + updateGoodsInfoRequest.getGoodsImages().size()
          > MAX_IMAGE_NUM) {
        log.error("[GoodsService][updateGoodsInfo] : \"{}\" 상품 이미지 등록 개수 초과", goods.getGoodsName());
        throw new GoodsException(MAX_IMAGE_LIMIT_EXCEEDED);
      }

      for (MultipartFile multipart : updateGoodsInfoRequest.getGoodsImages()) {
        String goodsImageUrl = s3Service.uploadFile(multipart, goods.getMember().getEmail());

        imageRepository.save(
            Image.builder()
                .imageUrl(goodsImageUrl)
                .goods(goods)
                .build()
        );
      }
    }

    if (updateGoodsInfoRequest.getLat() != null) {
      goods.setLat(updateGoodsInfoRequest.getLat());
    }

    if (updateGoodsInfoRequest.getLng() != null) {
      goods.setLng(updateGoodsInfoRequest.getLng());
    }

    if (updateGoodsInfoRequest.getDetailLocation() != null) {
      goods.setAddress(updateGoodsInfoRequest.getDetailLocation());
    }

    return UpdateGoodsInfoResponse.fromGoods(goods);
  }

  @Transactional
  public void updateState(UserDetailsImpl member, Long goodsId,
      UpdateGoodsStateRequest updateGoodsStateRequest) {

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    if (!goods.getMember().getEmail().equals(member.getUsername())) {
      throw new GoodsException(MISMATCHED_SELLER);
    }

    goods.setGoodsStatus(updateGoodsStateRequest.getGoodsStatus());
  }
}
