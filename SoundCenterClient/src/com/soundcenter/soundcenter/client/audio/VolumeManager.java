package com.soundcenter.soundcenter.client.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.audio.player.PlayerController;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;

public class VolumeManager {

	private AudioManager audioManager = null;	
	
	private double masterVolume = 100.D;
	
	private List<Integer> priorities = Collections.synchronizedList(new ArrayList<Integer>());
	private int highestPriority = 10;

	public VolumeManager(AudioManager audioManager) {
		this.audioManager = audioManager;
	}
	
	public void setPlayerVolume(byte type, short id, byte volumePercent) {
		PlayerController controller = audioManager.getPlayer(type, id);
		if (controller != null) {
			setPlayerVolume(controller, volumePercent);
		}		
	}
	
	public void setPlayerVolume(PlayerController controller, byte volumePercent) {
		byte value = (byte) (volumePercent * masterVolume/100.D);
		byte priotizedVolume = prioritizeVolume(keepInBounds(value), controller.getPlayerPriority());
		
		boolean allowFade = controller.getType() != GlobalConstants.TYPE_VOICE;
		
		controller.setVolume(priotizedVolume, allowFade);
	}
	
	private byte prioritizeVolume(byte value, int priority) {
		byte volume = value;
		if (audioManager.playersOverlap()) {
			int percent = 100 - ((11-highestPriority) - (11-priority))*10;
			volume = (byte) ( ((double) percent / 100.0) * value);
		}
		return volume;
	}
	
	private synchronized void updateHighestPriority() {
		int min = 10;
		for (int prior : priorities) {
			if (prior < min) {
				min = prior;
			}
		}
		
		highestPriority = min;
	}
	
	public byte getMasterVolume() {
		return (byte) masterVolume;
	}
	
	public void setMasterVolume(byte value) {
		masterVolume = value;
		/* update biome players */
		for (Entry<Short, PlayerController> entry : App.audioManager.biomePlayers.entrySet()) {
			setPlayerVolume(entry.getValue(), (byte) 100);
		}
		/* update world players */
		for (Entry<Short, PlayerController> entry : App.audioManager.worldPlayers.entrySet()) {
			setPlayerVolume(entry.getValue(), (byte) 100);
		}
		/* update global player */
		if (audioManager.globalPlayer != null) {
			setPlayerVolume(audioManager.globalPlayer, (byte) 100);
		}
	}
	
		public synchronized void addPriority(Integer value) {
			priorities.add(value);
			updateHighestPriority();
		}
		public synchronized void removePriority(Integer value) {
			priorities.remove(value);
			updateHighestPriority();
		}
	
	public byte keepInBounds(byte value) {
		if (value > 100)
			value = 100;
		if (value < 0)
			value = 0;
		return value;
	}
}
