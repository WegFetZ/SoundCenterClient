package com.soundcenter.soundcenter.client.gui.tabs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.actions.MusicTabActions;
import com.soundcenter.soundcenter.client.gui.dialogs.AddSongDialog;
import com.soundcenter.soundcenter.client.gui.renderer.SongListCellRenderer;
import com.soundcenter.soundcenter.lib.data.Song;

@SuppressWarnings("serial")
public class MusicTab extends JPanel {
	
	public AddSongDialog addSongDialog = null;
	
	public JList<Song> songList = new JList<Song>();
	
	public JButton addButton = new JButton("Add...");
	public JButton deleteButton = new JButton("Delete");
	public JButton playButton = new JButton("Play...");
	public JButton stopButton = new JButton("Stop...");
	
	public MusicTab() {
		
		/* initialize components */
		songList.setCellRenderer(new SongListCellRenderer());
		songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		songList.setLayoutOrientation(JList.VERTICAL);
		songList.setVisibleRowCount(-1);
		songList.setModel(Client.database.getSongModel());
		JScrollPane musicScroller = new JScrollPane(songList);
		musicScroller.setPreferredSize(new Dimension(450, 280));
		musicScroller.setMinimumSize(new Dimension(200,80));
		
		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
		playButton.setEnabled(false);
		stopButton.setEnabled(false);
		
		//Actions	
		songList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				MusicTabActions.listSelectionChanged();
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.addButtonPressed();
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.deleteButtonPressed(songList);
			}
		});
		
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.playButtonPressed(songList);
			}
		});
		
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.stopButtonPressed(songList);
			}
		});
		
		/* build gui */		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		add(musicScroller);
		
		add(Box.createRigidArea(new Dimension(0,10)));
			
		Box controlsBox = Box.createHorizontalBox();
			controlsBox.add(addButton);
			controlsBox.add(Box.createRigidArea(new Dimension(20, 0)));
			controlsBox.add(deleteButton);
			controlsBox.add(Box.createHorizontalGlue());
			controlsBox.add(playButton);
			controlsBox.add(Box.createRigidArea(new Dimension(20, 0)));
			controlsBox.add(stopButton);
		add(controlsBox);
			
		add(Box.createVerticalGlue());
		
	}

}
