package it.greenvulcano.gvesb.iam.service;

import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

public interface ExternalAuthenticationService {

    String getProviderID();

    ExternalAutheticatedUser authenticate(String credentials) throws UserNotFoundException, UserExpiredException, UnverifiableUserException;

}