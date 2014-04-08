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
		File configFile = new File(AppletStarter.dataFolder + "sc_client.ini");
		properties = new ConfigProperties(configFile);
	}

	public void load() {

		try {
			properties.load();

			// get the values
			AppletStarter.gui.controller.setName(properties.getString("Minecraft-Name", "Herobrine"));
			AppletStarter.gui.controller.setAddress(properties.getString("Minecraft-Server-IP", "127.0.0.1"));
			AppletStarter.gui.controller.setPort(properties.getString("CustomMusic-Plugin-Port", "4224"));
			AppletStarter.gui.controller.setAutoConnectActive(properties.getBoolean("Connect-On-Startup", false));
			AppletStarter.gui.controller.setAutoReconnectActive(properties.getBoolean("Reconnect-On-Disconnect", true));
			AppletStarter.gui.controller.setDebugActive(properties.getBoolean("Debug-Mode", false));
			AppletStarter.gui.controller.setMasterVolume(properties.getInteger("Master-Volume", 100), true);
			AppletStarter.gui.controller.setMusicActive(properties.getBoolean("Music-Active", true));
			AppletStarter.gui.controller.setVoiceActive(properties.getBoolean("Voice-Active", true));

		} catch (Exception e) {
			AppletStarter.logger.w("Could not load properties file:", e);
		}	
		
		loadMutes(Client.database.mutedAreas, new File(AppletStarter.dataFolder + "mutedAreas.txt"));
		loadMutes(Client.database.mutedBoxes, new File(AppletStarter.dataFolder + "mutedBoxes.txt"));
		loadMutes(Client.database.mutedBiomes, new File(AppletStarter.dataFolder + "mutedBiomes.txt"));
		loadMutes(Client.database.mutedWorlds, new File(AppletStarter.dataFolder + "mutedWorlds.txt"));
		
		AppletStarter.logger.i("Configuration loaded.", null);
		if (AppletStarter.gui.controller.isDebugActive())
			AppletStarter.logger.i("Debug-mode enabled.", null);
		
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
			AppletStarter.logger.i("Error while loading muteList:" , e);
		} catch(NumberFormatException e) {
			AppletStarter.logger.d("Error while parsing short from muteList:" , e);
		}
	}

	public void save() {

		// put the information into the properties list
		// general tab
		properties.addString("Minecraft-Name", AppletStarter.gui.controller.getName());
		properties.addString("Minecraft-Server-IP", AppletStarter.gui.controller.getAddress());
		properties.addString("CustomMusic-Plugin-Port", AppletStarter.gui.controller.getPort());
		properties.addBoolean("Connect-On-Startup", AppletStarter.gui.controller.isAutoConnectActive());
		properties.addBoolean("Reconnect-On-Disconnect", AppletStarter.gui.controller.isAutoReconnectActive());
		properties.addBoolean("Debug-Mode", AppletStarter.gui.controller.isDebugActive());
		properties.addInteger("Master-Volume", AppletStarter.gui.controller.getMasterVolume());
		properties.addBoolean("Music-Active", AppletStarter.gui.controller.isMusicActive());
		properties.addBoolean("Voice-Active", AppletStarter.gui.controller.isVoiceActive());

		try {
			properties.save("=== SoundCenter-AudioClient configuration ===");
		} catch (Exception e) {
			AppletStarter.logger.w("[ERROR] Could not save properties file:\n", e);
		}
		
		saveMutes(Client.database.mutedAreas, new File(AppletStarter.dataFolder + "mutedAreas.txt"));
		saveMutes(Client.database.mutedBoxes, new File(AppletStarter.dataFolder + "mutedBoxes.txt"));
		saveMutes(Client.database.mutedBiomes, new File(AppletStarter.dataFolder + "mutedBiomes.txt"));
		saveMutes(Client.database.mutedWorlds, new File(AppletStarter.dataFolder + "mutedWorlds.txt"));
		
		AppletStarter.logger.i("Configuration saved.", null);
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
			AppletStarter.logger.i("Error while writing muteList to file.", e);
		}
	}
}
