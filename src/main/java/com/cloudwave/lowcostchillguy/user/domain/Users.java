package com.cloudwave.lowcostchillguy.user.domain;

import com.cloudwave.lowcostchillguy.movie.domain.Movie;
import com.cloudwave.lowcostchillguy.ticket.domain.TicketStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    public Users(String email,String password , String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}