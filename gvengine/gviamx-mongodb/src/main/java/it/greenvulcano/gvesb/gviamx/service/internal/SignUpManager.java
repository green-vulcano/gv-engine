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
package it.greenvulcano.gvesb.gviamx.service.internal;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest.NotificationStatus;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;
import it.greenvulcano.gvesb.gviamx.service.CallBackService;
import it.greenvulcano.gvesb.gviamx.service.NotificationService;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class SignUpManager {

    private final static Logger LOG = LoggerFactory.getLogger(SignUpManager.class);

    private final ExecutorService executor = Executors.newWorkStealingPool();
    private final SecureRandom secureRandom = new SecureRandom();

    private final List<NotificationService> notificationServices = new LinkedList<>();
    private final List<CallBackService> callbackServices = new LinkedList<>();

    private UserActionRepository signupRepository;
    private UsersManager usersManager;
    private Long expireTime = 60 * 60 * 1024L;
    private final Set<String> defaultRoles = new LinkedHashSet<>();

    public void setNotificationServices(List<NotificationService> notificationServices) {

        this.notificationServices.clear();
        if (notificationServices != null && !notificationServices.isEmpty()) {
            this.notificationServices.addAll(notificationServices);
        }
    }

    public void setCallbackServices(List<CallBackService> callbackServices) {

        this.callbackServices.clear();
        if (callbackServices != null && !callbackServices.isEmpty()) {
            this.callbackServices.addAll(callbackServices);
        }
    }

    public void setRepository(UserActionRepository signupRepository) {

        this.signupRepository = signupRepository;
    }

    public void setUsersManager(UsersManager usersManager) {

        this.usersManager = usersManager;
    }

    public UsersManager getUsersManager() {

        return usersManager;
    }

    public void setExpireTime(Long expireTime) {

        this.expireTime = expireTime;
    }

    public void setDefaultRoles(String roles) {

        Optional.ofNullable(roles).ifPresent(r -> {

            defaultRoles.clear();

            Stream.of(r.split(",")).map(String::trim).filter(roleName -> roleName.matches(Role.ROLE_PATTERN)).forEach(defaultRoles::add);

        });
    }

    public void setDefaultRoles(Set<String> roles) {

        defaultRoles.addAll(roles);
    }

    public Set<String> getDefaultRoles() {

        return defaultRoles;
    }

    public void createSignUpRequest(String email, byte[] request) throws UserExistException {

        if (email == null || !email.matches(UserActionRequest.EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }

        try {
            usersManager.getUser(email.toLowerCase());
            throw new UserExistException(email);
        } catch (UserNotFoundException e) {
           
        }

        UserActionRequest signUpRequest = signupRepository.get(email.toLowerCase(), UserActionRequest.Action.SIGNUP).orElseGet(UserActionRequest::newSignupRequest);
        signUpRequest.setEmail(email.toLowerCase());
        signUpRequest.setExpiresIn(expireTime);
        signUpRequest.setRequest(request);
        signUpRequest.setNotificationStatus(NotificationStatus.PENDING);

        String clearTextToken = secureRandom.ints(3, 11, 99).mapToObj(Integer::toString).collect(Collectors.joining());
        signUpRequest.setToken(DigestUtils.sha256Hex(clearTextToken));

        signupRepository.add(signUpRequest);

        signUpRequest.setClearToken(clearTextToken);
        notificationServices.stream().map(n -> new NotificationService.NotificationTask(n, signUpRequest, signupRepository, "signup")).forEach(executor::submit);

    }

    public UserActionRequest retrieveSignUpRequest(String email, String token) {

        UserActionRequest signupRequest = signupRepository.get(email.toLowerCase(), UserActionRequest.Action.SIGNUP)
                                                      .orElseThrow(() -> new IllegalArgumentException("No sign-up request found for this email"));

        if (DigestUtils.sha256Hex(token).equals(signupRequest.getToken())) {

            if (signupRequest.getIssueTime().plusMillis(signupRequest.getExpiresIn()).isBefore(Instant.now())) {
                signupRepository.remove(signupRequest.getId());
                throw new IllegalArgumentException("No sign-up request found for this email");
            }

            return signupRequest;

        } else {
            throw new SecurityException("Token missmatch");
        }

    }

    public void consumeSignUpRequest(UserActionRequest signupRequest) {

        try {
            signupRepository.remove(signupRequest.getId());
            callbackServices.stream().map(c -> new CallBackService.CallBackTask(c, Base64.getDecoder().decode(signupRequest.getRequest()))).forEach(executor::submit);

        } catch (Exception fatalException) {
            LOG.error("Fail to process sign-up request with id " + signupRequest.getId(), fatalException);
            usersManager.deleteUser(signupRequest.getEmail());
            throw fatalException;
        }
    }

}
