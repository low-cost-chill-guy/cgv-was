//package com.cloudwave.lowcostchillguy.user.controller;
//
//import com.cloudwave.lowcostchillguy.user.dto.AdCalculationResponse;
//import com.cloudwave.lowcostchillguy.user.service.LambdaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.CompletableFuture;
//
//@RestController
//@RequiredArgsConstructor
//public class AdController {
//    private final LambdaService lambdaService;
//
//    @GetMapping("/ads/position")
//    public CompletableFuture<ResponseEntity<AdCalculationResponse>> getAdPosition(
//            @RequestParam LocalDateTime movieStartTime) {
//
//        return lambdaService.calculateAdPosition(movieStartTime, LocalDateTime.now())
//                .thenApply(ResponseEntity::ok)
//                .exceptionally(e -> ResponseEntity.internalServerError().build());
//    }
//}