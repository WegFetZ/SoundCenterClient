package com.soundcenter.soundcenter.client.audio.player;

import java.io.StreamCorruptedException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.xiph.speex.SpeexDecoder;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class VoicePlayer extends PlayerController {

	protected BlockingQueue<UdpPacket> queue = new LinkedBlockingQueue<UdpPacket>();
	private SpeexDecoder speexDecoder = new SpeexDecoder();

	public VoicePlayer(byte type, short id) {
		this.type = type;
		this.playerId = id;
		this.fadedIn = true; //we do not want to fade in
		
		App.audioManager.volumeManager.addPriority(playerPriority);
	}
	
	public VoicePlayer(Song song, int priority) {
		App.logger.w("Cannot instantiate a WebPlayer(Song song, int priority). Use WebPlayer(byte type, short id) instead.", null);
		exit = true;
	}

	public void setQueue(BlockingQueue<UdpPacket> queue) {
		this.queue = queue;
	}

	public void addToQueue(UdpPacket packet) {
		queue.add(packet);
	}

	@Override
	public void run() {
		this.setName("SpeexPlayer - ID: " + playerId);
		init();
		
		byte[] decodedData;
		while (line.isOpen() && !exit) {
			try {

				UdpPacket packet = queue.poll(3L, TimeUnit.SECONDS);

				// quit when no packet arrives for 3 seconds
				if (packet == null) {
					break;
				}

				speexDecoder.processData(packet.getStreamData(), 0, packet.getStreamData().length);

				decodedData = new byte[speexDecoder.getProcessedDataByteSize()];
				speexDecoder.getProcessedData(decodedData, 0);
				line.write(decodedData, 0, decodedData.length);
			} catch (StreamCorruptedException e) {
				App.logger.w("Stream corrupted in voice player (type: " + type + " id: " + playerId + "):" , e);
			} catch (InterruptedException e) {
			}
		}
		if (!exit) {
			close(false);
		}
	}

	private void init() {
		AudioFormat speexFormat = new AudioFormat(16000, 16, 1, true, false);
		DataLine.Info sourceLineInfo = new DataLine.Info(SourceDataLine.class, speexFormat, bufferSize);
		speexDecoder.init(1, 16000, 1, false);
		try {
			line = (SourceDataLine) AudioSystem.getLine(sourceLineInfo);
			line.open(speexFormat, bufferSize);

			volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			minGainDB = volumeControl.getMinimum();
			ampGainDB = ((10.0f / 20.0f) * volumeControl.getMaximum()) - volumeControl.getMinimum();
			cste = Math.log(10.0) / 20;

			volumeControl.setValue((float) minGainDB);

			line.start();

			// Applet.logger.d("SpeexPlayer SourceDataLine started!", null);
		} catch (LineUnavailableException e) {
			App.logger.w("Failed to create voice player (type: " + type + " id: " + playerId + "):" , e);
			if (!exit)
				close(false);
		}
	}

	@Override
	public void close(boolean preventRestart) {
		super.close(preventRestart);
		queue.clear();
	}

}
