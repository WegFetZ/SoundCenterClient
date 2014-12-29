package com.soundcenter.soundcenter.client.gui.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.dialogs.AddSongDialog;
import com.soundcenter.soundcenter.client.gui.dialogs.PlaySongDialog;
import com.soundcenter.soundcenter.client.util.HttpUtil;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class MusicTabActions {

	public static void tabOpened() {
		if (Client.initialized) {
			if (Client.database.permissionGranted("sc.add.song")) {
				App.gui.musicTab.addButton.setEnabled(true);
			}
			App.gui.musicTab.playButton.setEnabled(true);
			App.gui.musicTab.stopButton.setEnabled(true);
		}
	}

	public static void listSelectionChanged() {
		Song song = (Song) App.gui.musicTab.songList.getSelectedValue();

		if (song != null) {
			if (song.getOwner().equalsIgnoreCase(Client.userName) || Client.database.permissionGranted("sc.others.delete")) {
				App.gui.musicTab.deleteButton.setEnabled(true);
			} else {
				App.gui.musicTab.deleteButton.setEnabled(false);
			}
		} else {
			App.gui.musicTab.deleteButton.setEnabled(false);
		}
	}

	public static void addButtonPressed() {
		AddSongDialog addDialog = new AddSongDialog(new JFrame());
		addDialog.setVisible(true);
	}

	public static void deleteButtonPressed(JList<Song> songList) {
		Song song = (Song) songList.getSelectedValue();
		if (song != null) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_DELETE_SONG, song, null);
		}
	}

	public static void playButtonPressed(JList<Song> songList) {
		Song song = songList.getSelectedValue();
		if (song != null) {
			PlaySongDialog playDialog = new PlaySongDialog(new JFrame(), song, true);
			playDialog.setVisible(true);
		}
	}

	public static void stopButtonPressed(JList<Song> songList) {
		Song song = songList.getSelectedValue();
		if (song != null) {
			PlaySongDialog playDialog = new PlaySongDialog(new JFrame(), song, false);
			playDialog.setVisible(true);
		}
	}

	/* --------------------- Play Dialog ------------------------- */
	public static void playSongDialogRadioButtonSelected(PlaySongDialog dialog) {
		if (dialog.worldButton.isSelected()) {
			dialog.worldTextField.setEnabled(true);
		} else {
			dialog.worldTextField.setEnabled(false);
		}
	}

	public static void playSongDialogPlayButtonPressed(PlaySongDialog dialog, boolean play) {
		if (dialog.selfButton.isSelected()) {
			if (play) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_PLAY_SONG, dialog.song, null);
			} else {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_STOP_SONG, dialog.song, null);
			}
			dialog.dispose();
			return;
		}

		if (dialog.worldButton.isSelected()) {
			String world = dialog.worldTextField.getText();
			if (world.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please enter the name of the world.", "Error", JOptionPane.OK_OPTION);
				return;
			}
			if (!Client.database.worldExists(world)) {
				JOptionPane.showMessageDialog(null, "A world with this name doesn't exist.", "Error", JOptionPane.OK_OPTION);
				return;
			}
			if (play) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_PLAY_SONG, dialog.song, world);
			} else {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_STOP_SONG, dialog.song, world);
			}
			dialog.dispose();
			return;
		}

		if (dialog.globalButton.isSelected()) {
			if (play) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_PLAY_SONG, dialog.song, "/global");
			} else {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CMD_STOP_SONG, dialog.song, "/global");
			}
			dialog.dispose();
		}
	}

	/* ---------------------- Add Dialog ------------------------- */

	public static void addSongDialogAddButtonPressed(AddSongDialog dialog) {
		String urlString = dialog.urlTextField.getText();
		String title = dialog.titleTextField.getText();
		if (urlString.isEmpty() || title.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Please enter title and URL.", "Error", JOptionPane.OK_OPTION);
			return;
		}

		if (Client.database.getSong(title) != null) {
			JOptionPane.showMessageDialog(null, "A song with this title does already exist.", "Error", JOptionPane.OK_OPTION);
			return;
		}

		dialog.glassPane.setVisible(true);
		
		try {
			URL url = new URL(urlString);
			AudioInputStream ais = AudioSystem.getAudioInputStream(url);
			AudioFileFormat aff = AudioSystem.getAudioFileFormat(ais);
			AudioFormat format = aff.getFormat();

			long duration = -1;
			long bytes = -1;

			String type = aff.getType().toString();
			// for music files we need to know the duration and size
			if (!dialog.radioCheckBox.isSelected()) {

				bytes = aff.getByteLength();
				if (bytes == AudioSystem.NOT_SPECIFIED) {
					bytes = HttpUtil.getFileSize(url);
				}
				if (bytes == -1) { // if we couldn't get the file size, the
									// format or webswerver is unsupported
					throw new UnsupportedAudioFileException("Cannot get file-size information. Audio-file, or webserver unsupported.");
				}

				float frameRate = format.getFrameRate();
				// for mp3 we need to get the frame size from its properties
				if (type.equalsIgnoreCase("MP3")) {
					int frameLength = -1;
					int padding = 0;
					int samplerate = -1;
					int bitrate = 0;
					int layer = 1;
					Map<String, Object> properties = aff.properties();
					if (properties.containsKey("mp3.version.layer")) {
						layer = Integer.valueOf((String) properties.get("mp3.version.layer")) ;
					}
					if (properties.containsKey("mp3.padding") && (boolean) properties.get("mp3.padding")) {
						padding = 1;
					}
					if (properties.containsKey("mp3.bitrate.nominal.bps")) {
						bitrate = (int) properties.get("mp3.bitrate.nominal.bps") ;
					}
					if (properties.containsKey("mp3.frequency.hz")) {
						samplerate = (int) properties.get("mp3.frequency.hz") ;
					}
					if (layer == 1) {
						frameLength = (int) (((double)(12*bitrate)/(double)(samplerate) + padding)*4);
					} else {
						frameLength =  144*bitrate/samplerate + padding;	
					}
					if (frameLength > 0 && bytes > 0) {
						duration = (long) ((float) (bytes / frameLength) / frameRate) * 1000;
					}

				} else { // we will use #frames/fps for all other formats. if
							// this doesn't work, the format is unsupported
					if (aff.getFrameLength() != AudioSystem.NOT_SPECIFIED && frameRate != AudioSystem.NOT_SPECIFIED) {
						duration = (long) (aff.getFrameLength() / frameRate) * 1000;
					}
				}
				if (duration <= 0) {
					throw new UnsupportedAudioFileException("Cannot get framerate frame-size information.");
				}
			}

			Song song = new Song(Client.userName, title, urlString, duration, bytes);
			song.setFormat(type);
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_ADD_SONG, song, null);

			dialog.glassPane.setVisible(false);
			dialog.dispose();

		} catch (MalformedURLException e) {
			App.gui.controller.setLoading(false);
			JOptionPane.showMessageDialog(null, "You have entered an invalid URL.", "Error", JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: invalid URL:", e);
		} catch (UnsupportedAudioFileException e) {
			dialog.glassPane.setVisible(false);
			String help = "Please note that the url for webradio streams must directly point to a .mp3 or .ogg formatted stream.";
			if (!dialog.radioCheckBox.isSelected()) {
				help = "Please note that only MP3 files are supported for songs.\nIf you want to add a radio stream, please select the checkbox.";
			}
			JOptionPane.showMessageDialog(null, "This audioformat is not supported.\n" + help, "Error", JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: audioformat unsupported:", e);
		} catch (IOException e) {
			dialog.glassPane.setVisible(false);
			JOptionPane.showMessageDialog(null, "Fie not found. You have either entered a wrong URL or lost your network connection.", "Error",
					JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: File not found. Wrong URL or lost network connection:", e);
		}
	}

}
