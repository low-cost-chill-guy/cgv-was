package com.cloudwave.lowcostchillguy.ticket.service;

import org.springframework.stereotype.Service;

import com.cloudwave.lowcostchillguy.movie.domain.Movie;
import com.cloudwave.lowcostchillguy.movie.repository.MovieRepository;
import com.cloudwave.lowcostchillguy.ticket.domain.Ticket;
import com.cloudwave.lowcostchillguy.ticket.dto.TicketRequestDTO;
import com.cloudwave.lowcostchillguy.ticket.dto.TicketResponseDTO;
import com.cloudwave.lowcostchillguy.ticket.repository.TicketRepository;
import com.cloudwave.lowcostchillguy.user.domain.Users;
import com.cloudwave.lowcostchillguy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    // 🎟 티켓 예매
    @Transactional
    public TicketResponseDTO bookTicket(TicketRequestDTO requestDTO) {
        Users user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Movie movie = movieRepository.findById(requestDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("영화를 찾을 수 없습니다."));

        Ticket ticket = new Ticket(user, movie, requestDTO.getTheater(), requestDTO.getSeat());
        ticketRepository.save(ticket);

        return new TicketResponseDTO(ticket);
    }

    // 🎟 특정 사용자의 티켓 목록 조회
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getUserTickets(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream().map(TicketResponseDTO::new).collect(Collectors.toList());
    }

    // 🎟 특정 티켓 상세 조회
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));
        return new TicketResponseDTO(ticket);
    }

    // 🎟 티켓 예매 취소
    @Transactional
    public TicketResponseDTO cancelTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));

        ticket.cancelTicket();
        ticketRepository.save(ticket);

        return new TicketResponseDTO(ticket);
    }
}
