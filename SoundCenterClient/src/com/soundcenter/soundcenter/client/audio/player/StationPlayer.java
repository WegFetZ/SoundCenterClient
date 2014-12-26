package com.soundcenter.soundcenter.client.audio.player;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;

public class StationPlayer extends PlayerController {

	private AudioFormat decodedFormat = null;
	List<Song> songList = null;
	String prefix;

	public StationPlayer(byte type, short id) {
		
		this.type = type;
		this.playerId = id;
		this.station = Client.database.getStation(type, playerId);
		if (station != null) {
			this.playerPriority = station.getPriority();
			this.maxVolume = station.getMaxVolume();
		}
		
		if (type == GlobalConstants.TYPE_BOX || type == GlobalConstants.TYPE_AREA) {
			allowPlayback = false;
		}
		
		App.audioManager.volumeManager.addPriority(playerPriority);
	}
	
	public StationPlayer(Song song, int priority) {
		App.logger.w("Cannot instantiate a WebPlayer(Song song, int priority). Use WebPlayer(byte type, short id) instead.", null);
		exit = true;
	}

	@Override
	public void run() {
		if (exit) {
			return;
		}
		Thread.currentThread().setName("WebPlayer");
		// get the list of songs to be played
		
		if (station != null) {
			songList = station.getSongs();
			prefix = "[StationPlayer " + type + "(" + playerId + ")] ";
		}

		//App.logger.d("Starting player: " + prefix, null);
		
		if ((songList == null || songList.isEmpty())) {
			App.logger.d(prefix + "No songs provided - player closing. ", null);
			close(false);
			return;
		}

		int index = 0;
		long byteOffset = 0;
		ListIterator<Song> iter = songList.listIterator();
		
		if (!station.shouldStartFromBeginning()) {
			// get the offset for the playback
			long[] offset = getOffset(songList);
			index = (int) offset[0];
			byteOffset = offset[1];
		
			if (index == -1) {
				App.logger.d(prefix + "Got negative index - player closing.", null);
				close(false);
				return;
			}
			
			// set iterator to the calculated index
			while (iter.hasNext() && iter.nextIndex() < index) {
				iter.next();
			}
		}
		
		while (!exit) {
			if (songList.isEmpty()) {
				close(false);
				return;
			}
			if (!iter.hasNext()) {
				if (station.shouldLoop()) {
					iter = songList.listIterator();
				} else {
					//if we shall not loop, sleep until player gets closed
					while(!exit) {
						try { Thread.sleep(100); } catch(InterruptedException e){}
					}
					close(false);
					App.logger.d(prefix + "Player closing.", null);
					return;
				}
			}
			index = iter.nextIndex();
			Song currentSong = iter.next();
			
			streamSong(currentSong, byteOffset, (byte) index);

			byteOffset = 0;
		}
		App.logger.d(prefix + "Player closing.", null);
	}
	
	private void streamSong (Song song, long byteOffset, byte index) {
		try {
			URL url = new URL(song.getUrl());
			if (song.getFormat().equalsIgnoreCase("MP3")) {
				URLConnection uc = url.openConnection();
				uc.setUseCaches(false);
				if (byteOffset > 0) {
					uc.setRequestProperty("Range", "bytes="+byteOffset+"-");
				}
				uc.connect();
				encodedAudioStream = AudioSystem.getAudioInputStream(uc.getInputStream());
			} else {
				encodedAudioStream = AudioSystem.getAudioInputStream(url);
			}
			
			if (encodedAudioStream != null) {
				init(encodedAudioStream.getFormat());
				decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, encodedAudioStream);
				if (!song.getFormat().equalsIgnoreCase("MP3")) {
					decodedAudioStream.skip(byteOffset);
				}
				
				while(!exit && station.shouldStartFromBeginning() && !allowPlayback) {
					try { Thread.sleep(50); } catch(InterruptedException e) {}
				}
				
				int numBytesRead = 0;
				while (!exit && line.isOpen() && numBytesRead != -1) {

					byte[] data = new byte[bufferSize];
					numBytesRead = decodedAudioStream.read(data, 0, data.length);
					if (!fadedIn) {// now we can fade in the volume, if not already done
						fadeIn();
						fadedIn = true;
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

	private long[] getOffset(List<Song> songList) {
		long[] songDurations = new long[songList.size()];
		long[] songSizes = new long[songList.size()];
		long totalDuration = 0;
		// get duration of all files
		Iterator<Song> iter = songList.iterator();
		int loops = 0;
		while (iter.hasNext()) {
			Song song = iter.next();
			songDurations[loops] = song.getDuration();
			songSizes[loops] = song.getSize();
			totalDuration += songDurations[loops];
			loops++;
		}
		if (totalDuration == 0) {
			return new long[] { -1, 0, 0 };
		}
		long currentTime = System.currentTimeMillis();
		long timeOffset = currentTime % totalDuration; // amount of time that
		// has to be skipped
		// over all files
		long partOffset = 0; // sum of all file durations until the file that is
		// currently played
		long restOffset = 0; // time offset of the file to be played
		int fileIndex = 0; // index of file to be played
		for (int i = 0; i < songDurations.length; i++) {
			fileIndex = i;
			partOffset += songDurations[i];
			if (partOffset >= timeOffset) {
				restOffset = songDurations[i] - (partOffset - timeOffset);
				break;
			}
		}
		
		long byteOffset = (long) (((double) songSizes[fileIndex] / (double) songDurations[fileIndex]) * restOffset);
		return new long[] { fileIndex, byteOffset};
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

		//for stations we want to set the initial volume to 0
		volumeControl.setValue((float) minGainDB);
		this.oldVolume = 0;
		
		line.start();

		//App.logger.d("prefix + WebPlayer SourceDataLine started!", null);
	}
}
