package com.unity.goods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableWebSocketMessageBroker
public class GoodsApplication {

  public static void main(String[] args) {
    SpringApplication.run(GoodsApplication.class, args);
  }
}
