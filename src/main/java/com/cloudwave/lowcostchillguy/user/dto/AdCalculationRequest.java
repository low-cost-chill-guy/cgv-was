package com.cloudwave.lowcostchillguy.user.dto;

import java.time.LocalDateTime;

public record AdCalculationRequest(
      LocalDateTime movieStartTime,
      LocalDateTime currentTime
){}