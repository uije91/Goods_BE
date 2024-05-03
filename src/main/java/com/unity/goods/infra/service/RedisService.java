package com.unity.goods.infra.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;

  // Key 를 이용해서 Redis 에서 Value 조회
  public String getData(String key) {
    return (String) redisTemplate.opsForValue().get(key);
  }

  // Key 를 이용해서 Redis 의 데이터 삭제
  public void deleteData(String key) {
    redisTemplate.delete(key);
  }

  // Redis 에 값 저장
  public void setData(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  // Redis 에 값 저장(만료설정 -> 자동삭제)
  public void setDataExpire(String key, String value, Long expiredTime) {
    redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
  }

  // Redis 에 값이 존재하는지 확인
  public boolean existData(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

}
