package com.soundcenter.soundcenter.client.gui.actions;

import javax.swing.DefaultComboBoxModel;
import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class GeneralTabActions {

	public static void connectButtonPressed() {
		if (Client.active) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_CON_INFO_DISCONNECT, null, null);
			
			//wait for disconnect packet do be sent
			try { Thread.sleep(100); } catch(InterruptedException e) {}
			
			Client.reconnect = false;
			Client.shutdown();
		} else {
			int port = 0;
			try {
				port = Integer.parseInt(Applet.gui.controller.getPort());
				
				//add name to player choosers
				String name = Applet.gui.controller.getName();
				DefaultComboBoxModel playerMusicModel = (DefaultComboBoxModel) Applet.gui.musicTab.playerComboBox.getModel();
				DefaultComboBoxModel playerStationsModel = (DefaultComboBoxModel) Applet.gui.stationsTab.playerComboBox.getModel();
				playerMusicModel.addElement(name);
				playerStationsModel.addElement(name);
				
				//start client
				Client.start(Applet.gui.controller.getAddress(), port, name);
			} catch(NumberFormatException e) {
				Applet.logger.w("Cannot connect to port" + port + "!\n" +
						"Port must be an integer.", null);
			}
		}
	}
	
	public static void muteVoiceButtonClicked() {
		boolean selected = Applet.gui.generalTab.muteVoiceButton.isSelected();
		
		if (!selected) {
			Applet.gui.generalTab.muteVoiceButton.setText("Voice muted");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_MUTE_VOICE, null, null);
			}
			Applet.audioManager.stopVoice();
		} else {
			Applet.gui.generalTab.muteVoiceButton.setText("Voice active");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_UNMUTE_VOICE, null, null);
			}
		}
	}
	
	public static void muteMusicButtonClicked() {
		boolean selected = Applet.gui.generalTab.muteMusicButton.isSelected();
		
		if (!selected) {
			Applet.gui.generalTab.muteMusicButton.setText("Music muted");
			Applet.audioManager.stopMusic();
		} else {
			Applet.gui.generalTab.muteMusicButton.setText("Music active");
		}
	}
}
