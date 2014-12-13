package com.soundcenter.soundcenter.client;

import javax.swing.JApplet;

import com.soundcenter.soundcenter.client.gui.UserInterface;

public class SoundCenterApplet extends JApplet {
	@Override
	public void init() {
		
		// start the runtime controller, which shuts down the applet
		new Thread(new AppletRuntimeController(Thread.currentThread())).start();
		
		App appStarter = new App();
		
		App.gui = new UserInterface();
		App.gui.createGlassPane(this);
		this.setContentPane(App.gui);
		
		appStarter.name = getParameter("minecraft-name");
		appStarter.address = getParameter("server-ip");
		appStarter.port = getParameter("soundcenter-port");
		
		appStarter.start();
	}
}
