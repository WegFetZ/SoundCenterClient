package com.soundcenter.soundcenter.client.gui;

import com.soundcenter.soundcenter.client.Applet;

public class GuiController {
	
	private UserInterface mainPanel = null;
	
	public GuiController(UserInterface mainPanel) {
		this.mainPanel = mainPanel;
	}

	public void clearLog() {
		mainPanel.generalTab.logArea.setText("");
	}
	
	/* ------------------------ GENERAL TAB ----------------------------*/	
	public String getName() {
		return mainPanel.generalTab.userNameField.getText();
	}
	public void setName(String name) {
		mainPanel.generalTab.userNameField.setText(name);
	}
	
	public String getAddress() {
		return mainPanel.generalTab.serverAddressField.getText();
	}
	public void setAddress(String ip) {
		mainPanel.generalTab.serverAddressField.setText(ip);
	}
	
	public String getPort() {
		return mainPanel.generalTab.pluginPortField.getText();
	}
	public void setPort(String port) {
		mainPanel.generalTab.pluginPortField.setText(port);
	}
	
	public boolean isDebugActive() {
		return mainPanel.generalTab.debugCheckBox.isSelected();
	}
	public void setDebugActive(boolean value) {
		mainPanel.generalTab.debugCheckBox.setSelected(value);
	}
	
	public boolean isAutoConnectActive() {
		return mainPanel.generalTab.autoConnectCheckBox.isSelected();
	}
	public void setAutoConnectActive(boolean value) {
		mainPanel.generalTab.autoConnectCheckBox.setSelected(value);
	}
	
	public boolean isAutoReconnectActive() {
		return mainPanel.generalTab.autoReconnectCheckBox.isSelected();
	}
	public void setAutoReconnectActive(boolean value) {
		mainPanel.generalTab.autoReconnectCheckBox.setSelected(value);
	}
	
	public void setConnectButtonEnabled(boolean value) {
		mainPanel.generalTab.connectButton.setEnabled(value);
	}
	
	public void setConnectButtonText(String text) {
		mainPanel.generalTab.connectButton.setText(text);
	}
	
	public void setConnectionStatus(String text) {
		mainPanel.generalTab.statusLabel.setText(text);
	}
	
	public void setMasterVolume(int value, boolean updateSlider) {
		if (updateSlider) {
			mainPanel.generalTab.volumeSlider.setValue(value);
		}
		mainPanel.generalTab.volumeLabel.setText(String.valueOf(value));
		Applet.audioManager.volumeManager.setMasterVolume(value);
	}
	public int getMasterVolume() {
		return mainPanel.generalTab.volumeSlider.getValue();
	}
	
	public void setMusicActive(boolean value) {
		mainPanel.generalTab.muteMusicButton.setSelected(value);
		if (value) {
			mainPanel.generalTab.muteMusicButton.setText("Music active");
		} else {
			mainPanel.generalTab.muteMusicButton.setText("Music muted");
		}
	}
	public boolean isMusicActive() {
		return mainPanel.generalTab.muteMusicButton.isSelected();
	}
	
	public void setVoiceActive(boolean value) {
		mainPanel.generalTab.muteVoiceButton.setSelected(value);
		if (value) {
			mainPanel.generalTab.muteVoiceButton.setText("Voice active");
		} else {
			mainPanel.generalTab.muteVoiceButton.setText("Voice muted");
		}
	}
	public boolean isVoiceActive() {
		return mainPanel.generalTab.muteVoiceButton.isSelected();
	}
	
	public void disableConnectionDataFields() {
		mainPanel.generalTab.userNameField.setEnabled(false);
		mainPanel.generalTab.serverAddressField.setEnabled(false);
		mainPanel.generalTab.pluginPortField.setEnabled(false);
	}
	
	public void enableConnectionDataFields() {
		mainPanel.generalTab.userNameField.setEnabled(true);
		mainPanel.generalTab.serverAddressField.setEnabled(true);
		mainPanel.generalTab.pluginPortField.setEnabled(true);
	}
	
	public void setPlayButtonText(String text) {
		mainPanel.musicTab.playButton.setText(text);
	}
	
	public void setLoading(boolean value) {
		mainPanel.glassPane.setVisible(value);
	}
	
	public void chooseOwnPlayer() {
		mainPanel.musicTab.playerComboBox.setSelectedItem(getName());
		mainPanel.stationsTab.playerComboBox.setSelectedItem(getName());
		mainPanel.stationsTab.typeComboBox.setSelectedItem("Boxes");
	}
}
