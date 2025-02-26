package com.cloudwave.lowcostchillguy.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Test endpoint working!");
	}

	@GetMapping("/highload")
	public ResponseEntity<String> highLoad() {
		// CPU 부하를 발생시키는 작업
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < 500) {
			// CPU 집약적인 계산 수행
			for (int i = 0; i < 1000000; i++) {
				Math.sqrt(i);
			}
		}
		return ResponseEntity.ok("High load processed!");
	}
}