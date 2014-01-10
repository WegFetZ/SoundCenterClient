package com.soundcenter.soundcenter.client.network.tcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JFrame;

import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.dialogs.UploadSongDialog;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class UploadManager implements Runnable {
	
	private UploadSongDialog dialog = null;
	
	private File[] files = null;
	private boolean uploadApproved = false;
	private boolean skip = false;
	private boolean uploadDone = false;
	private boolean active = true;
	private boolean exit = false;
	
	private DecimalFormat df = new DecimalFormat("#.##");
	private long lastStatsUpdateTime = 0;
	private long lastStatsUpdateBytesRead = 0;
	private long bytesRead = 0;

	
	public UploadManager(File[] files) {
		this.files = files;
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("UploadManager");
		active = true;
		
		for (File file : files) {
			reset();
			
			Song song = new Song(Client.userName, file);
			dialog = new UploadSongDialog(new JFrame(), this);
			
			java.awt.EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	dialog.setVisible(true);
	            }
	        });

			dialog.fileLabel.setText(song.getTitle());
			dialog.sizeLabel.setText(song.getMBSize());
			
			if (requestUploadApproval(file)) {
				upload(file);
				waitForServer();
			}

			dialog.dispose();
			if (exit) {
				break;
			}
		}
	
		active = false;
	}
	
	private boolean requestUploadApproval(File file) {
		
		//send request
		Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_RECEIVE_SONG, file.getName(), file.length());
		
		//wait for approval/ disapproval
		while (!exit && !uploadApproved && !skip) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		return (uploadApproved && !exit && !skip);
	}
	
	private void upload(File file) {
		if (file != null && file.exists()) {
			try {
				FileInputStream fileIn = new FileInputStream(file);
				byte[] buffer = new byte[4096];
				int read = 0;
				
				//start sending the file
				try {
					while(!exit && read != -1) {
						read = fileIn.read(buffer,0 , buffer.length);
						if (read != -1) {
							bytesRead += read;
							byte[] chunk = new byte[read];
							System.arraycopy(buffer, 0, chunk, 0, read);
							Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_SONG_CHUNK, chunk, null);
						
							updateStats(file, bytesRead);
						}
					}
					Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_INFO_UPLOAD_ENDED, null, null);
					Client.database.removeSongToUpload(file);
				} finally {
					fileIn.close();
				}
			} catch (IOException e) {
				Applet.logger.i("Error while uploading song. \n"
						+ "Maybe the connection was lost?", e);
			}		
		}
	}
	
	private void updateStats(File file, long bytesRead) {

		if (System.currentTimeMillis()-lastStatsUpdateTime >= 500) {
	        String mbRead = df.format((double) (bytesRead/(1024.0*1024.0)));
	        int percent = (int) ( ((double)bytesRead/(double)file.length()) *100);
	       
	        int speed = (int) (0.5*(bytesRead-lastStatsUpdateBytesRead) / 1024) ;
	        
	        lastStatsUpdateTime = System.currentTimeMillis();
	        lastStatsUpdateBytesRead = bytesRead;
	        dialog.setStats(mbRead, percent, speed);
	    }
	}
	
	private void waitForServer() {
		while(!exit && !uploadDone) {
			try { Thread.sleep(100); }catch(InterruptedException e) {}
		}
	}
	
	public void approve() {
		uploadApproved = true;
	}
	
	public void uploadDone() {
		uploadDone = true;
	}
	
	public void cancelAll() {
		exit = true;
	}
	
	public void skip() {
		skip = true;
	}
	
	public boolean isActive() {
		return active;
	}
	
	private void reset() {
		skip = false;
		uploadApproved = false;
		uploadDone = false;
		lastStatsUpdateTime = 0;
		lastStatsUpdateBytesRead = 0;
		bytesRead = 0;
	}
}
