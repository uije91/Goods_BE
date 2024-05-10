package com.unity.goods.domain.goods.controller;

import com.unity.goods.domain.goods.dto.WishlistDto;
import com.unity.goods.domain.goods.service.WishService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class WishController {

  private final WishService wishService;

  @GetMapping("/likes")
  public ResponseEntity<?> getWishList(
      @AuthenticationPrincipal UserDetailsImpl principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("uploadBefore").descending());
    Page<WishlistDto> wishlistPage = wishService.getWishlist(principal.getId(), pageable);
    return ResponseEntity.ok().body(wishlistPage);
  }

  @PostMapping("/{goodsId}/likes")
  public ResponseEntity<?> addWishlist(
      @PathVariable Long goodsId, @AuthenticationPrincipal UserDetailsImpl principal) {

    wishService.addWishlist(goodsId, principal.getId());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{goodsId}/likes")
  public ResponseEntity<?> deleteWishlist(
      @PathVariable Long goodsId, @AuthenticationPrincipal UserDetailsImpl principal) {
    wishService.deleteWishlist(goodsId, principal.getId());
    return ResponseEntity.ok().build();
  }
}
