package com.unity.goods.domain.chat.entity;

import com.unity.goods.domain.goods.entity.Goods;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chat_room_id")
  private Long id;
  private Long sellerId;
  private Long buyerId;
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "goods_id")
  private Goods goods;

  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
  @Builder.Default
  private List<ChatLog> chatLogs = new ArrayList<>();

  public void changeDate(LocalDateTime localDateTime) {
    this.updatedAt = localDateTime;
  }
}
