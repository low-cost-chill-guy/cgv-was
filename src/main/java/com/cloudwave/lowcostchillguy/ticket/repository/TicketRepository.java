package com.cloudwave.lowcostchillguy.ticket.repository;



import com.cloudwave.lowcostchillguy.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUserId(Long userId);
}
