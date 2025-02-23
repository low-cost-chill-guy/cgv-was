//package com.cloudwave.lowcostchillguy.ad.domain;
//
//
//import jakarta.persistence.Column;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class Ad {
//    @Id
//    @GeneratedValue(generator = "UUID")
//    @Column(name = "id", updatable = false, nullable = false)
//    private UUID id;
//
//    @Column(unique = true)
//    private String movieTitle;
//
//    @Column
//    private LocalDateTime movieStartTime;
//
//    @Column
//    private String place;
//}