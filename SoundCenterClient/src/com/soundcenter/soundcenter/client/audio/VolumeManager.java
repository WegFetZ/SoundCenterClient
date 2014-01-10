package com.soundcenter.soundcenter.client.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.soundcenter.soundcenter.client.audio.player.PlayerController;

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
		byte actualVolume = prioritizeVolume(keepInBounds(value), controller.getPlayerPriority());
		
		if (!controller.isFading()) {
			controller.setVolume(actualVolume, false);
		}
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
	
	public void setMasterVolume(int value) {
		masterVolume = value;
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
