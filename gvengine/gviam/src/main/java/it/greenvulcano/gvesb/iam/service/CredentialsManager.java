/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.iam.service;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;

/**
 * 
 * Manage sucurity-related business operations involving a third party actor called "client"
 * 
 * (Yes, it is for OAuth2)
 * 
 */
public interface CredentialsManager {

    Credentials create(String username, String password, String clientUsername, String clientPassword) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, UnverifiableUserException;

    Credentials check(String accessToken) throws InvalidCredentialsException, CredentialsExpiredException;

    Credentials refresh(String refreshToken, String accessToken) throws InvalidCredentialsException, CredentialsExpiredException;

    Credentials create(String username, String provider) throws UserNotFoundException, UserExpiredException, PasswordMissmatchException, UnverifiableUserException;

}