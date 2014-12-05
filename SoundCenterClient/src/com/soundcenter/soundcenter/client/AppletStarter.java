package com.soundcenter.soundcenter.client;

import java.io.File;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import com.soundcenter.soundcenter.client.audio.AudioManager;
import com.soundcenter.soundcenter.client.gui.UserInterface;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.util.AppletLogger;

public class AppletStarter extends JApplet {

	public static final double version = 0.113;
	public static String dataFolder = "";
	public static UserInterface gui = null;
	public static AppletLogger logger = null;
	public static Configuration config = null;
	public static AudioManager audioManager = null;

	@Override
	public void init() {
		try {
			Thread.currentThread().setName("Applet");

			dataFolder = System.getProperty("user.home") + File.separator + ".soundcenter" + File.separator;
			new File(dataFolder + "musicdata" + File.separator).mkdirs();

			// start the runtime controller, which shuts down the app
			new Thread(new AppletRuntimeController(Thread.currentThread())).start();

			final JApplet applet = this;

			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui = new UserInterface();
					gui.setOpaque(true);
					setContentPane(gui);
					gui.createGlassPane(applet);

					logger = new AppletLogger(Logger.getLogger(AppletStarter.class.getName()), gui);
					audioManager = new AudioManager();
					config = new Configuration();
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

					
					// autoconnect
					if (gui.controller.isAutoConnectActive()) {
						GeneralTabActions.connectButtonPressed();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
