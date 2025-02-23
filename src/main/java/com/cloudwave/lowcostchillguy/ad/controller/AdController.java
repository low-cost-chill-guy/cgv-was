package com.cloudwave.lowcostchillguy.ad.controller;


import com.cloudwave.lowcostchillguy.ad.domain.Ad;
import com.cloudwave.lowcostchillguy.ad.dto.AdRequstDTO;
import com.cloudwave.lowcostchillguy.ad.service.AdService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ads")
public class AdController {

	private final AdService adService;

	@PostMapping("/add")
	@ResponseStatus(HttpStatus.CREATED)
	public Ad addAd(@RequestBody AdRequstDTO adRequstDTO) {
		return adService.addAdForPlace(adRequstDTO);
	}
}
