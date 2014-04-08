package com.soundcenter.soundcenter.client.audio;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.audio.player.MusicPlayer;
import com.soundcenter.soundcenter.client.audio.player.MidiPlayer;
import com.soundcenter.soundcenter.client.audio.player.PlayerController;
import com.soundcenter.soundcenter.client.audio.player.RadioPlayer;
import com.soundcenter.soundcenter.client.audio.player.VoicePlayer;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.tcp.MidiNotificationPacket;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class AudioManager {

	public Recorder recorder = new Recorder();
	public VolumeManager volumeManager = new VolumeManager(this);

	public ConcurrentHashMap<Short, PlayerController> areaPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> boxPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> biomePlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> worldPlayers = new ConcurrentHashMap<Short, PlayerController>();
	public ConcurrentHashMap<Short, PlayerController> voicePlayers = new ConcurrentHashMap<Short, PlayerController>();
	public PlayerController globalPlayer = null;

	public AudioManager() {
		new Thread(recorder).start();
	}

	public void feedPacket(UdpPacket packet) {
		byte type = packet.getType();
		short id = packet.getID();

		PlayerController controller = getPlayer(type, id);
		if (controller == null) {
			//create a new player for voice streams and global streams
			if (type == GlobalConstants.TYPE_VOICE && isVoiceActive() 
					|| type == GlobalConstants.TYPE_GLOBAL && isMusicActive()) {
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
		
		//if controller is midi player, start a new music player
		if (controller instanceof MidiPlayer && !Client.database.isMuted(type, id)) {
			int priority = controller.getPlayerPriority();
			controller.close();
			
			controller = new MusicPlayer(type, id);
			controller.setPlayerPriority(priority);
			controller.start();
		}
		
		controller.addToQueue(packet);
	}

	public void updatePlayer(byte type, short id, double dist) {
		PlayerController controller = getPlayer(type, id);
		if (controller == null) {
			Station station = Client.database.getStation(type, id);
			if (station == null || (station.getSongs().isEmpty() && !station.isRadio())
					|| Client.database.isMuted(type, id)) {
				return;
			}
			controller = createNewPlayer(type, id);

			if (!(controller instanceof RadioPlayer)) {
				sendStartCommand(type, id);
			}
		}
		if (type == GlobalConstants.TYPE_BOX) {
			int range = Client.database.getStation(GlobalConstants.TYPE_BOX, id).getRange();
			byte volumePercent = (byte) (100 - (dist / range) * 100);
			volumeManager.setPlayerVolume(controller, volumePercent);

		} else if (type == GlobalConstants.TYPE_AREA) {
			int fadeout = Client.database.getStation(GlobalConstants.TYPE_AREA, id).getRange();
			if (fadeout > 0 && dist < fadeout) {
				byte volumePercent = (byte) ((100.0 / fadeout) * dist);
				volumeManager.setPlayerVolume(controller, volumePercent);
			} else {
				volumeManager.setPlayerVolume(controller, (byte) 100);
			}
		}
	}
	
	public void playMidi(MidiNotificationPacket notification, boolean waitForCurrentSong) {
		byte type = notification.getType();
		short id = notification.getId();
		PlayerController controller = getPlayer(type, id);
		if (controller != null) {
			controller.setNextMidiPlayer(notification);
		} else if (type == GlobalConstants.TYPE_GLOBAL && !Client.database.isMuted(type, id)) {
			controller = createNewPlayer(type, id);
			controller.setNextMidiPlayer(notification);
		}
	}

	public void setVoiceActive(boolean value) {
		AppletStarter.gui.controller.setVoiceActive(value);
		if (!value) {
			stopVoice();
		}
	}
	public boolean isVoiceActive() {
		return AppletStarter.gui.controller.isVoiceActive();
	}
	public void setMusicActive(boolean value) {
		AppletStarter.gui.controller.setMusicActive(value);
		if (!value) {
			stopMusic();
		}
	}
	public boolean isMusicActive() {
		return AppletStarter.gui.controller.isMusicActive();
	}

	public boolean isPlayingGlobally() {
		return (globalPlayer != null);
	}
	
	public boolean playersOverlap() {
		//ceck if there is more than one player (excluding speex players)
		int players = (areaPlayers.size() + boxPlayers.size() + biomePlayers.size() + worldPlayers.size());
		return players > 1;
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
			AppletStarter.gui.controller.setPlayButtonText("Play Globally");
		}
	}
	
	private PlayerController createNewPlayer(byte type, short id) {
		
		AppletStarter.logger.d("Creating new player. Type: " + type + " id: " + id, null);
		
		PlayerController controller = null;
		
		Station station = Client.database.getStation(type, id);
		ConcurrentHashMap<Short, PlayerController> map = getPlayerMap(type);
		if (map != null) {
			controller = new MusicPlayer(type, id);
			
			if (station == null && type == GlobalConstants.TYPE_VOICE) {
				controller = new VoicePlayer(type, id);
			
			} else if (station != null && station.isRadio()) {
				controller = new RadioPlayer(type, id, station.getRadioURL());
			}
			
			map.put(id, controller);
		}
		
		if (type == GlobalConstants.TYPE_GLOBAL) {
			AppletStarter.gui.controller.setPlayButtonText("Stop Globally");
			controller = new MusicPlayer(type, id);
			
			globalPlayer = controller;
		}
		
		if (controller != null) {
			//start the player
			controller.start();
		}
		
		return controller;
	}
	
	public void stopPlayer(final byte type, final short id) {
		PlayerController controller = getPlayer(type, id);
		if (controller != null) {
			controller.close();
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
					controller.close();
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
					controller.close();
				}
				for (Entry<Short, PlayerController> entry : boxPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close();
				}
				for (Entry<Short, PlayerController> entry : biomePlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close();
				}
				for (Entry<Short, PlayerController> entry : worldPlayers.entrySet()) {
					PlayerController controller = entry.getValue();
					controller.close();
				}
				
				if (globalPlayer != null) {
					globalPlayer.close();
				}
				
				areaPlayers.clear();
				boxPlayers.clear();
				biomePlayers.clear();
				worldPlayers.clear();
				globalPlayer = null;
				AppletStarter.gui.controller.setPlayButtonText("Play Globally");
			}
		}).start();
	}
	
	public void sendStartCommand(byte type, short id) {
		Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_START, type, id);
	}
	
	public void sendStopCommand(byte type, short id) {
		Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_STOP, type, id);
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
		case GlobalConstants.TYPE_VOICE:
			return voicePlayers;
		default:
			return null;
		}
	}

}
