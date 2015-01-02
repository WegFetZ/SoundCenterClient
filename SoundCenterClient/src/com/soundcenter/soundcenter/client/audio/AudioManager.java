package com.soundcenter.soundcenter.client.audio;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.audio.player.PlayerController;
import com.soundcenter.soundcenter.client.audio.player.SingleSongPlayer;
import com.soundcenter.soundcenter.client.audio.player.StationPlayer;
import com.soundcenter.soundcenter.client.audio.player.VoicePlayer;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class AudioManager {

	public Recorder recorder = new Recorder();
	public VolumeManager volumeManager = new VolumeManager(this);

	//station players
	public ConcurrentHashMap<Short, PlayerController> areaPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> boxPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> biomePlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> worldPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> wgRegionPlayers = new ConcurrentHashMap<Short, PlayerController>();
	//voice players are handled as station players
	public ConcurrentHashMap<Short, PlayerController> voicePlayers = new ConcurrentHashMap<Short, PlayerController>();
	//single song players
	public ConcurrentHashMap<Song, PlayerController> singleSongPlayers = new ConcurrentHashMap<Song, PlayerController>();

	public AudioManager() {
		new Thread(recorder).start();
	}

	public void feedVoicePacket(UdpPacket packet) {
		byte type = packet.getType();
		short id = packet.getID();
		PlayerController controller = getStationPlayer(type, id);
		if (controller == null) {
			if (!Client.database.isMuted(type, id)) {
				controller = createNewStationPlayer(type, id);
				if (controller == null) {
					return;
				}
			}
		}
		if (controller instanceof VoicePlayer) {
			((VoicePlayer) controller).addToQueue(packet);
		}
	}
	
	public void updateStationPlayer(byte type, short id, double dist) {
		PlayerController controller = getStationPlayer(type, id);
		if (controller == null) {
			Station station = Client.database.getStation(type, id);
			if (station == null || (station.getSongs().isEmpty())
					|| Client.database.isMuted(type, id)) {
				return;
			}
			controller = createNewStationPlayer(type, id);
			if (controller == null) {
				return;
			}

		}
		if (type == GlobalConstants.TYPE_BOX) {
			int range = Client.database.getStation(GlobalConstants.TYPE_BOX, id).getRange();
			byte volumePercent = (byte) (100 - (dist / range) * 100);
			if (volumePercent >= 0) {
				controller.allowPlayback();
				volumeManager.setPlayerVolume(controller, volumePercent);
			} else {
				volumeManager.setPlayerVolume(controller, (byte) 0);
			}

		} else if (type == GlobalConstants.TYPE_AREA) {
			int fadeout = Client.database.getStation(GlobalConstants.TYPE_AREA, id).getRange();
			if (dist >= 0) {
				controller.allowPlayback();
				if (fadeout > 0 && dist < fadeout) {
					byte volumePercent = (byte) ((100.0 / fadeout) * dist);
					volumeManager.setPlayerVolume(controller, volumePercent);
				} else {
					volumeManager.setPlayerVolume(controller, (byte) 100);
				}
			} else {
				volumeManager.setPlayerVolume(controller, (byte) 0);
			}
		} else {
			volumeManager.setPlayerVolume(controller, (byte) 100);
		}
	}
	
	public void playSingleSong(Song song) {
		if (singleSongPlayers.containsKey(song)) {
			return;
		}
		//TODO: add priority to single songs
		PlayerController controller = new SingleSongPlayer(song, 1);
		controller.start();
		
		singleSongPlayers.put(song, controller);
	}

	public void setVoiceActive(boolean value) {
		App.gui.controller.setVoiceActive(value);
		if (!value) {
			stopVoice();
		}
	}
	public boolean isVoiceActive() {
		return App.gui.controller.isVoiceActive();
	}
	public void setStationsActive(boolean value) {
		App.gui.controller.setStationsActive(value);
		if (!value) {
			stopStations();
		}
	}
	public boolean areStationsActive() {
		return App.gui.controller.areStationsActive();
	}
	public void setSingleSongsActive(boolean value) {
		App.gui.controller.setSingleSongsActive(value);
		if (!value) {
			stopSingleSongs();
		}
	}
	public boolean areSingleSongsActive() {
		return App.gui.controller.areSingleSongsActive();
	}
	
	public boolean playersOverlap() {
		//ceck if there is more than one player (excluding speex players)
		int players = (areaPlayers.size() + boxPlayers.size() + biomePlayers.size() + worldPlayers.size() + wgRegionPlayers.size() + singleSongPlayers.size());
		return players > 1;
	}

	public PlayerController getStationPlayer(byte type, short id) {
		
		ConcurrentHashMap<Short, PlayerController> map = getStationPlayerMap(type);
		if (map != null) {
			return map.get(id);
		}
		
		return null;
	}
	
	public void putStationPlayer(byte type, short id, PlayerController controller) {

		ConcurrentHashMap<Short, PlayerController> map = getStationPlayerMap(type);
		if (map != null) {
			map.put(id, controller);
		}

	}
	
	public void removeStationPlayer(byte type, short id, PlayerController controller) {

		ConcurrentHashMap<Short, PlayerController> map = getStationPlayerMap(type);
		if (map != null) {
			//prevent players from removing other players
			if (map.get(id) == controller) {
				map.remove(id);
			}
		}
	}
	
	public void removeSingleSongPlayer(Song song, PlayerController controller) {
		//prevent players from removing other players
		if (singleSongPlayers.get(song) == controller) {
			singleSongPlayers.remove(song);
		}
	}

	
	private PlayerController createNewStationPlayer(byte type, short id) {
		
		//voice players are handled as stations but not as such in database
		if (!Client.database.hasStation(type, id) && type != GlobalConstants.TYPE_VOICE) {
			return null;
		}
		
		App.logger.d("Creating new player. Type: " + type + " id: " + id, null);
		
		PlayerController controller = null;
		
		Station station = Client.database.getStation(type, id);
		ConcurrentHashMap<Short, PlayerController> map = getStationPlayerMap(type);
		if (map != null) {			
			if (station == null && type == GlobalConstants.TYPE_VOICE) {
				controller = new VoicePlayer(type, id);
			
			} else if (station != null) {
				controller = new StationPlayer(type, id);
			}
			
			map.put(id, controller);
		}
		
		if (controller != null) {
			//start the player
			controller.start();
		} else {
			App.logger.d("Failed to create new player. Type: " + type + " id: " + id, null);
		}
		
		return controller;
	}
	
	public void stopStationPlayer(final byte type, final short id, boolean preventRestart) {
		PlayerController controller = getStationPlayer(type, id);
		if (controller != null) {
			controller.close(preventRestart);
		}
	}
	
	public void stopSingleSongPlayer(Song song) {
		PlayerController controller = singleSongPlayers.get(song);
		if (controller != null) {
			controller.close(false);
		}
	}
	
	public void stopAll() {	
		stopStations();
		stopSingleSongs();
		stopVoice();
	}
	
	public void stopVoice() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (Entry<Short, PlayerController> entry : voicePlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(false);
				}
				
				voicePlayers.clear();
			}
		}).start();
	}
	
	public void stopStations() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (Entry<Short, PlayerController> entry : areaPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(true);
				}
				for (Entry<Short, PlayerController> entry : boxPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(true);
				}
				for (Entry<Short, PlayerController> entry : biomePlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(true);
				}
				for (Entry<Short, PlayerController> entry : worldPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(true);
				}
				for (Entry<Short, PlayerController> entry : wgRegionPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close(true);
				}
				
				areaPlayers.clear();
				boxPlayers.clear();
				biomePlayers.clear();
				worldPlayers.clear();
				wgRegionPlayers.clear();
			}
		}).start();
	}
	
	public void stopSingleSongs() {
		for (Entry<Song, PlayerController> entry : singleSongPlayers.entrySet()) {
			PlayerController controller = entry.getValue();
			controller.close(true);
		}
		singleSongPlayers.clear();
	}
	
	private ConcurrentHashMap<Short, PlayerController> getStationPlayerMap(byte type) {
		switch (type) {
		case GlobalConstants.TYPE_AREA:
			return areaPlayers;
		case GlobalConstants.TYPE_BOX:
			return boxPlayers;
		case GlobalConstants.TYPE_BIOME:
			return biomePlayers;
		case GlobalConstants.TYPE_WORLD:
			return worldPlayers;
		case GlobalConstants.TYPE_WGREGION:
			return wgRegionPlayers;
		case GlobalConstants.TYPE_VOICE:
			return voicePlayers;
		default:
			return null;
		}
	}

}
