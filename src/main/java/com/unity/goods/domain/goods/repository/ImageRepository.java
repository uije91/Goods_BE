package com.unity.goods.domain.goods.repository;

import com.unity.goods.domain.goods.entity.Image;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
  List<Image> findByGoodsId(Long goodsId);

}
