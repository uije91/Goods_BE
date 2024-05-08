package com.unity.goods.domain.goods.repository;

import com.unity.goods.domain.goods.entity.GoodsImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsImageRepository extends JpaRepository<GoodsImage, Long> {

}
