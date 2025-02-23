package com.cloudwave.lowcostchillguy.ticket.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketRequestDTO {
    private Long userId;
    private Long movieId;
    private String theater;  // 상영관 정보 (예: 5관, 6관)
    private String seat;     // 좌석 정보 (예: F-17)
}
