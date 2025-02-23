package com.cloudwave.lowcostchillguy.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class UserController {

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
//    //홈 화면 조회 API
    @GetMapping("/")
    public ResponseEntity<Void> getHome() {
        return ResponseEntity.ok().build();
    }

    //마이페이지 조회 API
    @GetMapping("/mypage")
    public ResponseEntity<Void> getMypage() {
        //return 값
        /**
         * 티켓 정보
         * - 영화 이름
         * - 영화 시간
         * - CGV 지점 이름 & 상영관*
         * - 광고?
         **/
        return ResponseEntity.ok().build();
    }
    // 9시 50분 영화 예매 -> 광고 on / 광고 길이 7분 ->  9시 57분 (영화시작)
    // 사용자가 53분에 입장 / 광고 3분 동안 진행중 / 4분 return
    // (영화 상영 시간 + 광고 길이) - 사용자가 접속/클릭한 시간 : 4분
    //
   // 미디어 -> 백 : url 주소, 동영상 길이,
    // 스케줄러 ?
    // 백이 이미 모든 데이터를 알고 있을 경우 : 스케줄링해서 날짜/시간/지점으로 그 날 광고 길이 계산해서 보여주기
    // 무거움, 쿼리 해야 됨
    // 백 -> 미디어 호출하는 방법은 안되나? ?!
    // 백- > 람다 -> 미디어
    // 미디어는 이미 done? -> 사전 정보 정보 다 알고 있음, s3에 객체 정보 저장
    // 사용자가 마이페이지 조회 버튼 클릭 -> 티켓 정보가 있을 때 kafka 이벤트 발행 (or 람다) ->
    // 백엔드 db ->
    // 영화 : 영화관 지점, 상영 시간, 영화 이름, 영화 id  <- db에 있는게 아쉽 (상영 시간은 매일 초기화됨, redis 캐싱)
    // 광고 :  광고 url , 광고 시간
    // 사용자 : 이름, 이메일, 비밀번호
    // 예매 정보 : 사용자 id, 영화 id
    //
}
