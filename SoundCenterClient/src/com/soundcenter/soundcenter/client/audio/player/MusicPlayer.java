package com.soundcenter.soundcenter.client.audio.player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class MusicPlayer extends PlayerController {
	
	private Player player = new Player();
	
	public MusicPlayer(byte type, short id) {
		super(type, id);
	}
	
	@Override
	public void run() {
		this.setName("MusicSession - ID: " + playerId);
		
		new Thread(player).start();

		int remainingLoops = -1;
		while (!exit) {
			try {
				
				//quit when next player has been set. play remaining packets first.
				if (remainingPackets >= 0) {
					if (remainingLoops == -1) {
						remainingLoops = remainingPackets;
					}
					
					if (remainingLoops <= 0) {
						break;
					}
					
					remainingLoops--;
				}
				
				UdpPacket packet = queue.poll(5L, TimeUnit.SECONDS);
				
				//quit when no packet arrives for 5 seconds
				if (packet == null) {
					//AppletStarter.logger.d("Player timed out. Closing...", null);
					close();
					break;
				}					
					
				if (songIndex != packet.getSongIndex()+127 && songIndex != -200) {
					//start next player if index changes or break if next song is midi
					if (nextPlayer == null) {
						resetPlayer();
					} else {
						break;
					}
				}
				
				songIndex = packet.getSongIndex()+127;
				
				player.write(packet.getStreamData());
				
			} catch (InterruptedException e) {}
		}
		
		if (!exit) {
			player.stopPlayer();
			startMidiPlayer();
		} else {
			close();
		}
		
		AppletStarter.logger.d("MusicSession ID: " + playerId + " closed.", null);
	}	
	
	//TODO fix exception on player reset
	private void resetPlayer() {
		player.stopPlayer();
		player = new Player();
		new Thread(player).start();
	}
	
	private class Player implements Runnable {
		
		private int pipeBufferSize = 8192;
		private BufferedInputStream streamIn = null;
		private BufferedOutputStream streamOut = null;
		
		private AudioFormat decodedFormat = null;
		private boolean stop = false;
		
		private Player() {
			try {
				PipedOutputStream pipeOut = new PipedOutputStream();
				PipedInputStream pipeIn = new PipedInputStream(pipeOut, pipeBufferSize);
				streamOut = new BufferedOutputStream(pipeOut);
				streamIn = new BufferedInputStream(pipeIn);
			} catch (IOException e) {
				AppletStarter.logger.i("Error while creating pipes for music player", e);
				stopPlayer();
			}
		}
		
		@Override
		public void run() {
			Thread.currentThread().setName("MusicPlayer");
			
			AudioInputStream encodedAudioStream = null;
			AudioInputStream decodedAudioStream = null;
			try {
				encodedAudioStream = AudioSystem.getAudioInputStream(streamIn);
				//AppletStarter.logger.d("Raw-Input-Stream created!", null);
				
				if (encodedAudioStream != null) {
					init(encodedAudioStream.getFormat());
					
					decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, encodedAudioStream);
					//AppletStarter.logger.d("Decoder-Stream created!", null);
					
					int numBytesRead = 0;
					while (!exit && !stop && line.isOpen()) {
		
						byte[] data = new byte[bufferSize];
						numBytesRead = decodedAudioStream.read(data, 0, data.length);
						if (numBytesRead > 0 && data != null) {
							firstPacketReceived();
							line.write(data, 0, data.length);
						}
					}
				}
			} catch (LineUnavailableException e) {
				if (!stop && ! exit)
					AppletStarter.logger.i("Error while playing music stream:", e);
			} catch (IOException e) {
				if (!stop && ! exit)
					AppletStarter.logger.i("Error while writing to sourceDataLine:", e);
			} catch (UnsupportedAudioFileException e) {
				AppletStarter.logger.i("Error while retrieving audio file format information:", e);
			}
			
			if (!stop) {
				stopPlayer();
			}
			
			try {
				if (encodedAudioStream != null)
					encodedAudioStream.close();
				if (decodedAudioStream != null)
					decodedAudioStream.close();
			} catch (IOException e) {}
			
			//AppletStarter.logger.d("Song stopped/ finished at player " + playerId + ".", null);
		}
		
	
		private void init(AudioFormat baseFormat) throws LineUnavailableException {
			
			decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
					baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(decodedFormat);
			
			volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			minGainDB = volumeControl.getMinimum();
			ampGainDB = ((10.0f/20.0f)*volumeControl.getMaximum()) - volumeControl.getMinimum();
			cste = Math.log(10.0)/20;
			
			if (type == GlobalConstants.TYPE_AREA || type == GlobalConstants.TYPE_BOX) {
				volumeControl.setValue((float) minGainDB);
			}
			
			line.start();
			
			//AppletStarter.logger.d("MusicPlayer SourceDataLine started!", null);
		}	
		
		private void write(byte[] data) {
			try {
				streamOut.write(data);
			} catch(IOException e) {
				if (!stop && !exit)
					AppletStarter.logger.d("Error while writing song data to musicplayer.", e);
				player.stopPlayer();
			}
		}
		
		private void stopPlayer() {
			stop = true;
			if (line != null) {
				line.stop();
				line.close();
			}
			
			try {
				if (streamIn != null)
					streamIn.close();
				if (streamOut != null)
					streamOut.close();
			} catch (IOException e) {}
		}
		
	}	
}
