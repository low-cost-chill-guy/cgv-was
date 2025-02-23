//package com.cloudwave.lowcostchillguy.user.service;
//
//import com.amazonaws.services.lambda.AWSLambda;
//import com.amazonaws.services.lambda.model.InvocationType;
//import com.amazonaws.services.lambda.model.InvokeRequest;
//import com.amazonaws.services.lambda.model.InvokeResult;
//import com.cloudwave.lowcostchillguy.user.dto.AdCalculationRequest;
//import com.cloudwave.lowcostchillguy.user.dto.AdCalculationResponse;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//@RequiredArgsConstructor
//public class LambdaService {
//    private final AWSLambda awsLambda;
//
//    @Value("${aws.lambda.function-name}")
//    private String functionName;
//
//    //비동기 방식 처리
//    @Async
//    public CompletableFuture<AdCalculationResponse> calculateAdPosition(Long userId) {
//        LocalDateTime currentTime = LocalDateTime.now();
//        //movieStartTime -> db에서 영화 시간 조회, 사용자 ID
//        // 예매 정보 : 사용자 ID에서 조회
//
//        AdCalculationRequest request = new AdCalculationRequest(movieStartTime, currentTime);
//
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                InvokeRequest invokeRequest = new InvokeRequest()
//                        .withFunctionName(functionName)
//                        .withInvocationType(InvocationType.Event)
//                        .withPayload(new ObjectMapper()
//                                .writeValueAsString(request));
//
//                InvokeResult result = awsLambda.invoke(invokeRequest);
//
//                return new ObjectMapper()
//                        .readValue(new String(result.getPayload().array()),
//                                AdCalculationResponse.class);
//
//            } catch (Exception e) {
//                throw new RuntimeException("Lambda 호출 실패", e);
//            }
//        });
//    }
//}