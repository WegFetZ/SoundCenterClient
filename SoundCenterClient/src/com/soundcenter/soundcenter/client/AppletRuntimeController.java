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
		AppletStarter.logger.i("Shutting down...", null);
		Client.shutdown();
		AppletStarter.audioManager.recorder.stop();
		AppletStarter.config.save();

		AppletStarter.logger = null;
		AppletStarter.config = null;
		AppletStarter.gui = null;
	}
	
}
