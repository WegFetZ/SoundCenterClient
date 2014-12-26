package com.soundcenter.soundcenter.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.soundcenter.soundcenter.client.util.ConfigProperties;

public class Configuration {
	
	private ConfigProperties properties = null;

	public Configuration() {
		File configFile = new File(App.dataFolder + "sc_client.ini");
		properties = new ConfigProperties(configFile);
	}

	public void load() {

		try {
			properties.load();

			// get the values
			App.gui.controller.setName(properties.getString("Minecraft-Name", "Herobrine"));
			App.gui.controller.setAddress(properties.getString("Minecraft-Server-IP", "127.0.0.1"));
			App.gui.controller.setPort(properties.getString("CustomMusic-Plugin-Port", "4224"));
			App.gui.controller.setAutoConnectActive(properties.getBoolean("Connect-On-Startup", false));
			App.gui.controller.setAutoReconnectActive(properties.getBoolean("Reconnect-On-Disconnect", true));
			App.gui.controller.setDebugActive(properties.getBoolean("Debug-Mode", false));
			App.gui.controller.setMasterVolume((byte) properties.getInteger("Master-Volume", 100), true);
			App.gui.controller.setStationsActive(properties.getBoolean("Music-Active", true));
			App.gui.controller.setVoiceActive(properties.getBoolean("Voice-Active", true));
			App.gui.controller.setSingleSongsActive(properties.getBoolean("SingleSongs-Active", false));

		} catch (Exception e) {
			App.logger.w("Could not load properties file:", e);
		}	
		
		loadMutes(Client.database.mutedAreas, new File(App.dataFolder + "mutedAreas.txt"));
		loadMutes(Client.database.mutedBoxes, new File(App.dataFolder + "mutedBoxes.txt"));
		loadMutes(Client.database.mutedBiomes, new File(App.dataFolder + "mutedBiomes.txt"));
		loadMutes(Client.database.mutedWorlds, new File(App.dataFolder + "mutedWorlds.txt"));
		loadMutes(Client.database.mutedWGRegions, new File(App.dataFolder + "mutedWGRegions.txt"));
		
		App.logger.i("Configuration loaded.", null);
		if (App.gui.controller.isDebugActive())
			App.logger.i("Debug-mode enabled.", null);
		
	}
	
	public void loadMutes(List<Short> list, File file) {
		if (!file.exists()) {
			return;
		}
		
		try {
			FileReader fileIn = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileIn);

			try {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					
					short id = Short.parseShort(line);
					list.add(id);
				}
			} finally {
				reader.close();
			}
		} catch(IOException e) {
			App.logger.i("Error while loading muteList:" , e);
		} catch(NumberFormatException e) {
			App.logger.d("Error while parsing short from muteList:" , e);
		}
	}

	public void save() {

		// put the information into the properties list
		// general tab
		properties.addString("Minecraft-Name", App.gui.controller.getName());
		properties.addString("Minecraft-Server-IP", App.gui.controller.getAddress());
		properties.addString("CustomMusic-Plugin-Port", App.gui.controller.getPort());
		properties.addBoolean("Connect-On-Startup", App.gui.controller.isAutoConnectActive());
		properties.addBoolean("Reconnect-On-Disconnect", App.gui.controller.isAutoReconnectActive());
		properties.addBoolean("Debug-Mode", App.gui.controller.isDebugActive());
		properties.addInteger("Master-Volume", App.gui.controller.getMasterVolume());
		properties.addBoolean("Music-Active", App.gui.controller.areStationsActive());
		properties.addBoolean("Voice-Active", App.gui.controller.isVoiceActive());
		properties.addBoolean("SingleSongs-Active", App.gui.controller.areSingleSongsActive());

		try {
			properties.save("=== SoundCenter-AudioClient configuration ===");
		} catch (Exception e) {
			App.logger.w("[ERROR] Could not save properties file:\n", e);
		}
		
		saveMutes(Client.database.mutedAreas, new File(App.dataFolder + "mutedAreas.txt"));
		saveMutes(Client.database.mutedBoxes, new File(App.dataFolder + "mutedBoxes.txt"));
		saveMutes(Client.database.mutedBiomes, new File(App.dataFolder + "mutedBiomes.txt"));
		saveMutes(Client.database.mutedWorlds, new File(App.dataFolder + "mutedWorlds.txt"));
		saveMutes(Client.database.mutedWGRegions, new File(App.dataFolder + "mutedWGReions.txt"));
		
		App.logger.i("Configuration saved.", null);
	} 
	
	public void saveMutes(List<Short> list, File file) {
		try {
			file.delete();
			file.createNewFile();
				
			FileWriter fileOut = new FileWriter(file);
			PrintWriter writer = new PrintWriter(fileOut);
				
			try {
				for (short id : list) {
					writer.println(id);
				}
			} finally {
				writer.close();
			}
				
		} catch (IOException e) {
			App.logger.i("Error while writing muteList to file.", e);
		}
	}
}
