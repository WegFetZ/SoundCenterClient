package com.soundcenter.soundcenter.client.gui.actions;

import javax.swing.DefaultComboBoxModel;
import com.soundcenter.soundcenter.client.App;
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
				port = Integer.parseInt(App.gui.controller.getPort());
				
				//add name to player choosers
				String name = App.gui.controller.getName();
				DefaultComboBoxModel playerMusicModel = (DefaultComboBoxModel) App.gui.musicTab.playerComboBox.getModel();
				DefaultComboBoxModel playerStationsModel = (DefaultComboBoxModel) App.gui.stationsTab.playerComboBox.getModel();
				playerMusicModel.addElement(name);
				playerStationsModel.addElement(name);
				
				//start client
				Client.start(App.gui.controller.getAddress(), port, name);
			} catch(NumberFormatException e) {
				App.logger.w("Cannot connect to port" + port + "!\n" +
						"Port must be an integer.", null);
			}
		}
	}
	
	public static void muteVoiceButtonClicked() {
		boolean selected = App.gui.generalTab.muteVoiceButton.isSelected();
		
		if (!selected) {
			App.gui.generalTab.muteVoiceButton.setText("Voice muted");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_MUTE_VOICE, null, null);
			}
			App.audioManager.stopVoice();
		} else {
			App.gui.generalTab.muteVoiceButton.setText("Voice active");
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_UNMUTE_VOICE, null, null);
			}
		}
	}
	
	public static void muteMusicButtonClicked() {
		boolean selected = App.gui.generalTab.muteMusicButton.isSelected();
		
		if (!selected) {
			App.gui.generalTab.muteMusicButton.setText("Music muted");
			App.audioManager.stopMusic();
		} else {
			App.gui.generalTab.muteMusicButton.setText("Music active");
		}
	}
}
