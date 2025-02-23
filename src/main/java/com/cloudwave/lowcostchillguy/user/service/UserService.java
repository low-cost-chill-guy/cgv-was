package com.cloudwave.lowcostchillguy.user.service;

import com.cloudwave.lowcostchillguy.ticket.domain.Ticket;
import com.cloudwave.lowcostchillguy.ticket.dto.UserTicketResponseDTO;
import com.cloudwave.lowcostchillguy.ticket.repository.TicketRepository;
import com.cloudwave.lowcostchillguy.user.domain.Users;
import com.cloudwave.lowcostchillguy.user.dto.LoginRequestDTO;
import com.cloudwave.lowcostchillguy.user.dto.SignupRequestDTO;
import com.cloudwave.lowcostchillguy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users") // 공통 url 추가
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; // UserRepository 추가


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) { // @RequestBody 추가
        // 1. 이메일로 유저 찾기
        Users user = userRepository.findByEmail(loginRequestDTO.email());

        //        // 2. 유저가 없거나 비밀번호가 틀린 경우 에러 응답
        if (user == null || !user.getPassword().equals(loginRequestDTO.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다."); // 401 Unauthorized
        }
        // 4. 토큰과 함께 성공 응답 반환
        return ResponseEntity.ok("로그인 성공"); // LoginResponseDTO 추가

    }


    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO signupRequestDTO) { // @RequestBody 추가
        Users user = new Users(signupRequestDTO.email(),signupRequestDTO.password(),signupRequestDTO.name());
        userRepository.save(user);

        return ResponseEntity.ok("회원가입 성공"); // LoginResponseDTO 추가
    }


}