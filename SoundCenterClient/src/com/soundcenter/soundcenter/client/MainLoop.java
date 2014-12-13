package com.soundcenter.soundcenter.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.soundcenter.soundcenter.client.audio.player.PlayerController;
import com.soundcenter.soundcenter.client.util.IntersectionDetection;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.SCLocation;

public class MainLoop implements Runnable {

	private boolean exit = false;
	private SCLocation location = null;
	
	
	public void run() {
		Thread.currentThread().setName("StatusUpdater");
		
		long timeA;
		long timeB;
		int delay;
		
		short id;
		double dist;
		
		while(!exit) {
			timeA = System.currentTimeMillis();
			
			if (location != null && App.audioManager.isMusicActive()) {
				HashMap<Short, Double> boxesInRange = IntersectionDetection.getBoxesInRange(location);
				HashMap<Short, Double> areasInRange = IntersectionDetection.getAreasInRange(location);
				List<Short> biomesInRange = IntersectionDetection.getBiomeInRange(location);
				List<Short> worldsInRange = IntersectionDetection.getWorldInRange(location);
				
				//TODO: BORDERS!!!
				
				/* update box players */
				for (Entry<Short, PlayerController> entry : App.audioManager.boxPlayers.entrySet()) {
					id = entry.getKey();
	
					if (!boxesInRange.containsKey(id)) {
						App.audioManager.stopPlayer(GlobalConstants.TYPE_BOX, id);
					}
				}
				for (Entry<Short, Double> entry : boxesInRange.entrySet()) {
					id = entry.getKey();
					dist = entry.getValue();
					App.audioManager.updatePlayer(GlobalConstants.TYPE_BOX, id, dist);
				}
				
				/* update area players */
				for (Entry<Short, PlayerController> entry : App.audioManager.areaPlayers.entrySet()) {
					id = entry.getKey();
					
					if (!areasInRange.containsKey(id)) {					
						App.audioManager.stopPlayer(GlobalConstants.TYPE_AREA, id);
					}
				}
				for (Entry<Short, Double> entry : areasInRange.entrySet()) {
					id = entry.getKey();
					dist = entry.getValue();
					App.audioManager.updatePlayer(GlobalConstants.TYPE_AREA, id, dist);
				}
				
				/* update biome players */
				for (Entry<Short, PlayerController> entry : App.audioManager.biomePlayers.entrySet()) {
					id = entry.getKey();
	
					if (!biomesInRange.contains(id)) {					
						App.audioManager.stopPlayer(GlobalConstants.TYPE_BIOME, id);
					}
				}
				for (short biomeId : biomesInRange) {	
					App.audioManager.updatePlayer(GlobalConstants.TYPE_BIOME, biomeId, 0);
				}
				
				/* update world players */
				for (Entry<Short, PlayerController> entry : App.audioManager.worldPlayers.entrySet()) {
					id = entry.getKey();
	
					if (!worldsInRange.contains(id)) {					
						App.audioManager.stopPlayer(GlobalConstants.TYPE_WORLD, id);
					}
				}
				for (short worldId : worldsInRange) {	
					App.audioManager.updatePlayer(GlobalConstants.TYPE_WORLD, worldId, 0);
				}
			}
			
			timeB = System.currentTimeMillis();
			delay = (50) - (int) (timeB - timeA);
			
			if (delay < 0)
				delay = 0;
			try {
				Thread.sleep(delay);
			} catch(InterruptedException e) {}
		}
	}
	
	public void setLocation(SCLocation loc) {
		this.location = loc;
	}
	
	public void shutdown() {
		exit = true;
	}
	
}
