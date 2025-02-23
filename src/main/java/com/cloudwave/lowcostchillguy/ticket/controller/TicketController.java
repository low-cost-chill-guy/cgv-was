package com.cloudwave.lowcostchillguy.ticket.controller;

import com.cloudwave.lowcostchillguy.ticket.dto.TicketRequestDTO;
import com.cloudwave.lowcostchillguy.ticket.dto.TicketResponseDTO;
import com.cloudwave.lowcostchillguy.ticket.dto.UserTicketResponseDTO;
import com.cloudwave.lowcostchillguy.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // 티켓 예매 API
    @PostMapping("/book")
    public ResponseEntity<TicketResponseDTO> bookTicket(@RequestBody TicketRequestDTO requestDTO) {
        TicketResponseDTO ticket = ticketService.bookTicket(requestDTO);
        return ResponseEntity.ok(ticket);
    }

    // 사용자가 예매한 티켓 목록 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserTicketResponseDTO>> getUserTickets(@PathVariable Long userId) {
        List<UserTicketResponseDTO> tickets = ticketService.getUserTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    // 특정 티켓 상세 조회 API
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID ticketId) {
        TicketResponseDTO ticket = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    //  티켓 예매 취소 API
    @PutMapping("/cancel/{ticketId}")
    public ResponseEntity<TicketResponseDTO> cancelTicket(@PathVariable UUID ticketId) {
        TicketResponseDTO cancelledTicket = ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok(cancelledTicket);
    }
}
