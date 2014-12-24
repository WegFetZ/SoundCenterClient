package com.soundcenter.soundcenter.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;


public class Database {
	
	public ConcurrentHashMap<Short, Station> areas = new ConcurrentHashMap<Short, Station>();
	public ConcurrentHashMap<Short, Station> boxes = new ConcurrentHashMap<Short, Station>();
	public ConcurrentHashMap<Short, Station> biomes = new ConcurrentHashMap<Short, Station>();
	public ConcurrentHashMap<Short, Station> worlds = new ConcurrentHashMap<Short, Station>();
	public ConcurrentHashMap<Short, Station> wgRegions = new ConcurrentHashMap<Short, Station>();
	public List<Song> songs = new ArrayList<Song>();
	
	private ConcurrentHashMap<String, DefaultListModel> areaModels = new ConcurrentHashMap<String, DefaultListModel>();
	private ConcurrentHashMap<String, DefaultListModel> boxModels = new ConcurrentHashMap<String, DefaultListModel>();
	private ConcurrentHashMap<String, DefaultListModel> biomeModels = new ConcurrentHashMap<String, DefaultListModel>();
	private ConcurrentHashMap<String, DefaultListModel> worldModels = new ConcurrentHashMap<String, DefaultListModel>();
	private ConcurrentHashMap<String, DefaultListModel> wgRegionModels = new ConcurrentHashMap<String, DefaultListModel>();
	private ConcurrentHashMap<String, DefaultListModel> songModels = new ConcurrentHashMap<String, DefaultListModel>();
	
	private DefaultListModel availableBiomesModel = new DefaultListModel();
	private DefaultListModel availableWorldsModel = new DefaultListModel();
	
	private DefaultListModel songsToUploadModel = new DefaultListModel();
	
	public List<Short> mutedAreas = new ArrayList<Short>(); 
	public List<Short> mutedBoxes = new ArrayList<Short>(); 
	public List<Short> mutedBiomes = new ArrayList<Short>(); 
	public List<Short> mutedWorlds = new ArrayList<Short>(); 
	public List<Short> mutedWGRegions = new ArrayList<Short>(); 
	
	private List<String> permissions = new ArrayList<String>();
	
	
	public void addStation(Station station) {
		ConcurrentHashMap<Short, Station> stationMap = getStationMap(station.getType());
		ConcurrentHashMap<String, DefaultListModel> modelMap = getModelMap(station.getType());
		
		if (stationMap != null && modelMap != null) {
			removeStation(station.getType(), station.getId(), false);
			
			stationMap.put(station.getId(), station);
			DefaultListModel model = null;
			if (modelMap.containsKey(station.getOwner())) {
				model = modelMap.get(station.getOwner());
				
			} else {			
				model = new DefaultListModel();
				modelMap.put(station.getOwner(), model);
			}
			
			model.addElement(station);
			
			String item = "";
			switch (station.getType()) {
			case GlobalConstants.TYPE_AREA:
				item = "Areas";
				break;
			case GlobalConstants.TYPE_BOX:
				item = "Boxes";
				break;
			case GlobalConstants.TYPE_BIOME:
				item = "Biomes";
				removeAvailableBiome(station.getName());
				break;
			case GlobalConstants.TYPE_WORLD:
				item = "Worlds";
				removeAvailableWorld(station.getName());
				break;
			case GlobalConstants.TYPE_WGREGION:
				item = "WorldGuard Regions";
			}
			updateStationsTab(station.getOwner(), item, model);
		}
	}
	
	public boolean hasStation(byte type, short id) {
		ConcurrentHashMap<Short, Station> map = getStationMap(type);
		return map.containsKey(id);
	}

	public Station getStation(byte type, short id) {
		Station station = null;
		ConcurrentHashMap<Short, Station> map = getStationMap(type);
		if (map != null) {
			station = (Station) map.get(id);
		}

		return station;
	}

	public Station removeStation(byte type, short id, boolean updateAvailables) {
		Station station = getStation(type, id);
		if (station != null) {
			removeStation(station, updateAvailables);
		}
		
		return station;
	}
	
	public void removeStation(Station station, boolean updateAvailables) {
		ConcurrentHashMap<Short, Station> stationMap = getStationMap(station.getType());
		ConcurrentHashMap<String, DefaultListModel> modelMap = getModelMap(station.getType());

		if (stationMap != null) {
			stationMap.remove(station.getId());

		}
		if (modelMap != null) {			
			DefaultListModel model = null;
			if (modelMap.containsKey(station.getOwner())) {
				model = modelMap.get(station.getOwner());
				model.removeElement(station);
			}

			String item = "";
			switch (station.getType()) {
			case GlobalConstants.TYPE_AREA:
				item = "Areas";
				break;
			case GlobalConstants.TYPE_BOX:
				item = "Boxes";
				break;
			case GlobalConstants.TYPE_WGREGION:
				item = "WorldGuard Regions";
				break;
			case GlobalConstants.TYPE_BIOME:
				item = "Biomes";
				if (updateAvailables) {
					addAvailableBiome(station.getName());
				}
				break;
			case GlobalConstants.TYPE_WORLD:
				item = "Worlds";
				if (updateAvailables) {
					addAvailableWorld(station.getName());
				}
			}
			updateStationsTab(station.getOwner(), item, model);
		}
	}

	private ConcurrentHashMap<Short, Station> getStationMap(byte type) {
		switch (type) {
		case GlobalConstants.TYPE_AREA:
			return areas;
		case GlobalConstants.TYPE_BOX:
			return boxes;
		case GlobalConstants.TYPE_BIOME:
			return biomes;
		case GlobalConstants.TYPE_WORLD:
			return worlds;
		case GlobalConstants.TYPE_WGREGION:
			return wgRegions;
		default:
			return null;
		}
	}
	
	private ConcurrentHashMap<String, DefaultListModel> getModelMap(byte type) {
		switch (type) {
		case GlobalConstants.TYPE_AREA:
			return areaModels;
		case GlobalConstants.TYPE_BOX:
			return boxModels;
		case GlobalConstants.TYPE_BIOME:
			return biomeModels;
		case GlobalConstants.TYPE_WORLD:
			return worldModels;
		case GlobalConstants.TYPE_WGREGION:
			return wgRegionModels;
		default:
			return null;
		}
	}
	
	/* --------------------------- AREAS -------------------------- */
	public DefaultListModel getAreaModel(String player) {
		return areaModels.get(player);
	}

	
	/* --------------------------- BOXES -------------------------- */
	public DefaultListModel getBoxModel(String player) {
		return boxModels.get(player);
	}
	
	
	/* --------------------------- WGREGIONS -------------------------- */
	public DefaultListModel getWGRegionModel(String player) {
		return wgRegionModels.get(player);
	}
	
	
	/* --------------------------- BIOMES -------------------------- */	
	public DefaultListModel getBiomeModel(String player) {
		return biomeModels.get(player);
	}
	
	public DefaultListModel getAvailableBiomes() {
		return availableBiomesModel;
	}
	
	public void addAvailableBiome(String biome) {
		availableBiomesModel.addElement(biome);
	}
	
	public void removeAvailableBiome(String biome) {
		availableBiomesModel.removeElement(biome);
	}
	
	/* --------------------------- WORLDS -------------------------- */
	public DefaultListModel getWorldModel(String player) {
		return worldModels.get(player);
	}
	
	public void addAvailableWorld(String world) {
		availableWorldsModel.addElement(world);
	}
	
	public DefaultListModel getAvailableWorlds() {
		return availableWorldsModel;
	}
	
	public void removeAvailableWorld(String world) {
		availableWorldsModel.removeElement(world);
	}
	
	
	/* --------------------------- SONGS -------------------------- */
	public ConcurrentHashMap<String, DefaultListModel> getSongModelsMap() {
		return songModels;
	}
	
	public DefaultListModel getSongModel(String player) {
		return songModels.get(player);
	}
	
	public void addSong(Song song) {
			
		songs.remove(song);
		songs.add(song);
		
		DefaultListModel model = null;
		if (songModels.containsKey(song.getOwner())) {
			model = songModels.get(song.getOwner());
		
		} else {			
			model = new DefaultListModel();
			songModels.put(song.getOwner(), model);
		}
		
		model.addElement(song);
		
		updateMusicTab(song.getOwner(), model);
	}
	
	public void removeSong(Song song) {
		
		DefaultListModel model = null;
		if (songModels.containsKey(song.getOwner())) {
			model = songModels.get(song.getOwner());
			model.removeElement(song);
		}
		
		updateMusicTab(song.getOwner(), null);
		
		for (Entry<Short, Station> entry : areas.entrySet()) {
			entry.getValue().removeSong(song);
		}
		for (Entry<Short, Station> entry : boxes.entrySet()) {
			entry.getValue().removeSong(song);
		}
		for (Entry<Short, Station> entry : biomes.entrySet()) {
			entry.getValue().removeSong(song);
		}
		for (Entry<Short, Station> entry : worlds.entrySet()) {
			entry.getValue().removeSong(song);
		}
		for (Entry<Short, Station> entry : wgRegions.entrySet()) {
			entry.getValue().removeSong(song);
		}
	}
	
	
	/* --------------------------- MUTES --------------------------------*/
	public void addMutedStation(byte type, Short id) {
		List<Short> list = getMutedList(type);
		if (list != null) {
			list.add(id);
		}
	}
	
	public void removeMutedStation(byte type, Short id) {
		List<Short> list = getMutedList(type);
		if (list != null) {
			list.remove(id);
		}
	}
	
	public boolean isMuted(byte type, Short id) {
		List<Short> list = getMutedList(type);
		if (list != null) {
			return list.contains(id);
		}
		return false;
	}
	
	public List<Short> getMutedList(byte type) {
		switch (type) {
		case GlobalConstants.TYPE_AREA:
			return mutedAreas;
		case GlobalConstants.TYPE_BOX:
			return mutedBoxes;
		case GlobalConstants.TYPE_BIOME:
			return mutedBiomes;
		case GlobalConstants.TYPE_WORLD:
			return mutedWorlds;
		case GlobalConstants.TYPE_WGREGION:
			return mutedWGRegions;
		default:
			return null;
		}
	}
	
	
	/* --------------------------- MISC -------------------------- */
	public void addPermission(String perm) {
		permissions.add(perm);
	}
	
	public boolean permissionGranted(String perm) {
		return permissions.contains(perm);
	}
	
	
	public void updateStationsTab(String player, String type, DefaultListModel listModel) {
		if (listModel != null) {
			DefaultComboBoxModel typeChooserModel = (DefaultComboBoxModel) App.gui.stationsTab.typeComboBox.getModel();
			DefaultComboBoxModel playerChooserModel = (DefaultComboBoxModel) App.gui.stationsTab.playerComboBox.getModel();
			if (playerChooserModel.getIndexOf(player) < 0) {
				if (player.equals(Client.userName)) {
					playerChooserModel.insertElementAt(player, 0);
				} else {
					playerChooserModel.addElement(player);
				}
			}
			if (player.equals(Client.userName) || playerChooserModel.getSelectedItem() == null) {
				playerChooserModel.setSelectedItem(null);
				playerChooserModel.setSelectedItem(player);
				typeChooserModel.setSelectedItem(type);
			}		

		} else if (!areaModels.containsKey(player) && !boxModels.containsKey(player) 
					&& !biomeModels.containsKey(player) && !worldModels.containsKey(player) 
					&& !player.equals(Client.userName)) {
			App.gui.stationsTab.playerComboBox.removeItem(player);
		}
	}
		
	public void updateMusicTab(String player, DefaultListModel listModel) {
		if (listModel != null) {
			DefaultComboBoxModel chooserModel = (DefaultComboBoxModel) App.gui.musicTab.playerComboBox.getModel();
			if (chooserModel.getIndexOf(player) < 0) {
				if (player.equals(Client.userName)) {
					chooserModel.insertElementAt(player, 0);
				} else {
					chooserModel.addElement(player);
				}
			}
			if (player.equals(Client.userName) || chooserModel.getSelectedItem() == null) {
				chooserModel.setSelectedItem(null);
				chooserModel.setSelectedItem(player);
			}
			
		} else if (!songModels.containsKey(player) && !player.equals(Client.userName)) {
			App.gui.musicTab.playerComboBox.removeItem(player);
		}
	}
	
	public void reset() {
		for (Entry<String, DefaultListModel> entry : areaModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		for (Entry<String, DefaultListModel> entry : boxModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		for (Entry<String, DefaultListModel> entry : biomeModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		for (Entry<String, DefaultListModel> entry : worldModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		for (Entry<String, DefaultListModel> entry : wgRegionModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		for (Entry<String, DefaultListModel> entry : songModels.entrySet()) {
			DefaultListModel model = entry.getValue();
			model.removeAllElements();
		}
		
		availableBiomesModel.removeAllElements();
		availableWorldsModel.removeAllElements();
		songsToUploadModel.removeAllElements();
		
		areas.clear();
		boxes.clear();
		biomes.clear();
		worlds.clear();
		songs.clear();
		areaModels.clear();
		boxModels.clear();
		biomeModels.clear();
		worldModels.clear();
		wgRegionModels.clear();
		songModels.clear();
		permissions.clear();
		
		DefaultComboBoxModel playerMusicModel = (DefaultComboBoxModel) App.gui.musicTab.playerComboBox.getModel();
		DefaultComboBoxModel playerStationsModel = (DefaultComboBoxModel) App.gui.stationsTab.playerComboBox.getModel();
		playerMusicModel.setSelectedItem(null);
		App.gui.musicTab.playerComboBox.removeAllItems();
		playerStationsModel.setSelectedItem(null);
		App.gui.stationsTab.playerComboBox.removeAllItems();
	}
		
}
