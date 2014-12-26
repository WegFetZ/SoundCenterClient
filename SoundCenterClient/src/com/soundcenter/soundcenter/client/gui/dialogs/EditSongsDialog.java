package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.actions.StationsTabActions;
import com.soundcenter.soundcenter.client.gui.renderer.SongListCellRenderer;
import com.soundcenter.soundcenter.client.util.GuiUtil;
import com.soundcenter.soundcenter.lib.data.Song;

@SuppressWarnings("serial")
public class EditSongsDialog extends JDialog {
	
	public EditStationDialog parent = null;
	
	public JList<Song> stationSongsList = new JList<Song>();
	public JList<Song> availableSongList = new JList<Song>();
	
	public JButton addButton = new JButton("+");
	public JButton removeButton = new JButton("-");
	public JButton upButton = new JButton("^");
	public JButton downButton = new JButton("v");
	public JButton applyButton = new JButton("Apply");
	public JButton cancelButton = new JButton("Cancel");
	
	public EditSongsDialog(EditStationDialog parent) {	
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		this.parent = parent;
		
		/* initialize components */		
		addButton.setEnabled(false);
		addButton.setMargin(new Insets(1,1,1,1));
		GuiUtil.setFixedSize(addButton, new Dimension(20, 20));
		
		removeButton.setEnabled(false);
		removeButton.setMargin(new Insets(1,1,1,1));
		GuiUtil.setFixedSize(removeButton, new Dimension(20, 20));
		
		upButton.setEnabled(false);
		upButton.setMargin(new Insets(1,1,1,1));
		GuiUtil.setFixedSize(upButton, new Dimension(30, 30));
		
		downButton.setEnabled(false);
		downButton.setMargin(new Insets(1,1,1,1));
		GuiUtil.setFixedSize(downButton, new Dimension(30, 30));
		
		stationSongsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		stationSongsList.setLayoutOrientation(JList.VERTICAL);
		stationSongsList.setVisibleRowCount(-1);
		stationSongsList.setCellRenderer(new SongListCellRenderer());
		JScrollPane stationSongsScroller = new JScrollPane(stationSongsList);
		stationSongsScroller.setPreferredSize(new Dimension(350, 160));
		
		availableSongList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		availableSongList.setLayoutOrientation(JList.VERTICAL);
		availableSongList.setVisibleRowCount(-1);
		availableSongList.setCellRenderer(new SongListCellRenderer());
		JScrollPane playerSongsScroller = new JScrollPane(availableSongList);
		playerSongsScroller.setPreferredSize(new Dimension(350, 160));
		
		
		//Actions
		final EditSongsDialog dialog = this;
		ListSelectionListener listListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				StationsTabActions.editSongsDialogListSelectionChanged(dialog, e);
			}
		};
		stationSongsList.getSelectionModel().addListSelectionListener(listListener);
		availableSongList.getSelectionModel().addListSelectionListener(listListener);
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editSongsDialogAddButtonPressed(dialog);		
			}
		});
		
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editSongsDialogRemoveButtonPressed(dialog);		
			}
		});
		
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editSongsDialogUpButtonPressed(dialog);		
			}
		});
		
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editSongsDialogDownButtonPressed(dialog);		
			}
		});
		
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editSongsDialogApplyButtonPressed(dialog);		
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();				
			}
		});
		
		
		//load properties
		loadProperties();
		
		
		/* build the gui */
		JComponent pane = (JComponent) getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(new JLabel("Current songs on the station:"));
			hBox1.add(Box.createHorizontalGlue());
		pane.add(hBox1);
		
		Box hBox2 = Box.createHorizontalBox();
			Box vBox1 = Box.createVerticalBox();
				vBox1.add(upButton);
				vBox1.add(Box.createRigidArea(new Dimension(0, 5)));
				vBox1.add(downButton);
			hBox2.add(vBox1);
			hBox2.add(Box.createRigidArea(new Dimension(4, 0)));
			hBox2.add(stationSongsScroller);
			hBox2.add(Box.createHorizontalGlue());
		pane.add(hBox2);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box hBox3 = Box.createHorizontalBox();
			hBox3.add(Box.createRigidArea(new Dimension(34, 0)));
			hBox3.add(addButton);
			hBox3.add(Box.createRigidArea(new Dimension(10, 0)));
			hBox3.add(removeButton);
			hBox3.add(Box.createHorizontalGlue());
		pane.add(hBox3);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box hBox4 = Box.createHorizontalBox();
			hBox4.add(new JLabel("Available Songs:"));
			hBox4.add(Box.createHorizontalGlue());
		pane.add(hBox4);
		pane.add(playerSongsScroller);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		pane.add(Box.createVerticalGlue());
		
		Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(applyButton);
			buttonBox.add(Box.createRigidArea(new Dimension(20,0)));
			buttonBox.add(cancelButton);
		pane.add(buttonBox);
		
		pack();	
		setLocationRelativeTo(parent);
	}	
	
	private void loadProperties() {	
		availableSongList.setModel(Client.database.getSongModel());
		
		//load songs
		DefaultListModel<Song> parentListModel = (DefaultListModel<Song>) parent.songList.getModel();
		DefaultListModel<Song> listModel = new DefaultListModel<Song>();		
		for(int i=0; i<parentListModel.getSize(); i++) {
			Song song = (Song) parentListModel.getElementAt(i);
			listModel.addElement(song);
		}
		
		stationSongsList.setModel(listModel);
	}
	
}
