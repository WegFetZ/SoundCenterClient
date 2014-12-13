package com.soundcenter.soundcenter.client.audio.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;

public class MidiPlayer extends PlayerController {
	
	private Receiver receiver = null;
	
	private String filePath = "";
	private long position = 0; // starting position in milliseconds
	
	
	public MidiPlayer(byte type, short id, String filePath, long position) {
		super(type, id);
		this.filePath = filePath;
		this.position = position;
	}

	@Override
	public void run() {
		
		try {
			
			init();

			App.logger.d("Starting midi: " + filePath + " (pos: " + position + ") on " 
			+ type + " (" + playerId + ").", null);

				FileInputStream stream = new FileInputStream(filePath);
				Sequence sequence = MidiSystem.getSequence(stream);
	
				sequencer.setSequence(sequence);
	
				if (position > 0)
					setPosition(position);
				sequencer.start();

		} catch (InvalidMidiDataException e) {
			if (!exit)
				App.logger.i("Error while playing Midi:", e);
			close();
		} catch (FileNotFoundException e) {
			if (!exit)
				App.logger.i("Error while playing Midi:", e);
			close();
		} catch (IOException e) {
			if (!exit)
				App.logger.i("Error while playing Midi:", e);
			close();
		}
		
		//player gets removed in the close() method
	}

	public void init() {
		try {
			sequencer = MidiSystem.getSequencer(false);
			receiver = MidiSystem.getReceiver();
			sequencer.open();
			sequencer.getTransmitter().setReceiver(receiver);
			
			if (type != GlobalConstants.TYPE_GLOBAL) {
				setVolume((byte) 0, false);
				this.oldVolume = 0;
			}
			
			// add the MetaEventListener to listen for end of track
			 sequencer.addMetaEventListener(new MetaEventListener() {

		            @Override
		            public void meta(MetaMessage metaMsg) {
		                if (metaMsg.getType() == 0x2F) {
		                	close();
		                }
		            }
		        });
			
		} catch (MidiUnavailableException e) {
			App.logger.i("Error while initializing MidiPlayer:", e);
		}
	}
	
	public void setPosition(long position) {
		sequencer.setMicrosecondPosition(position * 1000);
	}

	@Override
	public void setVolume(byte value, boolean allowFade) {
		if (sequencer != null && sequencer.isOpen()) {
			
			//calculate value: f(x) = -0,0126x^2 + 2.53x 	(half range is 75% of volume)
			final byte vol = (byte) (-0.0126*Math.pow(value, 2) + 2.53*value);
			
			boolean fade = false;
			if (allowFade && Math.abs(oldVolume - value) > 20) {
				fade = true;
			}
			
			if (fade) {
				fadeVolume(value);
			}
			
			sendVolumeMessage(new ShortMessage(), vol);
			
			oldVolume = vol;
		}
	}
	
	private void fadeVolume(final int newVolume) {
		
		Runnable fadeRunnable = new Runnable() {
			@Override
			public void run() {
				fading = true;
				
				ShortMessage volMessage = new ShortMessage();
				//increase
				if (oldVolume < newVolume) {
					for (int i = oldVolume; i < (oldVolume-1); i=i+1) {
						int stepVol = (int) (-0.0126*Math.pow(i, 2) + 2.53*newVolume);
						sendVolumeMessage(volMessage, stepVol);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
					
				//increase
				} else {
					for (int i = oldVolume; i > (oldVolume+1); i=i-1) {
						int stepVol = (int) (-0.0126*Math.pow(i, 2) + 2.53*newVolume);
						sendVolumeMessage(volMessage, stepVol);
						try { Thread.sleep(10); } catch(InterruptedException e) {}
					}
				}
				
				fading = false;
			}
		};
		
		fading = true; // we need to set to true here already, else while loop won't run
		volumeExecutor.execute(fadeRunnable);

		while (fading) {
			try { Thread.sleep(100); } catch(InterruptedException e) {}
		}
	}

	private void sendVolumeMessage(ShortMessage message, int vol) {
		for (int i = 0; i < 16; i++) {
			try {
				message.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, vol);
				receiver.send(message, -1);
			} catch (InvalidMidiDataException e) {
				App.logger.d("Error while setting midi volume.", e);
			}
		}
	}
	
	@Override
	public void close() {
		super.close();
		
		App.logger.d("MidiPlayer " + type + " (" + playerId + ") closed.", null);
	}

}
