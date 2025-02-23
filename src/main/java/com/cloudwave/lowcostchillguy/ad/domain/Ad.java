package com.cloudwave.lowcostchillguy.ad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Ad {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column
   private Long time;

   @Column
   private String place;

   @Column
   private String adUrl;



   public Ad(Long time, String place, String adUrl) {
      this.time = time;
      this.place = place;
      this.adUrl = adUrl;
   }
}
