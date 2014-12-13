package com.soundcenter.soundcenter.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.soundcenter.soundcenter.client.gui.UserInterface;

public class SoundCenterApplication {

	public static void main(String[] args) {
		
		App appStarter = new App();
		
		JFrame frame = new JFrame("SoundCenter Client v" + App.version);
		frame.addWindowListener(new WindowEventHandler());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(680, 360);
		App.gui = new UserInterface();
		App.gui.createGlassPane(frame);
		frame.setContentPane(App.gui);
		frame.setVisible(true);
		
		appStarter.start();
	}
	
}

class WindowEventHandler extends WindowAdapter {
	  public void windowClosing(WindowEvent evt) {
		  App.logger.i("Shutting down...", null);
			Client.shutdown();
			App.audioManager.recorder.stop();
			App.config.save();

			App.logger = null;
			App.config = null;
			App.gui = null;
			System.exit(0);
	  }
}
