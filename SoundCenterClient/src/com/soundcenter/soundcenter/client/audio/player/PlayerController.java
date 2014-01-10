package com.soundcenter.soundcenter.client.audio.player;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.Sequencer;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.tcp.MidiNotificationPacket;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class PlayerController extends Thread {

	protected byte type = 0;
	protected short playerId = 0;
	protected Station station = null;
	protected int playerPriority = 1;
	
	protected boolean fading = false;
	protected int oldVolume = 0;
	
	protected Sequencer sequencer = null;
	protected SourceDataLine line = null;
	
	protected FloatControl volumeControl = null;
	protected double minGainDB = 0;
	protected double ampGainDB = 0;
	protected double cste = 0;
	
	protected int bufferSize = 640;
	protected BlockingQueue<UdpPacket> queue = new LinkedBlockingQueue<UdpPacket>();
	
	protected PlayerController nextPlayer = null;
	protected int remainingPackets = -1;
	protected int songIndex = -200;
	
	protected boolean exit = false;	
	
	
	public PlayerController(byte type, short id) {
		this.type = type;
		this.playerId = id;
		this.station = Client.database.getStation(type, playerId); 
		if (station != null) {
			this.playerPriority = station.getPriority();
		}
		
		Applet.audioManager.volumeManager.addPriority(playerPriority);
	}
	
	
	
	public short getPlayerId() {
		return playerId;
	}
	public void setPlayerId(short id) {
		this.playerId = id;
	}
	
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	
	public int getPlayerPriority() {
		return playerPriority;
	}
	public void setPlayerPriority(int value) {
		playerPriority = value;
	}
	
	public boolean isFading() {
		return fading;
	}
	
	public boolean isActive() {
		if (line != null) {
			return line.isActive();
		} else if (sequencer != null) {
			return sequencer.isRunning();
		}
		
		return false;
	}
	
	public void setQueue(BlockingQueue<UdpPacket> queue) {
		this.queue = queue;
	}
	
	public void addToQueue(UdpPacket packet){
		queue.add(packet);
	}

	public void setVolume(int value, boolean fade) {
		if (volumeControl != null) {
			final float valueDB = (float) (minGainDB + (1/cste)*Math.log(1+(Math.exp(cste*ampGainDB)-1)*(value/100.0f)));
			
			if (Math.abs(oldVolume - value) > 20) {
				fade = true;
			}
			
			if (fade) {
				fading = true;
				//fade volume change
				if (oldVolume < value) {
					for (int i = oldVolume; i <= (value - 1); i= i+1) {
						setVolume(i, false);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
				} else {
					for (int i = oldVolume; i >= (value + 1); i = i-1) {
						setVolume(i, false);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
				}
				fading = false;
			}
		
			volumeControl.setValue(valueDB);
			
			oldVolume = value;
		}
	}
	
	public int getSongIndex() {
		return songIndex;
	}
	public void setSongIndex(int index) {
		this.songIndex = index;
	}
	
	protected void startMidiPlayer() {
		if (nextPlayer == null) {
			return;
		}
		
		//add the next player to the player list
		Applet.audioManager.putPlayer(type, playerId, nextPlayer);
		nextPlayer.start();
	}
	
	public void setNextMidiPlayer(MidiNotificationPacket notification) {
		nextPlayer = new MidiPlayer(notification.getType(), notification.getId()
				, Applet.dataFolder + "musicdata" + File.separator + notification.getPath()
				, notification.getBytesToSkip());
		nextPlayer.setPlayerPriority(playerPriority);
		
		remainingPackets = queue.size();
		
		//interrupt to unblock thread
		if (queue.isEmpty()) {
			this.interrupt();
		}
	}
	
	public void close() {
		Applet.audioManager.removePlayer(type, playerId, this);

		/* TODO: fade out properly
		setVolume(0, true);
		//wait for volume fadeout
		try { Thread.sleep(1500); } catch(InterruptedException e) {}
		*/ 
		
		exit = true;
		
		this.interrupt();
		
		if (line != null) {
			line.stop();
			line.close();
		}
		if (sequencer != null) {
			try {
				sequencer.close();
			} catch (IllegalStateException e) {}
		}
		
		Applet.audioManager.volumeManager.removePriority(playerPriority);
		
		queue.clear();
	}
}
