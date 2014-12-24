package com.soundcenter.soundcenter.client.audio;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.audio.player.PlayerController;
import com.soundcenter.soundcenter.client.audio.player.WebPlayer;
import com.soundcenter.soundcenter.client.audio.player.VoicePlayer;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class AudioManager {

	public Recorder recorder = new Recorder();
	public VolumeManager volumeManager = new VolumeManager(this);

	public ConcurrentHashMap<Short, PlayerController> areaPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> boxPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> biomePlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> worldPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> wgRegionPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> voicePlayers = new ConcurrentHashMap<Short, PlayerController>();
	public PlayerController globalPlayer = null;

	public AudioManager() {
		new Thread(recorder).start();
	}

	public void feedVoicePacket(UdpPacket packet) {
		byte type = packet.getType();
		short id = packet.getID();
		PlayerController controller = getPlayer(type, id);
		if (controller == null) {
			// create a new player for voice streams and global streams
			if (type == GlobalConstants.TYPE_VOICE && isVoiceActive()) {
				if (!Client.database.isMuted(type, id)) {
					controller = createNewPlayer(type, id);
					if (controller == null) {
						return;
					}
				}
			} else {
				return;
			}
		}
		if (controller instanceof VoicePlayer) {
			((VoicePlayer) controller).addToQueue(packet);
		}
	}
	
	public void updatePlayer(byte type, short id, double dist) {
		PlayerController controller = getPlayer(type, id);
		if (controller == null) {
			Station station = Client.database.getStation(type, id);
			if (station == null || (station.getSongs().isEmpty())
					|| Client.database.isMuted(type, id)) {
				return;
			}
			controller = createNewPlayer(type, id);
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
		} else if (type == GlobalConstants.TYPE_WORLD || type == GlobalConstants.TYPE_BIOME || type == GlobalConstants.TYPE_WGREGION) {
			volumeManager.setPlayerVolume(controller, (byte) 100);
		}
	}
	
	public void playGlobal(Song song) {
		if (globalPlayer != null) {
			globalPlayer.close(false);
		}
		globalPlayer = new WebPlayer(GlobalConstants.TYPE_GLOBAL, (short) 1);
		globalPlayer.setSingleSong(song);
		globalPlayer.start();
		App.gui.controller.setPlayButtonText("Stop Globally");
		
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
	public void setMusicActive(boolean value) {
		App.gui.controller.setMusicActive(value);
		if (!value) {
			stopMusic();
		}
	}
	public boolean isMusicActive() {
		return App.gui.controller.isMusicActive();
	}

	public boolean isPlayingGlobally() {
		return (globalPlayer != null);
	}
	
	public boolean playersOverlap() {
		//ceck if there is more than one player (excluding speex players)
		int players = (areaPlayers.size() + boxPlayers.size() + biomePlayers.size() + worldPlayers.size() + wgRegionPlayers.size());
		return players > 1 || (players == 1 && globalPlayer != null);
	}

	public PlayerController getPlayer(byte type, short id) {
		
		if (type == GlobalConstants.TYPE_GLOBAL) {
			return globalPlayer;
		}
		
		ConcurrentHashMap<Short, PlayerController> map = getPlayerMap(type);
		if (map != null) {
			return map.get(id);
		}
		
		return null;
	}
	
	public void putPlayer(byte type, short id, PlayerController controller) {

		ConcurrentHashMap<Short, PlayerController> map = getPlayerMap(type);
		if (map != null) {
			map.put(id, controller);
		}
		
		if (type == GlobalConstants.TYPE_GLOBAL) {
			globalPlayer = controller;
		}
	}	
	
	public void removePlayer(byte type, short id, PlayerController controller) {

		ConcurrentHashMap<Short, PlayerController> map = getPlayerMap(type);
		if (map != null) {
			//prevent players from removing other players
			if (map.get(id) == controller) {
				map.remove(id);
			}
		}
		
		if (type == GlobalConstants.TYPE_GLOBAL) {
			globalPlayer = null;
			App.gui.controller.setPlayButtonText("Play Globally");
		}
	}
	
	private PlayerController createNewPlayer(byte type, short id) {
		
		if (!Client.database.hasStation(type, id)) {
			return null;
		}
		
		App.logger.d("Creating new player. Type: " + type + " id: " + id, null);
		
		PlayerController controller = null;
		
		Station station = Client.database.getStation(type, id);
		ConcurrentHashMap<Short, PlayerController> map = getPlayerMap(type);
		if (map != null) {			
			if (station == null && type == GlobalConstants.TYPE_VOICE) {
				controller = new VoicePlayer(type, id);
			
			} else if (station != null) {
				controller = new WebPlayer(type, id);
			}
			
			map.put(id, controller);
		}
		
		if (controller != null) {
			//start the player
			controller.start();
		}
		
		return controller;
	}
	
	public void stopPlayer(final byte type, final short id, boolean preventRestart) {
		PlayerController controller = getPlayer(type, id);
		if (controller != null) {
			controller.close(preventRestart);
		}
	}
	
	public void stopAll() {	
		stopMusic();
		stopVoice();
		recorder.stop();
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
	
	public void stopMusic() {
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
				
				if (globalPlayer != null) {
					globalPlayer.close(true);
				}
				
				areaPlayers.clear();
				boxPlayers.clear();
				biomePlayers.clear();
				worldPlayers.clear();
				wgRegionPlayers.clear();
				globalPlayer = null;
				if (App.gui != null) {
					App.gui.controller.setPlayButtonText("Play Globally");
				}
			}
		}).start();
	}
	
	private ConcurrentHashMap<Short, PlayerController> getPlayerMap(byte type) {
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
