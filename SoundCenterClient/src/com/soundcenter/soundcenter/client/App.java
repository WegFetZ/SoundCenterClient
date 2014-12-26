package com.soundcenter.soundcenter.client;

import java.io.File;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.soundcenter.soundcenter.client.audio.AudioManager;
import com.soundcenter.soundcenter.client.gui.UserInterface;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.util.SCLogger;

public class App {

	public static final double version = 0.300;
	public static String dataFolder = "";
	public static UserInterface gui = null;
	public static SCLogger logger = null;
	public static Configuration config = null;
	public static AudioManager audioManager = null;
	
	public String name;
	public String address;
	public String port;
	
	public void start() {
		try {
		
			Thread.currentThread().setName("App");
	
			dataFolder = System.getProperty("user.home") + File.separator + ".soundcenter" + File.separator;
			new File(dataFolder + "musicdata" + File.separator).mkdirs();
			
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui.setOpaque(true);
	
					logger = new SCLogger(Logger.getLogger(App.class.getName()), gui);
					logger.i("SoundCenterClient v" + version + " started.", null);
					config = new Configuration();
					audioManager = new AudioManager();
					config.load();
					
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
