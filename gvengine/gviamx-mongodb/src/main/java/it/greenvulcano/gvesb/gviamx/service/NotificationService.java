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
package it.greenvulcano.gvesb.gviamx.service;

import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;

public interface NotificationService {

	public void sendNotification(UserActionRequest userActionRequest, UserActionRepository userActionRepository,  String event);
	
	public static class NotificationTask implements Runnable {
		
		private final NotificationService notificationService;		
		private final UserActionRequest userActionRequest;
		private final UserActionRepository userActionRepository;
		private final String event;
			
		public NotificationTask(NotificationService notificationService, UserActionRequest userActionRequest, UserActionRepository userActionRepository, String event) {
			this.notificationService = notificationService;
			this.userActionRequest = userActionRequest;
			this.userActionRepository = userActionRepository;
			this.event = event;
		}

		@Override
		public void run() {
			notificationService.sendNotification(userActionRequest, userActionRepository,  event);
			
		}
		
	}
	
}
