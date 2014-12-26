package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soundcenter.soundcenter.client.gui.actions.StationsTabActions;
import com.soundcenter.soundcenter.client.gui.renderer.SongListCellRenderer;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.SCLocation;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;

@SuppressWarnings("serial")
public class EditStationDialog extends JDialog {
	
	public byte type = 0;
	public Station station = null;

	public JLabel nameLabel = new JLabel("");
	public JLabel idLabel = new JLabel("");
	public JLabel ownerLabel = new JLabel("");
	public JLabel locationLabel = new JLabel("");
	public JLabel worldLabel = new JLabel("");
	public JLabel maxVolumeLabel = new JLabel("100");
	
	public JTextField rangeField = new JTextField("25");
	public JTextField priorityField = new JTextField("1");
	
	public JSlider maxVolumeSlider = new JSlider();
	
	public JCheckBox editableByOthersCheckBox = new JCheckBox("Station is editable by others.");
	public JCheckBox startFromBeginningCheckBox = new JCheckBox("Always start playlist from beginning when coming in range of the station.");
	public JCheckBox loopCheckBox = new JCheckBox("Loop playlist.");
	
	public JList<Song> songList = new JList<Song>();
	
	public JButton editSongsButton = new JButton("Edit Songlist...");
	public JButton applyButton = new JButton("Apply");
	public JButton cancelButton = new JButton("Cancel");
	
	public EditStationDialog(JFrame parent, byte type, Station station) {	
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		this.type = type;
		this.station = station;
		
		/* initialize components */
		rangeField.setPreferredSize(new Dimension(40, 25));
		priorityField.setPreferredSize(new Dimension(40, 25));
		
		maxVolumeSlider.setValue(100);
		
		songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		songList.setLayoutOrientation(JList.VERTICAL);
		songList.setVisibleRowCount(-1);
		songList.setModel(new DefaultListModel<Song>());
		songList.setCellRenderer(new SongListCellRenderer());
		
		JScrollPane songScroller = new JScrollPane(songList);
		songScroller.setPreferredSize(new Dimension(350, 160));
		
		//Actions
		final EditStationDialog frame = this;
		
		maxVolumeSlider.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent ce) {
	            maxVolumeLabel.setText(String.valueOf(maxVolumeSlider.getValue()));
	        }
	    });
		
		editSongsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.editStationDialogEditSonglistButtonPressed(frame);
			}
		});
		
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.editStationDialogApplyButtonPressed(frame);		
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();				
			}
		});
		
		/* load station properties */
		loadProperties(type, station);
		
		
		/* build the gui */
		JComponent pane = (JComponent) getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		if (type == GlobalConstants.TYPE_BIOME || type == GlobalConstants.TYPE_WORLD || type == GlobalConstants.TYPE_WGREGION) {
			Box nameBox = Box.createHorizontalBox();
				JLabel nameTitleLabel = new JLabel("Name: ");
				nameTitleLabel.setPreferredSize(new Dimension(100, 20));
				nameBox.add(nameTitleLabel);
				nameBox.add(nameLabel);
				nameBox.add(Box.createHorizontalGlue());
			pane.add(nameBox);
			
			pane.add(Box.createRigidArea(new Dimension(0,10)));
		}
			
		Box idBox = Box.createHorizontalBox();
			JLabel idTitleLabel = new JLabel("ID: ");
			idTitleLabel.setPreferredSize(new Dimension(100, 20));
			idBox.add(idTitleLabel);
			idBox.add(idLabel);
			idBox.add(Box.createHorizontalGlue());
		pane.add(idBox);
		
		pane.add(Box.createRigidArea(new Dimension(0,10)));
		
		Box ownerBox = Box.createHorizontalBox();
			JLabel ownerTitleLabel = new JLabel("Owner: ");
			ownerTitleLabel.setPreferredSize(new Dimension(100, 20));
			ownerBox.add(ownerTitleLabel);
			ownerBox.add(ownerLabel);
			ownerBox.add(Box.createHorizontalGlue());
		pane.add(ownerBox);
		
		pane.add(Box.createRigidArea(new Dimension(0,10)));
		
		if (type == GlobalConstants.TYPE_AREA || type == GlobalConstants.TYPE_BOX) {
			Box locationBox = Box.createHorizontalBox();
				JLabel locationTitleLabel = new JLabel("Location: ");
				locationTitleLabel.setPreferredSize(new Dimension(100, 20));
				locationBox.add(locationTitleLabel);
				locationBox.add(locationLabel);
				locationBox.add(Box.createHorizontalGlue());
			pane.add(locationBox);
			
			pane.add(Box.createRigidArea(new Dimension(0,20)));
			
			Box worldBox = Box.createHorizontalBox();
				JLabel worldTitleLabel = new JLabel("World: ");
				worldTitleLabel.setPreferredSize(new Dimension(100, 20));
				worldBox.add(worldTitleLabel);
				worldBox.add(worldLabel);
				worldBox.add(Box.createHorizontalGlue());
			pane.add(worldBox);
			
			pane.add(Box.createRigidArea(new Dimension(0,20)));
		}
		
		if (type == GlobalConstants.TYPE_AREA || type == GlobalConstants.TYPE_BOX) {
			Box rangeBox = Box.createHorizontalBox();
				String titleString = "Fadeout-Range: ";
				if (type == GlobalConstants.TYPE_BOX)
					titleString = "Range: ";			
				JLabel rangeTitleLabel = new JLabel(titleString);
				rangeTitleLabel.setPreferredSize(new Dimension(100, 20));
				rangeBox.add(rangeTitleLabel);
				rangeBox.add(rangeField);
				rangeBox.add(Box.createHorizontalGlue());
			pane.add(rangeBox);
			
			pane.add(Box.createRigidArea(new Dimension(0,10)));
		}
		
		Box maxVolumeBox = Box.createHorizontalBox();
			JLabel maxVolumeTitleLabel = new JLabel("Maximum Volume: ");
			maxVolumeLabel.setPreferredSize(new Dimension(100, 20));
			maxVolumeBox.add(maxVolumeTitleLabel);
			maxVolumeBox.add(maxVolumeSlider);
			maxVolumeBox.add(Box.createRigidArea(new Dimension(5,0)));
			maxVolumeBox.add(maxVolumeLabel);
			maxVolumeBox.add(Box.createHorizontalGlue());
		pane.add(maxVolumeBox);
			
		pane.add(Box.createRigidArea(new Dimension(0,15)));
		
		Box priorityBox = Box.createHorizontalBox();
			JLabel priorityTitleLabel = new JLabel("Priority: ");
			priorityTitleLabel.setPreferredSize(new Dimension(100, 20));
			priorityBox.add(priorityTitleLabel);
			priorityBox.add(priorityField);
			priorityBox.add(Box.createHorizontalGlue());
		pane.add(priorityBox);
			
		pane.add(Box.createRigidArea(new Dimension(0,15)));
		
		Box editableBox = Box.createHorizontalBox();
			editableBox.add(editableByOthersCheckBox);
			editableBox.add(Box.createHorizontalGlue());
		pane.add(editableBox);	
		
		pane.add(Box.createRigidArea(new Dimension(0,15)));
		
		Box songBox = Box.createHorizontalBox();
			songBox.add(new JLabel("Songs:"));
			songBox.add(Box.createHorizontalGlue());
		pane.add(songBox);
		pane.add(songScroller);
		pane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		Box songButtonBox = Box.createHorizontalBox();
			songButtonBox.add(editSongsButton);
			songButtonBox.add(Box.createHorizontalGlue());			
		pane.add(songButtonBox);
		
		pane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(startFromBeginningCheckBox);
			hBox1.add(Box.createHorizontalGlue());
		pane.add(hBox1);
		
		pane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		Box hBox2 = Box.createHorizontalBox();
			hBox2.add(loopCheckBox);
			hBox2.add(Box.createHorizontalGlue());
		pane.add(hBox2);
		
		pane.add(Box.createRigidArea(new Dimension(0, 15)));
		pane.add(Box.createVerticalGlue());
		
		Box doneButtonBox = Box.createHorizontalBox();
			doneButtonBox.add(Box.createHorizontalGlue());
			doneButtonBox.add(applyButton);
			doneButtonBox.add(Box.createRigidArea(new Dimension(20,0)));
			doneButtonBox.add(cancelButton);
		pane.add(doneButtonBox);
		
		pack();
		setLocationRelativeTo(parent);
	}
	
	private void loadProperties(byte type, Station station) {
		
		//load common properties
		idLabel.setText(String.valueOf(station.getId()));
		ownerLabel.setText(station.getOwner());
		maxVolumeLabel.setText(String.valueOf(station.getMaxVolume()));
		maxVolumeSlider.setValue(station.getMaxVolume());
		priorityField.setText(String.valueOf(station.getPriority()));
		rangeField.setText(String.valueOf(station.getRange()));
		editableByOthersCheckBox.setSelected(station.isEditableByOthers());
		startFromBeginningCheckBox.setSelected(station.shouldStartFromBeginning());
		loopCheckBox.setSelected(station.shouldLoop());
		
		//load specific properties
		String title = "Edit Station";
		switch (type) {
		case GlobalConstants.TYPE_AREA:
			title = "Edit Area";
			SCLocation min = station.getMin();
			SCLocation max = station.getMax();
			locationLabel.setText("(" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() 
					+ ")  -  (" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")");
			worldLabel.setText(station.getWorld());
			break;
			
		case GlobalConstants.TYPE_BOX:
			title = "Edit Box";
			SCLocation loc = station.getLocation();
			locationLabel.setText(loc.getPoint());
			worldLabel.setText(station.getWorld());
			break;
			
		case GlobalConstants.TYPE_BIOME:
			title = "Edit Biome";
			nameLabel.setText(station.getName());
			break;
			
		case GlobalConstants.TYPE_WORLD:
			title = "Edit World";
			nameLabel.setText(station.getName());
			break;
		}
		setTitle(title);
		
		//populate songlist
		DefaultListModel<Song> songListModel = (DefaultListModel<Song>) songList.getModel();
		for (Song song : station.getSongs()) {
			songListModel.addElement(song);
		}
	}

}
