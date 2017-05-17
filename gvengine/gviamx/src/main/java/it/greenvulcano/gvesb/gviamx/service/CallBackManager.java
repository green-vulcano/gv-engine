package it.greenvulcano.gvesb.gviamx.service;

public interface CallBackManager {

	public void performCallBack(byte[] payload);
	
	public static class CallBackTask implements Runnable {

		private final CallBackManager callback;
		private final byte[] payload;
				
		public CallBackTask(CallBackManager callback, byte[] payload) {			
			this.callback = callback;
			this.payload = payload;
		}
		
		@Override
		public void run() {
			callback.performCallBack(payload);
			
		}
		
	}
	
}
