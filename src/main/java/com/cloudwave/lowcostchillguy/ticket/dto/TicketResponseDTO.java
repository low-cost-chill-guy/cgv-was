package com.cloudwave.lowcostchillguy.ticket.dto;

import com.cloudwave.lowcostchillguy.ticket.domain.Ticket;
import com.cloudwave.lowcostchillguy.ticket.domain.TicketStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TicketResponseDTO {
    private UUID ticketId;
    private String movieTitle;
    private LocalDateTime movieStartTime;
    private String theater;
    private String seat;
    private TicketStatus status;

    public TicketResponseDTO(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.movieTitle = ticket.getMovie().getMovieTitle();
        this.movieStartTime = ticket.getMovieStartTime();
        this.theater = ticket.getTheater();
        this.seat = ticket.getSeat();
        this.status = ticket.getStatus();
    }
}
