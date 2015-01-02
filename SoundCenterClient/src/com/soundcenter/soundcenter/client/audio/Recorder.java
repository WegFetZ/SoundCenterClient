package com.soundcenter.soundcenter.client.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.xiph.speex.SpeexEncoder;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.udp.UdpOpcodes;

public class Recorder implements Runnable {

	private boolean record = false;
	
	int bufferSize = 2560;
	int speexBufferSize = 640;
	SpeexEncoder mEncoder = new SpeexEncoder();
	TargetDataLine targetLine = null;

	public Recorder() {

		try {
			//Get microphone targetline
			AudioFormat format = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format, bufferSize);
			targetLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetLine.open(getAudioFormat(), bufferSize);
		} catch (LineUnavailableException e) {
			App.logger.w("No supported microphone found.", e);
		}

		//Initialize the speex encoder
		if (!mEncoder.init(1, 8, 16000, 1)) {
			App.logger.w("Failed to initialize speex encoder!", null);
		}
	}

	public void run() {
		Thread.currentThread().setName("Recorder");

		if (targetLine == null)
			return;

		while (!Client.exit) {

			if (record) {
				//capture audiodata
				byte[] buffer = new byte[speexBufferSize];
				targetLine.read(buffer, 0, speexBufferSize);
				mEncoder.processData(buffer, 0, speexBufferSize);
				int encodedBytes = mEncoder.getProcessedData(buffer, 0);
				
				if (encodedBytes > 0) {
					//send audiodata over udp
					Client.udpClient.sendData(buffer, UdpOpcodes.TYPE_VOICE);
				}
			} else {
				try { Thread.sleep(100); } catch (InterruptedException e) {}
			}
		}
		stop();
	}

	public void start() {
		if (targetLine != null) {
			record = true;
			targetLine.start();
			App.logger.i("Recording voice...", null);
		}
	}

	public void stop() {
		if (record)
			App.logger.i("Recording ended.", null);
		record = false;
		if (targetLine != null) {
			targetLine.stop();
			targetLine.close();
		}
	}
	
	public boolean isRecording() {
		return record;
	}

	public static AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		//8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		//8,16
		int channels = 1;
		//1,2
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

}
