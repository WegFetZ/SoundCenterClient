package com.soundcenter.soundcenter.client.gui.actions;

import javax.swing.DefaultComboBoxModel;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class GeneralTabActions {

	public static void tabOpened() {
	}
	
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
				DefaultComboBoxModel<String> playerStationsModel = (DefaultComboBoxModel<String>) App.gui.stationsTab.playerComboBox.getModel();
				playerStationsModel.addElement(name);
				
				//start client
				Client.start(App.gui.controller.getAddress(), port, name);
			} catch(NumberFormatException e) {
				App.logger.w("Cannot connect to port" + port + "!\n" +
						"Port must be an integer.", null);
			}
		}
	}
	
	public static void voiceActiveCheckBoxChanged() {
		boolean selected = App.gui.generalTab.voiceActiveCheckBox.isSelected();
		
		if (!selected) {
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_MUTE_VOICE, null, null);
			}
			App.audioManager.stopVoice();
		} else {
			if (Client.tcpClient != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_UNMUTE_VOICE, null, null);
			}
		}
	}
	
	public static void stationsActiveCheckBoxChanged() {
		boolean selected = App.gui.generalTab.stationsActiveCheckBox.isSelected();
		
		if (!selected) {
			App.audioManager.stopStations();
		}
	}
	
	public static void singleSongsActiveCheckBoxChanged() {
		boolean selected = App.gui.generalTab.singleSongsActiveCheckBox.isSelected();
		
		if (!selected) {
			App.audioManager.stopSingleSongs();
		}
	}
}
