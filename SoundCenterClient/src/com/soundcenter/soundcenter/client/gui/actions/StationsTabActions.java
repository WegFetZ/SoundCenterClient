package com.soundcenter.soundcenter.client.gui.actions;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.dialogs.AddStationDialog;
import com.soundcenter.soundcenter.client.gui.dialogs.EditSongsDialog;
import com.soundcenter.soundcenter.client.gui.dialogs.EditStationDialog;
import com.soundcenter.soundcenter.client.gui.renderer.AreaListCellRenderer;
import com.soundcenter.soundcenter.client.gui.renderer.BoxListCellRenderer;
import com.soundcenter.soundcenter.client.gui.renderer.RegionListCellRenderer;
import com.soundcenter.soundcenter.lib.data.Area;
import com.soundcenter.soundcenter.lib.data.Box;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Region;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.data.WGRegion;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;

public class StationsTabActions {
	
	public static void tabOpened() {
	}
	
	/* ---------------------- Stations Tab ------------------------- */
	
	public static void stationChooserSelected() {
		JComboBox<String> playerComboBox = App.gui.stationsTab.playerComboBox;
		JComboBox<String> typeComboBox = App.gui.stationsTab.typeComboBox;
		
		DefaultListModel<Station> model = null;
		ListCellRenderer<Station> renderer = null;
		boolean addButtonEnabled = false;
		if (playerComboBox.getSelectedIndex() >= 0) {
			String type = (String) typeComboBox.getSelectedItem();
			String player = (String) playerComboBox.getSelectedItem();
			
			if(type.equals("Areas")) {
				model = Client.database.getAreaModel(player);
				renderer = new AreaListCellRenderer();

			} else if(type.equals("Boxes")) {
				model = Client.database.getBoxModel(player);
				renderer = new BoxListCellRenderer();
				
			} else if(type.equals("Biomes")) {
				model = Client.database.getBiomeModel(player);
				renderer = new RegionListCellRenderer();
				if (Client.database.permissionGranted("sc.set.biome"))
					addButtonEnabled = true;
				
			} else if(type.equals("Worlds")) {
				model = Client.database.getWorldModel(player);
				renderer = new RegionListCellRenderer();
				if (Client.database.permissionGranted("sc.set.world"))
					addButtonEnabled = true;

			} else if(type.equals("WorldGuard Regions")) {
				model = Client.database.getWGRegionModel(player);
				renderer = new RegionListCellRenderer();
			}
			
			if (model != null && renderer != null) {
				App.gui.stationsTab.stationList.setCellRenderer(null);
				App.gui.stationsTab.stationList.setModel(model);
				App.gui.stationsTab.stationList.setCellRenderer(renderer);
			} else {
				App.gui.stationsTab.stationList.setModel(new DefaultListModel<Station>());
			}
			
			if (player.equals(Client.userName)) {
				App.gui.stationsTab.addButton.setEnabled(addButtonEnabled);
				App.gui.stationsTab.editButton.setEnabled(true);
				App.gui.stationsTab.deleteButton.setEnabled(true);
			} else {
				App.gui.stationsTab.addButton.setEnabled(false);
				if (Client.database.permissionGranted("sc.others.edit")) {
					App.gui.stationsTab.editButton.setEnabled(true);
				} else {
					App.gui.stationsTab.editButton.setEnabled(false);
				}
				if (Client.database.permissionGranted("sc.others.delete")) {
					App.gui.stationsTab.deleteButton.setEnabled(true);
				} else {
					App.gui.stationsTab.deleteButton.setEnabled(false);
				}
			}
		} else {
			App.gui.stationsTab.stationList.setModel(new DefaultListModel<Station>());
			App.gui.stationsTab.addButton.setEnabled(false);
			App.gui.stationsTab.editButton.setEnabled(false);
			App.gui.stationsTab.deleteButton.setEnabled(false);
		}
	}	
	
	public static void listSelectionChanged() {
		Station station = App.gui.stationsTab.stationList.getSelectedValue();
		
		if (station != null) {
			App.gui.stationsTab.muteCheckBox.setEnabled(true);
			App.gui.stationsTab.muteCheckBox.setSelected(	
					Client.database.isMuted(station.getType(), station.getId()));
		} else {
			App.gui.stationsTab.muteCheckBox.setEnabled(false);
			App.gui.stationsTab.muteCheckBox.setSelected(false);
		}
	}
	
	public static void editStationButtonPressed() {
		byte type = (byte) App.gui.stationsTab.typeComboBox.getSelectedIndex();
		
		Station station = App.gui.stationsTab.stationList.getSelectedValue();
		if (station != null) {
			EditStationDialog editDialog = new EditStationDialog(new JFrame(), type, station);
			editDialog.setVisible(true);
		} 	
	}
	
	public static void addStationButtonPressed() {
		byte type = (byte) App.gui.stationsTab.typeComboBox.getSelectedIndex();
		
		if (type >= 0) {
			AddStationDialog addDialog = new AddStationDialog(new JFrame(), type);
			addDialog.setVisible(true);
		} 	
	}
	
	public static void deleteStationButtonPressed() {
		
		Station station = App.gui.stationsTab.stationList.getSelectedValue();
		
		if (station != null) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_DELETE_STATION, station.getType(), station.getId());
		} 	
	}
	
	public static void muteCheckBoxSelected() {
		Station station = App.gui.stationsTab.stationList.getSelectedValue();
		if (station == null) {
			return;
		}
		if (App.gui.stationsTab.muteCheckBox.isSelected()) {
			Client.database.addMutedStation(station.getType(), station.getId());
			App.audioManager.stopStationPlayer(station.getType(), station.getId(), true);
		} else {
			Client.database.removeMutedStation(station.getType(), station.getId());
		}
	}	
	
	/* ---------------------- Edit Dialog ------------------------- */
	
	public static void editStationDialogApplyButtonPressed(EditStationDialog dialog) {
		Station oldStation = dialog.station;
		byte type = oldStation.getType();;
		
		Station station = null;
		
		if (type == GlobalConstants.TYPE_AREA) {
			station = new Area(oldStation.getId(), oldStation.getOwner(), oldStation.getMin(), oldStation.getMax(), oldStation.getRange());
			
		} else if (type == GlobalConstants.TYPE_BOX) {
			station = new Box(oldStation.getId(), oldStation.getOwner(), oldStation.getLocation(), oldStation.getRange());
			
		} else if (type == GlobalConstants.TYPE_BIOME) {
			station = new Region(oldStation.getId(), oldStation.getOwner(), oldStation.getName(), type);
			
		} else if (type == GlobalConstants.TYPE_WORLD) {
			station = new Region(oldStation.getId(), oldStation.getOwner(), oldStation.getName(), type);
		
		} else if (type == GlobalConstants.TYPE_WGREGION) {
			station = new WGRegion(oldStation.getId(), oldStation.getOwner(), oldStation.getName(), oldStation.getMin(), oldStation.getMax(), oldStation.getPoints());
		}
		
		if (type == GlobalConstants.TYPE_AREA || type == GlobalConstants.TYPE_BOX) {
			try {
				int range = Integer.parseInt(dialog.rangeField.getText());
				if (range < 0) {
					throw new NumberFormatException();
				}
				station.setRange(range);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Range must be a number >= 0!", "Error", JOptionPane.OK_OPTION);
				return;
			}
		}
			
		byte maxVolume = (byte) dialog.maxVolumeSlider.getValue();
		station.setMaxVolume(maxVolume);
			
		try {
			byte priority = (byte) Byte.parseByte(dialog.priorityField.getText());
			if (priority < 1) {
				throw new NumberFormatException();
			} else if (priority > 10) {
				throw new NumberFormatException();
			}
			station.setPriority(priority);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Priority must be a number between 1 and 10 (including)!", "Error", JOptionPane.OK_OPTION);
			return;
		}
			
		station.setEditableByOthers(dialog.editableByOthersCheckBox.isSelected());
		station.setStartFromBeginning(dialog.startFromBeginningCheckBox.isSelected());
		station.setLoop(dialog.loopCheckBox.isSelected());
		
		station.removeAllSongs();
		DefaultListModel<Song> songsModel = (DefaultListModel<Song>) dialog.songList.getModel();
		for (Object song : songsModel.toArray()) {
			station.addSong((Song) song);
		}
		
		//send the edited station to the server
		Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_EDIT_STATION, type, station);
		
		dialog.dispose();
	}
	
	public static void editStationDialogEditSonglistButtonPressed(EditStationDialog parentDialog) {
		EditSongsDialog dialog = new EditSongsDialog(parentDialog);
		dialog.setVisible(true);
	}
	
	
	//Edit songs
	public static void editSongsDialogListSelectionChanged(EditSongsDialog dialog, ListSelectionEvent e) {		
		if (e.getSource().equals(dialog.stationSongsList.getSelectionModel())) {
			dialog.availableSongList.clearSelection();
		} else if (e.getSource().equals(dialog.availableSongList.getSelectionModel())) {
			dialog.stationSongsList.clearSelection();
		}
		
		if (dialog.stationSongsList.getSelectedIndex() >= 0) {
			dialog.removeButton.setEnabled(true);
			dialog.addButton.setEnabled(false);
			dialog.upButton.setEnabled(true);
			dialog.downButton.setEnabled(true);
			
		} else if (dialog.availableSongList.getSelectedIndex() >= 0) {
			dialog.removeButton.setEnabled(false);
			dialog.addButton.setEnabled(true);
			dialog.upButton.setEnabled(false);
			dialog.downButton.setEnabled(false);
		}
	}
	
	public static void editSongsDialogAddButtonPressed(EditSongsDialog dialog) {
		List<Song> songs =  dialog.availableSongList.getSelectedValuesList();
		if (songs != null) {
			for (Song song: songs) {
				DefaultListModel<Song> model = (DefaultListModel<Song>) dialog.stationSongsList.getModel();
				model.addElement(song);
			}
		}
	}

	public static void editSongsDialogRemoveButtonPressed(EditSongsDialog dialog) {
		List<Song> songs =  dialog.stationSongsList.getSelectedValuesList();
		if (songs != null) {
			for (Song song: songs) {
				DefaultListModel<Song> model = (DefaultListModel<Song>) dialog.stationSongsList.getModel();
				model.removeElement(song);
			}
		}
	}

	public static void editSongsDialogUpButtonPressed(EditSongsDialog dialog) {
		int[] indices = dialog.stationSongsList.getSelectedIndices();
		if (indices != null) {
				for (int index : indices) {
					if (index > 0) {
						DefaultListModel<Song> model = (DefaultListModel<Song>) dialog.stationSongsList.getModel();
						Song temp = model.get(index -1);
						model.set(index-1, model.get(index));
						model.set(index, temp);
						dialog.stationSongsList.setSelectedIndex(index-1);
					}
				}
		}
	}

	public static void editSongsDialogDownButtonPressed(EditSongsDialog dialog) {
		int[] indices = dialog.stationSongsList.getSelectedIndices();
		DefaultListModel<Song> model = (DefaultListModel<Song>) dialog.stationSongsList.getModel();
		if (indices != null) {
			for (int index : indices) {
				if (index < model.getSize()-1) {
					Song temp = model.get(index +1);
					model.set(index+1, model.get(index));
					model.set(index, temp);
					dialog.stationSongsList.setSelectedIndex(index+1);
				}	
			}
		}
	}

	public static void editSongsDialogApplyButtonPressed(EditSongsDialog dialog) {
		dialog.parent.songList.setModel(dialog.stationSongsList.getModel());
		dialog.dispose();
	}
	
	
	/* ---------------------- Add Dialog ------------------------- */
	
	public static void addStationDialogListSelectionChanged(AddStationDialog dialog) {
		if (dialog.availableList.getSelectedIndex() >= 0) {
			dialog.addButton.setEnabled(true);
		} else {
			dialog.addButton.setEnabled(false);
		}
	}
	
	public static void addStationDialogAddButtonPressed(AddStationDialog dialog) {
		byte type = dialog.type;
		
		if (dialog.availableList.getSelectedIndex() >= 0) {
			String name = (String) dialog.availableList.getSelectedValue();
			Station station = new Region((short) 0, Client.userName, name, type);

			//send a create station command to the server
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_CREATE_STATION, station.getType(), station);
			
			dialog.dispose();
		}
	}
}
