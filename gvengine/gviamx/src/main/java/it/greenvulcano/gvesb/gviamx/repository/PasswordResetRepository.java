package it.greenvulcano.gvesb.gviamx.repository;

import java.util.Optional;

import it.greenvulcano.gvesb.gviamx.domain.PasswordResetRequest;

public interface PasswordResetRepository extends Repository<PasswordResetRequest, Long> {
	
	Optional<PasswordResetRequest> get(String email);	


}