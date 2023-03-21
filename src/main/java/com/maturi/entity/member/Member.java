package com.maturi.entity.member;

import com.maturi.dto.member.AreaInterDTO;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true)
  private String email;
  private String passwd;
  private String salt;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String nickName;
  private String profileImg;
  private String profile;
  private String contact;
  @Enumerated(EnumType.STRING)
  private MemberStatus status;
  @Embedded//Area에 @Embeddable이 있으면 생략가능함 -> 쓰는걸 권장
  private Area area;

  public void changeInterArea(Area area) {
    this.area = area;
  }

  public void removeArea() {
      this.area = null;
  }
}
