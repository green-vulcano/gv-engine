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

import it.greenvulcano.gvesb.gviamx.domain.UserActionRequest;

public interface NotificationManager {

	public void sendNotification(UserActionRequest userActionRequest, String event);
	
	public static class NotificationTask implements Runnable {

		private final NotificationManager notificationManager;		
		private final UserActionRequest userActionRequest;
		private final String event;
			
		public NotificationTask(NotificationManager notificationManager, UserActionRequest userActionRequest, String event) {
			this.notificationManager = notificationManager;
			this.userActionRequest = userActionRequest;
			this.event = event;
		}

		@Override
		public void run() {
			notificationManager.sendNotification(userActionRequest, event);
			
		}
		
	}
	
}
