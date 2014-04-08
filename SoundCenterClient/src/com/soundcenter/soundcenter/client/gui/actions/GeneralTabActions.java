package com.soundcenter.soundcenter.client.gui.actions;

import javax.swing.DefaultComboBoxModel;
import com.soundcenter.soundcenter.client.AppletStarter;
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
				port = Integer.parseInt(AppletStarter.gui.controller.getPort());
				
				//add name to player choosers
				String name = AppletStarter.gui.controller.getName();
				DefaultComboBoxModel playerMusicModel = (DefaultComboBoxModel) AppletStarter.gui.musicTab.playerComboBox.getModel();
				DefaultComboBoxModel playerStationsModel = (DefaultComboBoxModel) AppletStarter.gui.stationsTab.playerComboBox.getModel();
				playerMusicModel.addElement(name);
				playerStationsModel.addElement(name);
				
				//start client
				Client.start(AppletStarter.gui.controller.getAddress(), port, name);
			} catch(NumberFormatException e) {
				AppletStarter.logger.w("Cannot connect to port" + port + "!\n" +
						"Port must be an integer.", null);
			}
		}
	}
	
	public static void muteVoiceButtonClicked() {
		boolean selected = AppletStarter.gui.generalTab.muteVoiceButton.isSelected();
		
		if (!selected) {
			AppletStarter.gui.generalTab.muteVoiceButton.setText("Voice muted");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_MUTE_VOICE, null, null);
			}
			AppletStarter.audioManager.stopVoice();
		} else {
			AppletStarter.gui.generalTab.muteVoiceButton.setText("Voice active");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_UNMUTE_VOICE, null, null);
			}
		}
	}
	
	public static void muteMusicButtonClicked() {
		boolean selected = AppletStarter.gui.generalTab.muteMusicButton.isSelected();
		
		if (!selected) {
			AppletStarter.gui.generalTab.muteMusicButton.setText("Music muted");
			AppletStarter.audioManager.stopMusic();
		} else {
			AppletStarter.gui.generalTab.muteMusicButton.setText("Music active");
		}
	}
}
