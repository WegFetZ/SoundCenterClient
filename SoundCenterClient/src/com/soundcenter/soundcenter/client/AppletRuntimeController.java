package com.soundcenter.soundcenter.client;

public class AppletRuntimeController implements Runnable {
	
	Thread appletThread = null;
	public AppletRuntimeController(Thread appletThread) {
		this.appletThread = appletThread;
	}
	
	
	public void run() {
		Thread.currentThread().setName("AppletRuntimeController");
		
		while(appletThread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		shutdown();
	}

	public void shutdown() {
		App.logger.i("Shutting down...", null);
		Client.shutdown();
		App.audioManager.recorder.stop();
		App.config.save();

		App.logger = null;
		App.config = null;
		App.gui = null;
	}
	
}
