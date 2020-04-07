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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest.NotificationStatus;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;
import it.greenvulcano.gvesb.gviamx.service.NotificationService;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.SearchCriteria;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class EmailChangeManager {

    private final static Logger LOG = LoggerFactory.getLogger(EmailChangeManager.class);

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

    public void createEmailChangeRequest(String currentEmailAddress, String newEmailAddress) throws UserNotFoundException, UserExistException {

        if (newEmailAddress == null || !newEmailAddress.matches(UserActionRequest.EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Invalid email: " + newEmailAddress);
        }

        try {
            usersManager.getUser(newEmailAddress.toLowerCase());
            throw new UserExistException(newEmailAddress);
        } catch (UserNotFoundException e) {

            if (usersManager.searchUsers(SearchCriteria.builder().byEmail(newEmailAddress.toLowerCase()).limitedTo(1).build()).getTotalCount() > 0) {
                throw new UserExistException(newEmailAddress);
            }
        }

        User user = usersManager.getUser(currentEmailAddress.toLowerCase());

        UserActionRequest request = repository.get(newEmailAddress.toLowerCase(), UserActionRequest.Action.UPDATE).orElseGet(UserActionRequest::newEmailChangeRequest);
        request.setUserId(user.getId());
        request.setEmail(newEmailAddress.toLowerCase());
        request.setExpiresIn(expireTime);
        request.setNotificationStatus(NotificationStatus.PENDING);

        String clearTextToken = secureRandom.ints(3, 11, 99).mapToObj(Integer::toString).collect(Collectors.joining());
        request.setToken(DigestUtils.sha256Hex(clearTextToken));

        repository.add(request);

        request.setClearToken(clearTextToken);
        notificationServices.stream().map(n -> new NotificationService.NotificationTask(n, request, repository, "update")).forEach(executor::submit);

    }

    public UserActionRequest retrieveEmailChangeRequest(String email, String token) {

        UserActionRequest request = repository.get(email.toLowerCase(), UserActionRequest.Action.UPDATE)
                                               .orElseThrow(() -> new IllegalArgumentException("No password reset request found for this email"));

        if (DigestUtils.sha256Hex(token).equals(request.getToken())) {

            if (request.getIssueTime().plusMillis(request.getExpiresIn()).isBefore(Instant.now())) {
                repository.remove(request.getId());
                throw new SecurityException("No password reset request found for this email");
            }

            return request;

        } else {
            throw new SecurityException("Token missmatch");
        }

    }

    public void consumeEmailChangeRequest(UserActionRequest request) {

        try {

            repository.remove(request.getId());
        } catch (Exception fatalException) {
            LOG.error("Fail to process  password reset request with id " + request.getId(), fatalException);

            throw fatalException;
        }
    }

}
