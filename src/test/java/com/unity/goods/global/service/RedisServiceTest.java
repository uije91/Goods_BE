package com.unity.goods.global.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unity.goods.infra.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisServiceTest {

  @Autowired
  RedisService redisService;

  private final Long expiredTime = 5000L; //5초

  @Test
  @DisplayName("데이터 저장-만료기간설정")
  void setDataExpire_test() throws InterruptedException {
    redisService.setDataExpire("TEST", "1234",expiredTime);
    String redisData = redisService.getData("TEST");

    assertNotNull(redisData);
    assertEquals("1234", redisData);

    //데이터 만료 확인
    Thread.sleep(expiredTime);
    redisData = redisService.getData("TEST");
    assertNull(redisData);
  }

  @Test
  @DisplayName("데이터 저장")
  void setData_test() {
    redisService.setData("TEST", "1234");
    String redisData = redisService.getData("TEST");

    assertNotNull(redisData);
    assertEquals("1234", redisData);
  }

  @Test
  @DisplayName("데이터 조회")
  void getData_test() {
    redisService.setDataExpire("TEST", "1234", expiredTime);
    String values = redisService.getData("TEST");

    assertEquals("1234", values);
  }

  @Test
  @DisplayName("데이터 삭제")
  void deleteData_test() {
    redisService.setDataExpire("TEST", "1234", expiredTime);
    redisService.deleteData("TEST");

    String values = redisService.getData("TEST");
    assertNull(values);
  }

  @Test
  @DisplayName("데이터 존재여부 확인")
  void existData_test() {
    redisService.setDataExpire("TEST", "1234", expiredTime);
    boolean exists = redisService.existData("TEST");

    assertTrue(exists);
  }
}
