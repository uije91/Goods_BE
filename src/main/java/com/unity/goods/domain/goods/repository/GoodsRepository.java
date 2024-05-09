package com.unity.goods.domain.goods.repository;

import com.unity.goods.domain.goods.entity.Goods;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

  Optional<Goods> findById(Long id);

}
