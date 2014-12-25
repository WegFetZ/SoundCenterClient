package com.soundcenter.soundcenter.client.gui.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.dialogs.AddSongDialog;
import com.soundcenter.soundcenter.client.util.HttpUtil;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class MusicTabActions {

	public static void musicChooserSelected() {
		JComboBox playerComboBox = App.gui.musicTab.playerComboBox;

		DefaultListModel model = null;
		if (playerComboBox.getSelectedIndex() >= 0) {
			String player = (String) playerComboBox.getSelectedItem();
			model = Client.database.getSongModel(player);

			if (model != null) {
				App.gui.musicTab.songList.setModel(model);
			} else {
				App.gui.musicTab.songList.setModel(new DefaultListModel());
			}

			if (player.equals(Client.userName)) {
				App.gui.musicTab.addButton.setEnabled(true);
				App.gui.musicTab.deleteButton.setEnabled(true);
			} else {
				App.gui.musicTab.addButton.setEnabled(false);
				if (Client.database.permissionGranted("sc.others.delete")) {
					App.gui.musicTab.deleteButton.setEnabled(true);
				} else {
					App.gui.musicTab.deleteButton.setEnabled(false);
				}
			}

			if (Client.database.permissionGranted("sc.play.global")) {
				App.gui.musicTab.playButton.setEnabled(true);
			} else {
				App.gui.musicTab.playButton.setEnabled(false);
			}

		} else {
			App.gui.musicTab.songList.setModel(new DefaultListModel());

			App.gui.musicTab.addButton.setEnabled(false);
			App.gui.musicTab.deleteButton.setEnabled(false);
			App.gui.musicTab.playButton.setEnabled(false);

		}
	}

	public static void addButtonPressed() {
		AddSongDialog addDialog = new AddSongDialog(new JFrame());
		addDialog.setVisible(true);
	}

	public static void deleteButtonPressed(JList songList) {
		Song song = (Song) songList.getSelectedValue();
		if (song != null) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_DELETE_SONG, song, null);
		}
	}

	public static void playButtonPressed(JList songList) {
		if (App.gui.musicTab.playButton.getText().equals("Stop Globally")) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_STOP_GLOBAL, null, null);
		} else {
			Song song = (Song) songList.getSelectedValue();
			if (song != null) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_PLAY_GLOBAL, song, null);
			}
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
		try {
			URL url = new URL(urlString);
			AudioFileFormat aff = AudioSystem.getAudioFileFormat(url);
			AudioFormat format = aff.getFormat();
			
			long duration = -1;
			long bytes = -1;
			
			String type = aff.getType().toString();
			//for music files we need to know the duration and size
			if (!dialog.radioCheckBox.isSelected()) {
				
				bytes = aff.getByteLength();
				if (bytes == AudioSystem.NOT_SPECIFIED) {
					bytes = HttpUtil.getFileSize(url);
				}
				if (bytes == -1) { //if we couldn't get the file size, the format or webswerver is unsupported
					throw new UnsupportedAudioFileException("Cannot get file-size information. Audio-file, or webserver unsupported.");
				}
				
				float frameRate = format.getFrameRate();
				//for mp3 we need to get the frame size from its properties
				if (type.equalsIgnoreCase("MP3")) {
					int frameSize = -1;
					Map<String, Object> properties = aff.properties();
					if (properties.get("mp3.framesize.bytes") != null) {
						frameSize = (int) properties.get("mp3.framesize.bytes");
					}
					if (frameSize > 0 && bytes > 0) {
						duration = (long) ((float)(bytes/frameSize)/frameRate)*1000;
					}
					
				} else { //we will use #frames/fps for all other formats. if this doesn't work, the format is unsupported
					if (aff.getFrameLength() != AudioSystem.NOT_SPECIFIED && frameRate != AudioSystem.NOT_SPECIFIED) {
						duration = (long) (aff.getFrameLength() / frameRate)*1000;
					}
				}
				if (duration <= 0) {
					throw new UnsupportedAudioFileException("Cannot get framerate frame-size information.");
				}
			}
			
			Song song = new Song(Client.userName, title, urlString, duration, bytes);
			song.setFormat(type);
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_ADD_SONG, song, null);
			
			dialog.dispose();
			
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null, "You have entered an invalid URL.", "Error", JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: invalid URL:", e);
		} catch (UnsupportedAudioFileException e) {
			JOptionPane.showMessageDialog(null, "This audioformat is not supported.", "Error", JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: audioformat unsupported:", e);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fie not found. You have either entered a wrong URL or lost your network connection.", "Error", JOptionPane.OK_OPTION);
			App.logger.d("Cannot add song: File not found. Wrong URL or lost network connection:", e);
		}
	}
}
