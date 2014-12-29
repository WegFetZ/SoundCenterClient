package com.soundcenter.soundcenter.client.audio.player;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;

public class PlayerController extends Thread {

	protected byte type = 0;
	protected short playerId = 0;
	protected Station station = null;
	protected Song singleSong = null;
	protected int playerPriority = 1;

	protected ExecutorService volumeExecutor = Executors.newFixedThreadPool(1);
	protected boolean fading = false;
	protected byte maxVolume = 100;
	protected int oldVolume = 0;

	AudioInputStream encodedAudioStream = null;
	AudioInputStream decodedAudioStream = null;
	protected SourceDataLine line = null;

	protected FloatControl volumeControl = null;
	protected double minGainDB = 0;
	protected double ampGainDB = 0;
	protected double cste = 0;
	protected boolean fadedIn = false; //indicates wether the volume was already faded in
	protected boolean allowPlayback = true; // used only for boxes/areas,
											// because they are started before
											// actually coming in range, to
											// reduce the delay

	protected int bufferSize = 640;

	protected int songIndex = -200;

	protected boolean exit = false;

	public PlayerController() {
	}
	
	public PlayerController(Song song, int priority) {
	}
	
	public PlayerController(byte type, short id) {
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

	public void setSingleSong(Song song) {
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
		}

		return false;
	}

	public void allowPlayback() {
		this.allowPlayback = true;
	}
	
	public int getVolume() {
		return oldVolume;
	}

	public void setVolume(int value, boolean allowFade) {
		if (volumeControl != null && !fading && fadedIn) {

			// we want to take care of our max volume
			value = (int) ((double) value * ((double) maxVolume / 100.d));

			boolean fade = false;
			if (allowFade && Math.abs(oldVolume - value) > 10) {
				fade = true;
			}

			if (fade) {
				fadeVolume(oldVolume, value);
			} else {
				float valueDB = (float) (minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * (value / 100.0f)));
				volumeControl.setValue(valueDB);
			}
		}
		oldVolume = value;
	}

	private void fadeVolume(final int from, final int to) {

		if (volumeControl == null) {
			return;
		}
		fading = true;

		Runnable fadeRunnable = new Runnable() {
			@Override
			public void run() {
				// increase
				if (from < to) {
					for (int i = from; i <= (to - 1); i = i + 1) {
						float stepValue = (float) (minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * (i / 100.0f)));
						volumeControl.setValue(stepValue);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}

					// decrease
				} else if (from > to) {
					for (int i = from; i >= (to + 1); i = i - 1) {
						float stepValue = (float) (minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * (i / 100.0f)));

						volumeControl.setValue(stepValue);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
				}

				fading = false;
				fadedIn = true;
			}
		};
		volumeExecutor.execute(fadeRunnable);

		while (fading) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public void close(final boolean preventRestart) {
		if (!preventRestart) {
			if (this instanceof SingleSongPlayer) {
				App.audioManager.removeSingleSongPlayer(this.singleSong, this);
			} else {
				App.audioManager.removeStationPlayer(type, playerId, this);
			}
			App.audioManager.volumeManager.removePriority(playerPriority);
		}

		fadeVolume(oldVolume, 0);
		
		final PlayerController controller = this;

		// shutdown in a second (waits for proper fade out)
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				exit = true;

				controller.interrupt();

				if (line != null) {
					line.stop();
					line.close();
				}
				try {
					if (encodedAudioStream != null)
						encodedAudioStream.close();
					if (decodedAudioStream != null)
						decodedAudioStream.close();
				} catch (IOException e) {
				}
				
				if (preventRestart) {
					if (PlayerController.this instanceof SingleSongPlayer) {
						App.audioManager.removeSingleSongPlayer(PlayerController.this.singleSong, PlayerController.this);
					} else {
						App.audioManager.removeStationPlayer(type, playerId, PlayerController.this);
					}
					App.audioManager.volumeManager.removePriority(playerPriority);
				}
			}
		}, 1000);
	}
	
	protected void fadeIn() {
		fadeVolume(0,oldVolume);
	}
}
