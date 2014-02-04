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
		Applet.logger.i("Shutting down...", null);
		Client.shutdown();
		Applet.audioManager.recorder.stop();
		Applet.config.save();

		Applet.logger = null;
		Applet.config = null;
		Applet.gui = null;
	}
	
}
