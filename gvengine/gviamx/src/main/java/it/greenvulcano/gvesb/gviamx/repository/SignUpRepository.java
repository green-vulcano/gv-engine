package it.greenvulcano.gvesb.gviamx.repository;

import java.util.Optional;

import it.greenvulcano.gvesb.gviamx.domain.SignUpRequest;

public interface SignUpRepository extends Repository<SignUpRequest, Long> {
	
	Optional<SignUpRequest> get(String email);
	
}