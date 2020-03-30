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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.mongodb.PasswordResetRequest;
import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest.NotificationStatus;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;
import it.greenvulcano.gvesb.gviamx.service.NotificationService;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class PasswordResetManager {

    private final static Logger LOG = LoggerFactory.getLogger(PasswordResetManager.class);

    private final ExecutorService executor = Executors.newWorkStealingPool();
    private final SecureRandom secureRandom = new SecureRandom();

    private final List<NotificationService> notificationServices = new ArrayList<>();

    private UserActionRepository repository;
    private UsersManager usersManager;
    private Long expireTime = 60 * 60 * 1024L;

    public void setNotificationServices(List<NotificationService> notificationServices) {

        this.notificationServices.clear();
        if (notificationServices != null && !notificationServices.isEmpty()) {
            this.notificationServices.addAll(notificationServices);
        }
    }

    public void setRepository(UserActionRepository repository) {

        this.repository = repository;
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

    public void createPasswordResetRequest(String email) throws UserNotFoundException, UnverifiableUserException {

        User user = usersManager.getUser(email.toLowerCase());

        if (user.getPassword().isPresent()) {

            PasswordResetRequest passwordResetRequest = repository.get(email.toLowerCase(), PasswordResetRequest.class).orElseGet(PasswordResetRequest::new);
            passwordResetRequest.setUser(user);
            passwordResetRequest.setEmail(email.toLowerCase());
            passwordResetRequest.setIssueTime(new Date());
            passwordResetRequest.setExpireTime(expireTime);
            passwordResetRequest.setNotificationStatus(NotificationStatus.PENDING);

            String clearTextToken = secureRandom.ints(3, 11, 99).mapToObj(Integer::toString).collect(Collectors.joining());
            passwordResetRequest.setToken(DigestUtils.sha256Hex(clearTextToken));

            repository.add(passwordResetRequest);

            passwordResetRequest.setClearToken(clearTextToken);
            notificationServices.stream().map(n -> new NotificationService.NotificationTask(n, passwordResetRequest, repository, "reset")).forEach(executor::submit);

        } else {
            throw new UnverifiableUserException(email);
        }
    }

    public PasswordResetRequest retrievePasswordResetRequest(String email, String token) {

        PasswordResetRequest signupRequest = repository.get(email.toLowerCase(), PasswordResetRequest.class)
                                                       .orElseThrow(() -> new IllegalArgumentException("No password reset request found for this email"));

        if (DigestUtils.sha256Hex(token).equals(signupRequest.getToken())) {

            if (System.currentTimeMillis() > signupRequest.getIssueTime().getTime() + signupRequest.getExpireTime()) {
                repository.remove(signupRequest);
                throw new SecurityException("No password reset request found for this email");
            }

            return signupRequest;

        } else {
            throw new SecurityException("Token missmatch");
        }

    }

    public void consumePasswordResetRequest(PasswordResetRequest passwordResetRequest) {

        try {

            repository.remove(passwordResetRequest);
        } catch (Exception fatalException) {
            LOG.error("Fail to process  password reset request with id " + passwordResetRequest.getId(), fatalException);

            throw fatalException;
        }
    }

}
