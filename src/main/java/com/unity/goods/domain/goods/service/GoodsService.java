package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD_OUT_GOODS;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static com.unity.goods.global.exception.ErrorCode.MISMATCHED_SELLER;

import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.GoodsImage;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsImageRepository;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

  private final GoodsRepository goodsRepository;
  private final GoodsImageRepository goodsImageRepository;
  private final S3Service s3Service;

  private final static int MAX_IMAGE_NUM = 10;

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
      goods.setPrice(Integer.parseInt(updateGoodsInfoRequest.getPrice()));
    }

    if (updateGoodsInfoRequest.getDescription() != null) {
      goods.setDescription(updateGoodsInfoRequest.getDescription());
    }

    if (!updateGoodsInfoRequest.getGoodsImages().isEmpty()) {
      if (goods.getGoodsImageList().size() + updateGoodsInfoRequest.getGoodsImages().size()
          > MAX_IMAGE_NUM) {
        log.error("[GoodsService][updateGoodsInfo] : \"{}\" 상품 이미지 등록 개수 초과", goods.getGoodsName());
        throw new GoodsException(MAX_IMAGE_LIMIT_EXCEEDED);
      }

      for (MultipartFile multipart : updateGoodsInfoRequest.getGoodsImages()) {
        String goodsImageUrl = s3Service.uploadFile(multipart, goods.getMember().getEmail());

        goodsImageRepository.save(
            GoodsImage.builder()
                .goodsImageUrl(goodsImageUrl)
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
}
