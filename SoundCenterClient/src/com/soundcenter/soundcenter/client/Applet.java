package com.soundcenter.soundcenter.client;

import java.io.File;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import com.soundcenter.soundcenter.client.audio.AudioManager;
import com.soundcenter.soundcenter.client.gui.UserInterface;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.util.AppletLogger;

public class Applet extends JApplet {

	public static final double version = 0.100;
	public static String dataFolder = ""; 
	public static AppletLogger logger = null;
	public static Configuration config = null;
	public static UserInterface gui = new UserInterface();
	public static AudioManager audioManager = new AudioManager();

	@Override
	public void init() {
		Thread.currentThread().setName("Applet");	
		
		dataFolder = System.getProperty("user.home") + File.separator + ".soundcenter" + File.separator;
		new File(dataFolder + "musicdata" + File.separator).mkdirs();
		
		logger = new AppletLogger(Logger.getLogger(Applet.class.getName()), gui);
		config = new Configuration();	

		//start the runtime controller, which shuts down the app
		new Thread(new AppletRuntimeController(Thread.currentThread())).start();
		
		config.load();

		String name = getParameter("minecraft-name");
		String address = getParameter("server-ip");
		String port = getParameter("soundcenter-port");
		if (name != null && name != "")
			gui.controller.setName(name);
		if (address != null && address != "")
			gui.controller.setAddress(address);
		if (port != null && port != "")
			gui.controller.setPort(port);

		final JApplet applet = this;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui.setOpaque(true);
					setContentPane(gui);
					gui.createGlassPane(applet);
				}
			});
		} catch (Exception e) {
			logger.s("Error while creating GUI:", e);
		}
		
		//autoconnect
		if (gui.controller.isAutoConnectActive()) {
			GeneralTabActions.connectButtonPressed();
		}
	}
	
}
