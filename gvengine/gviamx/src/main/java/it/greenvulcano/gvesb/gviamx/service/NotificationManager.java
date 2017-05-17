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
