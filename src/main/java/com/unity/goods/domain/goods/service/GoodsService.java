package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD_OUT_GOODS;
import static com.unity.goods.global.exception.ErrorCode.CANNOT_DELETE_SOLD_ITEM;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static com.unity.goods.global.exception.ErrorCode.MISMATCHED_SELLER;
import static com.unity.goods.global.exception.ErrorCode.NEED_LEAST_ONE_IMAGE;
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
import com.unity.goods.infra.service.GoodsSearchService;
import com.unity.goods.infra.service.S3Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
  private final GoodsSearchService goodsSearchService;

  private final static int MAX_IMAGE_NUM = 10;

  @Transactional
  public UploadGoodsResponse uploadGoods(UserDetailsImpl member,
      UploadGoodsRequest uploadGoodsRequest) {

    // 빈 이미지 파일 등록 시 에러 처리
    if (!uploadGoodsRequest.getGoodsImageFiles().isEmpty()
        && Objects.equals(uploadGoodsRequest.getGoodsImageFiles().get(0).getOriginalFilename(),
        "")) {
      throw new GoodsException(NEED_LEAST_ONE_IMAGE);
    }

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 이미지 s3 업로드
    List<String> uploadSuccessFiles = uploadGoodsRequest.getGoodsImageFiles().stream()
        .filter(
            multipartFile -> !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty())
        .map(multipartFile -> s3Service.uploadFile(multipartFile,
            member.getUsername() + "/" + uploadGoodsRequest.getGoodsName()))
        .toList();

    // Goods 생성 및 elasticsearch db에 저장
    Goods goods = Goods.fromUploadGoodsRequest(uploadGoodsRequest);
    goods.setMember(findMember);
    goodsRepository.save(goods);
    goodsSearchService.saveGoods(goods, uploadSuccessFiles.get(0));

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

    updateGoodsInfoRequest.updateGoodsEntity(goods);

    imageCheckAndDelete(member.getUsername(), updateGoodsInfoRequest, goods);

    return UpdateGoodsInfoResponse.fromGoods(goods);
  }

  private void imageCheckAndDelete(String email,
      UpdateGoodsInfoRequest updateGoodsInfoRequest, Goods goods) {

    Set<String> requestImageUrls = new HashSet<>(updateGoodsInfoRequest.getGoodsImageUrl());
    List<Image> currentImages = goods.getImageList();

    List<Image> imagesToDelete = currentImages.stream()
        .filter(image -> !requestImageUrls.contains(image.getImageUrl()))
        .toList();

    imagesToDelete.forEach(image -> {
      imageRepository.delete(image);
      s3Service.deleteFile(image.getImageUrl());
    });

    imageCheckAndSave(email, updateGoodsInfoRequest, goods, currentImages, imagesToDelete);
  }

  private void imageCheckAndSave(String email,
      UpdateGoodsInfoRequest updateGoodsInfoRequest,
      Goods goods, List<Image> currentImages, List<Image> imagesToDelete) {

    int remainingImageCount = currentImages.size() - imagesToDelete.size();

    List<MultipartFile> newImageFiles = updateGoodsInfoRequest.getGoodsImageFile();
    if (newImageFiles != null && remainingImageCount + newImageFiles.size() > MAX_IMAGE_NUM) {
      log.error("[GoodsService][updateGoodsInfo] : \"{}\" 상품 이미지 등록 개수 초과", goods.getGoodsName());
      throw new GoodsException(MAX_IMAGE_LIMIT_EXCEEDED);
    }

    if (newImageFiles != null) {
      newImageFiles.stream()
          .map(multipart -> s3Service.uploadFile(multipart,
              email + "/" + goods.getGoodsName()))
          .map(url -> Image.builder().imageUrl(url).goods(goods).build())
          .forEach(imageRepository::save);
    }
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

  @Transactional
  public void deleteGoods(UserDetailsImpl member, Long goodsId) {

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    if (!goods.getMember().getEmail().equals(member.getUsername())) {
      throw new GoodsException(MISMATCHED_SELLER);
    }

    // 판매완료 상품은 삭제 불가능
    if (goods.getGoodsStatus() == SOLDOUT) {
      throw new GoodsException(CANNOT_DELETE_SOLD_ITEM);
    }

    for (Image goodsImageUrl : goods.getImageList()) {
      imageRepository.deleteById(goodsImageUrl.getId());
      s3Service.deleteFile(goodsImageUrl.getImageUrl());
    }

    log.info("[GoodsService][deleteGoods] : \"GoodsId: {}, GoodsName: {}\" 상품 삭제"
        , goods.getId(), goods.getGoodsName());
    goodsRepository.deleteById(goodsId);
  }
}
