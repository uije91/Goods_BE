package com.unity.goods.domain.goods.service;

import static com.unity.goods.domain.goods.type.GoodsStatus.SOLDOUT;
import static com.unity.goods.domain.goods.type.GoodsStatus.fromDescription;
import static com.unity.goods.domain.trade.type.TradePurpose.BUY;
import static com.unity.goods.domain.trade.type.TradePurpose.SELL;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_SOLD_OUT_GOODS;
import static com.unity.goods.global.exception.ErrorCode.CANNOT_DELETE_SOLD_ITEM;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.MAX_IMAGE_LIMIT_EXCEEDED;
import static com.unity.goods.global.exception.ErrorCode.MISMATCHED_SELLER;
import static com.unity.goods.global.exception.ErrorCode.NEED_LEAST_ONE_IMAGE;
import static com.unity.goods.global.exception.ErrorCode.OUT_RANGED_COST;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.goods.dto.GoodsDetailDto.GoodsDetailResponse;
import com.unity.goods.domain.goods.dto.SellerSalesListDto.SellerSalesListResponse;
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
import com.unity.goods.domain.goods.repository.WishRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.BadgeRepository;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.exception.TradeException;
import com.unity.goods.domain.trade.repository.TradeRepository;
import com.unity.goods.domain.trade.type.TradePurpose;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.GoodsSearchService;
import com.unity.goods.infra.service.S3Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

  private final TradeRepository tradeRepository;
  private final MemberRepository memberRepository;
  private final S3Service s3Service;
  private final ImageRepository imageRepository;
  private final GoodsRepository goodsRepository;
  private final BadgeRepository badgeRepository;
  private final WishRepository wishRepository;
  private final GoodsSearchService goodsSearchService;

  private final static int MAX_IMAGE_NUM = 10;

  @Transactional
  public UploadGoodsResponse uploadGoods(UserDetailsImpl member,
      UploadGoodsRequest uploadGoodsRequest) {

    // 가격 제한 확인
    if (Long.parseLong(uploadGoodsRequest.getPrice()) > 10_000_000L) {
      throw new TradeException(OUT_RANGED_COST);
    }

    // 빈 이미지 파일 등록 시 에러 처리
    if (!uploadGoodsRequest.getGoods_image_files().isEmpty()
        && Objects.equals(uploadGoodsRequest.getGoods_image_files().get(0).getOriginalFilename(),
        "")) {
      throw new GoodsException(NEED_LEAST_ONE_IMAGE);
    }

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 이미지 s3 업로드
    List<String> uploadSuccessFiles = uploadGoodsRequest.getGoods_image_files().stream()
        .filter(
            multipartFile -> !Objects.requireNonNull(multipartFile.getOriginalFilename()).isEmpty())
        .map(multipartFile -> s3Service.uploadFile(multipartFile,
            member.getUsername() + "/" + uploadGoodsRequest.getGoods_name()))
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

  public GoodsDetailResponse getDetailGoods(Long goodsId) {

    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new GoodsException(GOODS_NOT_FOUND));

    GoodsDetailResponse goodsDetailResponse = GoodsDetailResponse.fromGoodsAndMember(goods,
        goods.getMember());

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (!principal.equals("anonymousUser")) {
      String loginVisitor = ((UserDetailsImpl) principal).getUsername();
      Member member = memberRepository.findMemberByEmail(loginVisitor);
      if (wishRepository.existsByGoodsAndMember(goods, member)) {
        goodsDetailResponse.setLiked(true);
      }
    }

    List<String> goodsImages = new ArrayList<>();
    for (Image image : goods.getImageList()) {
      goodsImages.add(image.getImageUrl());
    }
    goodsDetailResponse.setGoodsImages(goodsImages);

    List<String> badgeListString = badgeRepository.findAllByMember(goods.getMember()).stream()
        .map(badge -> badge.getBadge().getDescription())
        .collect(Collectors.toList());

    goodsDetailResponse.setBadgeList(badgeListString);
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

    // 가격 제한 확인
    if (Long.parseLong(updateGoodsInfoRequest.getPrice()) > 10_000_000L) {
      throw new TradeException(OUT_RANGED_COST);
    }

    int deleteImageCnt = (updateGoodsInfoRequest.getImages_to_delete()) == null ? 0
        : updateGoodsInfoRequest.getImages_to_delete().size();

    int addImageCnt = (updateGoodsInfoRequest.getGoods_image_files()) == null ? 0
        : updateGoodsInfoRequest.getGoods_image_files().size();

    if (goods.getImageList().size() - deleteImageCnt + addImageCnt > MAX_IMAGE_NUM) {
      log.error("[GoodsService][updateGoodsInfo] : \"{}\" 상품 이미지 등록 개수 초과", goods.getGoodsName());
      throw new GoodsException(MAX_IMAGE_LIMIT_EXCEEDED);
    }

    Optional.ofNullable(updateGoodsInfoRequest.getImages_to_delete())
        .ifPresent(urls -> urls.forEach(goodsUrl -> {
          imageRepository.deleteImageByImageUrl(goodsUrl);
          s3Service.deleteFile(goodsUrl);
        }));

    Optional.ofNullable(updateGoodsInfoRequest.getGoods_image_files())
        .ifPresent(files ->
            files.stream()
                .map(multipart -> s3Service.uploadFile(multipart,
                    member.getUsername() + "/" + goods.getGoodsName()))
                .map(url -> Image.builder().imageUrl(url).goods(goods).build())
                .forEach(imageRepository::save)
        );

    updateGoodsInfoRequest.updateGoodsEntity(goods);

    // ES data 기존 삭제, 새로 추가
    goodsSearchService.deleteGoodsDocument("keywords", String.valueOf(goods.getId()));

    String newThumbnail = "";
    if (!goods.getImageList().isEmpty()) {
      int count = 0;
      for (Image img : goods.getImageList()) {
        if (!updateGoodsInfoRequest.getImages_to_delete().isEmpty()
            && !(updateGoodsInfoRequest.getImages_to_delete().contains(img.getImageUrl()))) {
          newThumbnail = img.getImageUrl();
          break;
        }
        count++;
      }

      if (count == goods.getImageList().size() && !updateGoodsInfoRequest.getGoods_image_files()
          .isEmpty()) {
        newThumbnail = s3Service.uploadFile(updateGoodsInfoRequest.getGoods_image_files().get(0),
            member.getUsername() + "/" + goods.getGoodsName());
      }
    }

    goodsSearchService.saveGoods(goods, newThumbnail);
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

    goods.setGoodsStatus(fromDescription(updateGoodsStateRequest.getGoodsStatus()));
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
    goodsSearchService.deleteGoodsDocument("keywords", String.valueOf(goods.getId()));
  }

  public Page<SellerSalesListResponse> getSellerSalesList(Long sellerId, int page, int size) {

    Member seller = memberRepository.findById(sellerId)
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Goods> salesPage = goodsRepository.findByMemberId(sellerId, pageable);

    List<SellerSalesListResponse> salesList = salesPage.getContent().stream()
        .map(goods -> {
          String imageUrl =
              goods.getImageList().isEmpty() ? null : goods.getImageList().get(0).getImageUrl();

          return SellerSalesListResponse.builder()
              .goodsId(goods.getId())
              .sellerName(seller.getNickname())
              .goodsName(goods.getGoodsName())
              .price(String.valueOf(goods.getPrice()))
              .goodsThumbnail(imageUrl)
              .goodsStatus(goods.getGoodsStatus().getDescription())
              .uploadedBefore(getTradedBeforeSeconds(goods.getCreatedAt()))
              .build();
        }).collect(Collectors.toList());

    return new PageImpl<>(salesList, pageable, salesPage.getTotalElements());
  }

  private static long getTradedBeforeSeconds(LocalDateTime tradedDateTime) {
    return Duration.between(tradedDateTime, LocalDateTime.now()).getSeconds();
  }
}
