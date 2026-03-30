package com.bhaumik18.medisync_core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hospital")
public class HospitalStatusController {
	
	@GetMapping("/status")
	public ResponseEntity<String> geStatus(){
		return ResponseEntity.ok("Core Service Vault is UNLOCKED. Token successfully verified!");
	}
}
