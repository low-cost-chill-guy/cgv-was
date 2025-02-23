package com.cloudwave.lowcostchillguy.ad.service;

import com.cloudwave.lowcostchillguy.ad.domain.Ad;
import com.cloudwave.lowcostchillguy.ad.dto.AdRequstDTO;
import com.cloudwave.lowcostchillguy.ad.repository.AdRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdService {

	private final AdRepository adRepository;


	public Ad addAdForPlace(AdRequstDTO adRequstDTO) {
		Ad existingAd = adRepository.findByPlace(adRequstDTO.place())
			.orElse(null);

		if (existingAd != null) {
			throw new IllegalArgumentException("Advertisement for this place already exists.");
		}

		Ad newAd = new Ad(adRequstDTO.time(), adRequstDTO.place(), adRequstDTO.adUrl());
		return adRepository.save(newAd);
	}
}
