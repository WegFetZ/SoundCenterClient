package com.soundcenter.soundcenter.client.audio.player;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.lib.data.Song;

public class SingleSongPlayer extends PlayerController {

	private AudioFormat decodedFormat = null;
	private String prefix;
	
	public SingleSongPlayer(Song song, int priority) {
		this.singleSong = song;
		this.playerPriority = priority;
		
		App.audioManager.volumeManager.addPriority(playerPriority);
	}
	
	public SingleSongPlayer(byte type, short id) {
		App.logger.w("Cannot instantiate a SingleSongWebPlayer(byte type, short id). Use SingleSongWebPlayer(Song song, int priority) instead.", null);
		exit = true;
	}

	@Override
	public void run() {
		if (exit) {
			return;
		}
		Thread.currentThread().setName("SingleSongWebPlayer");
		
		prefix = "[SongPlayer " + singleSong.getTitle() + " - " + singleSong.getOwner() + "] "; 

		//App.logger.d("Starting player: " + prefix, null);
		
		if (singleSong == null) {
			App.logger.d("No song provided - player closing. ", null);
			close(false);
			return;
		}
			
		streamSong(singleSong);

		App.logger.d(prefix + "Player closing.", null);
	}
	
	private void streamSong (Song song) {
		try {
			URL url = new URL(song.getUrl());
			if (song.getFormat().equalsIgnoreCase("MP3")) {
				URLConnection uc = url.openConnection();
				uc.setUseCaches(false);
				uc.connect();
				encodedAudioStream = AudioSystem.getAudioInputStream(uc.getInputStream());
			} else {
				encodedAudioStream = AudioSystem.getAudioInputStream(url);
			}
			
			if (encodedAudioStream != null) {
				init(encodedAudioStream.getFormat());
				decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, encodedAudioStream);
				
				int numBytesRead = 0;
				while (!exit && line.isOpen() && numBytesRead != -1) {

					byte[] data = new byte[bufferSize];
					numBytesRead = decodedAudioStream.read(data, 0, data.length);
					if (!fadedIn) {// now we can fade in the volume, if not already done
						fadeIn();
					}
					if (numBytesRead > 0 && data != null)
						line.write(data, 0, data.length);
				}
			}
		} catch (LineUnavailableException e) {
			if (!exit)
				App.logger.i(prefix + "Error while starting playback.", e);
		} catch (IOException e) {
			if (!exit)
				App.logger.i(prefix + "Error while playing audio:", e);
		} catch (UnsupportedAudioFileException e) {
			App.logger.i(prefix + "Error while retrieving audio file format-information. Please remove song: "  + song.getTitle() + " - " + song.getOwner(), e);
		}

		if (!exit) {
			close(false);
		}

		// AppletStarter.logger.d("Song stopped/ finished at player " + id +
		// ".",
		// null);
	}

	private void init(AudioFormat baseFormat) throws LineUnavailableException {

		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
				baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(decodedFormat);

		volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		minGainDB = volumeControl.getMinimum();
		ampGainDB = ((10.0f / 20.0f) * volumeControl.getMaximum()) - volumeControl.getMinimum();
		cste = Math.log(10.0) / 20;

		oldVolume = 100;
		
		line.start();

		//App.logger.d("prefix + WebPlayer SourceDataLine started!", null);
	}
}
