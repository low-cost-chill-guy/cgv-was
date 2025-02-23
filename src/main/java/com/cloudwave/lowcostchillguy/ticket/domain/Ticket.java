package com.cloudwave.lowcostchillguy.ticket.domain;

import com.cloudwave.lowcostchillguy.movie.domain.Movie;
import com.cloudwave.lowcostchillguy.user.domain.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;



@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
public class Ticket {

    @Id
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID ticketId = UUID.randomUUID();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;  // 티켓 소유 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // 예매한 영화

    @Column(nullable = false)
    private LocalDateTime movieStartTime; // 영화 시작 시간

    @Column(nullable = false)
    private String theater; // 상영관 정보 (예: 5관, 6관)

    @Column(nullable = false)
    private String seat; // 좌석 정보 (예: F-17, F-16)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status; // 티켓 상태 (예매 완료, 취소 등)

    public Ticket(Users user, Movie movie, String theater, String seat) {
        this.ticketId = UUID.randomUUID();
        this.user = user;
        this.movie = movie;
        this.movieStartTime = movie.getMovieStartTime();
        this.theater = theater;
        this.seat = seat;
        this.status = TicketStatus.BOOKED; // 기본값: 예매 완료
    }

    // 예매 취소 메서드
    public void cancelTicket() {
        this.status = TicketStatus.CANCELLED;
    }
}
