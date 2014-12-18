package com.soundcenter.soundcenter.client.audio.player;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.tcp.MidiNotificationPacket;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class PlayerController extends Thread {

	protected byte type = 0;
	protected short playerId = 0;
	protected Station station = null;
	protected int playerPriority = 1;
	
	protected boolean firstPacketReceived = false;
	protected ExecutorService volumeExecutor = Executors.newFixedThreadPool(1);
	protected boolean fading = false;
	protected byte maxVolume = 100;
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
			this.maxVolume = station.getMaxVolume();
		}
		
		App.audioManager.volumeManager.addPriority(playerPriority);
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
	
	public int getVolume() {
		return oldVolume;
	}

	public void setVolume(int value, boolean allowFade) {
		// do not set the volume before the first packet was received
		// this is to prevent the volume from fading in before anything is played		
		if (volumeControl != null && !fading && firstPacketReceived) { 
			
			//we want to take care of our max volume
			value = (int) ((double)value*((double)maxVolume/100.d));
			
			boolean fade = false;
			if (allowFade && Math.abs(oldVolume - value) > 10) {
				fade = true;
			}
			
			if (fade) {
				fadeVolume(oldVolume, value);
			} else {
				float valueDB = (float) (minGainDB + (1/cste)*Math.log(1+(Math.exp(cste*ampGainDB)-1)*(value/100.0f)));
				volumeControl.setValue(valueDB);
			}
			
			oldVolume = value;
		}
	}
	
	private void fadeVolume(final int from, final int to) {
		
		if (volumeControl == null) {
			return;
		}
		fading = true;
		
		Runnable fadeRunnable = new Runnable() {
			@Override
			public void run() {
				//increase
				if (from < to) {
					for (int i = from; i <= (to - 1); i= i+1) {
						float stepValue = (float) (minGainDB + (1/cste)*Math.log(1+(Math.exp(cste*ampGainDB)-1)*(i/100.0f)));
						volumeControl.setValue(stepValue);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
					
				//decrease
				} else {					
					for (int i = from; i >= (to + 1); i = i-1) {
						float stepValue = (float) (minGainDB + (1/cste)*Math.log(1+(Math.exp(cste*ampGainDB)-1)*(i/100.0f)));

						volumeControl.setValue(stepValue);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
				}
				
				fading = false;
			}
		};
		volumeExecutor.execute(fadeRunnable);

		while (fading) {
			try { Thread.sleep(100); } catch(InterruptedException e) {}
		}
	}
	
	protected void firstPacketReceived() {
		if (!firstPacketReceived) {
			firstPacketReceived = true;
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
		App.audioManager.putPlayer(type, playerId, nextPlayer);
		nextPlayer.start();
	}
	
	public void setNextMidiPlayer(MidiNotificationPacket notification) {
		nextPlayer = new MidiPlayer(notification.getType(), notification.getId()
				, App.dataFolder + "musicdata" + File.separator + notification.getPath()
				, notification.getBytesToSkip());
		nextPlayer.setPlayerPriority(playerPriority);
		
		remainingPackets = queue.size();
		
		//interrupt to unblock thread
		if (queue.isEmpty()) {
			this.interrupt();
		}
	}
	
	public void close(final boolean preventRestart) {
		if (!preventRestart) {
			App.audioManager.removePlayer(type, playerId, this);
			App.audioManager.volumeManager.removePriority(playerPriority);
		}

		fadeVolume(oldVolume, 0);
		//wait for fadeout if close is called again from another thread
		while (fading) {
			try { Thread.sleep(100); } catch(InterruptedException e) {}
		}
		
		final PlayerController controller = this;
		
		//shutdown in a second (waits for proper fade out)
		new Timer().schedule(new TimerTask() {  
		    @Override
		    public void run() {
		    	exit = true;
				
				controller.interrupt();
				
				if (line != null) {
					line.stop();
					line.close();
				}
				if (sequencer != null) {
					try {
						sequencer.close();
					} catch (IllegalStateException e) {}
				}
				
				//tell the server to stop streaming to us
				if (type != GlobalConstants.TYPE_VOICE && type != GlobalConstants.TYPE_GLOBAL) {
					PlayerController newPlayer = App.audioManager.getPlayer(type, playerId);
					//do not stop streaming if a new player was created for this station
					if (newPlayer == null || newPlayer.exit) {
						App.audioManager.sendStopCommand(type, playerId);
					}
				}
				
				queue.clear();
				
				if (preventRestart) {
					App.audioManager.removePlayer(type, playerId, PlayerController.this);
					App.audioManager.volumeManager.removePriority(playerPriority);
				}
		    }
		}, 1000);
	}
}
