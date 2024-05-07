package com.unity.goods.domain.goods.service;

import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.goods.dto.GoodsDetailDto.GoodsDetailResponse;
import com.unity.goods.domain.goods.dto.UploadGoodsDto.UploadGoodsRequest;
import com.unity.goods.domain.goods.dto.UploadGoodsDto.UploadGoodsResponse;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.image.entity.Image;
import com.unity.goods.domain.image.repository.ImageRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GoodsService {

  private final MemberRepository memberRepository;
  private final S3Service s3Service;
  private final ImageRepository imageRepository;
  private final GoodsRepository goodsRepository;


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
    goods.setThumbnailImageUrl(uploadSuccessFiles.get(0));
    goodsRepository.save(goods);

    // 이미지 url db에 저장
    for (String uploadSuccessFile : uploadSuccessFiles){
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
    for(Image image : goods.getImageList()){
      goodsImages.add(image.getImageUrl());
    }
    goodsDetailResponse.setGoodsImages(goodsImages);
    return goodsDetailResponse;
  }
}
