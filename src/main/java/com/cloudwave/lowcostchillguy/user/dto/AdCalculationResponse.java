package com.cloudwave.lowcostchillguy.user.dto;

import java.time.LocalDateTime;

public record AdCalculationResponse (
         String adUrl,
         Integer currentPosition,  // 초 단위
        LocalDateTime adStartTime
){}
