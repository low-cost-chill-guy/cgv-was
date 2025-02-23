package com.cloudwave.lowcostchillguy.ticket.dto;

import com.cloudwave.lowcostchillguy.ticket.domain.Ticket;
import com.cloudwave.lowcostchillguy.ticket.domain.TicketStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class UserTicketResponseDTO {
    private String ticketNumber;  // ticketNumber
    private String movieTitle;
    private String date;
    private String time;
    private String theater;
    private String seat;
    private TicketStatus status;
    private String adUrl;

    public UserTicketResponseDTO(Ticket ticket,String adUrl) {
        this.ticketNumber = generateTicketNumber(ticket.getTicketId(), ticket.getMovieStartTime());
        this.movieTitle = ticket.getMovie().getMovieTitle();
        this.date = formatDate(ticket.getMovieStartTime());
        this.time = formatTime(ticket.getMovieStartTime());
        this.theater = ticket.getTheater();
        this.seat = ticket.getSeat();
        this.status = ticket.getStatus();
        this.adUrl = adUrl;
    }


    private String generateTicketNumber(UUID ticketId, LocalDateTime movieStartTime) {
        String formattedDate = movieStartTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int ticketNumberSuffix = ticketId.hashCode();
        return "TICKET-" + formattedDate + Math.abs(ticketNumberSuffix) % 100;
    }


    private String formatDate(LocalDateTime movieStartTime) {
        return movieStartTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }


    private String formatTime(LocalDateTime movieStartTime) {
        return movieStartTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
