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
		File configFile = new File(Applet.dataFolder + "sc_client.ini");
		properties = new ConfigProperties(configFile);
	}

	public void load() {

		try {
			properties.load();

			// get the values
			Applet.gui.controller.setName(properties.getString("Minecraft-Name", "Herobrine"));
			Applet.gui.controller.setAddress(properties.getString("Minecraft-Server-IP", "127.0.0.1"));
			Applet.gui.controller.setPort(properties.getString("CustomMusic-Plugin-Port", "4224"));
			Applet.gui.controller.setAutoConnectActive(properties.getBoolean("Connect-On-Startup", false));
			Applet.gui.controller.setAutoReconnectActive(properties.getBoolean("Reconnect-On-Disconnect", true));
			Applet.gui.controller.setDebugActive(properties.getBoolean("Debug-Mode", false));
			Applet.gui.controller.setMasterVolume(properties.getInteger("Master-Volume", 100), true);
			Applet.gui.controller.setMusicActive(properties.getBoolean("Music-Active", true));
			Applet.gui.controller.setVoiceActive(properties.getBoolean("Voice-Active", true));

		} catch (Exception e) {
			Applet.logger.w("Could not load properties file:", e);
		}	
		
		loadMutes(Client.database.mutedAreas, new File(Applet.dataFolder + "mutedAreas.txt"));
		loadMutes(Client.database.mutedBoxes, new File(Applet.dataFolder + "mutedBoxes.txt"));
		loadMutes(Client.database.mutedBiomes, new File(Applet.dataFolder + "mutedBiomes.txt"));
		loadMutes(Client.database.mutedWorlds, new File(Applet.dataFolder + "mutedWorlds.txt"));
		
		Applet.logger.i("Configuration loaded.", null);
		if (Applet.gui.controller.isDebugActive())
			Applet.logger.i("Debug-mode enabled.", null);
		
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
			Applet.logger.i("Error while loading muteList:" , e);
		} catch(NumberFormatException e) {
			Applet.logger.d("Error while parsing short from muteList:" , e);
		}
	}

	public void save() {

		// put the information into the properties list
		// general tab
		properties.addString("Minecraft-Name", Applet.gui.controller.getName());
		properties.addString("Minecraft-Server-IP", Applet.gui.controller.getAddress());
		properties.addString("CustomMusic-Plugin-Port", Applet.gui.controller.getPort());
		properties.addBoolean("Connect-On-Startup", Applet.gui.controller.isAutoConnectActive());
		properties.addBoolean("Reconnect-On-Disconnect", Applet.gui.controller.isAutoReconnectActive());
		properties.addBoolean("Debug-Mode", Applet.gui.controller.isDebugActive());
		properties.addInteger("Master-Volume", Applet.gui.controller.getMasterVolume());
		properties.addBoolean("Music-Active", Applet.gui.controller.isMusicActive());
		properties.addBoolean("Voice-Active", Applet.gui.controller.isVoiceActive());

		try {
			properties.save("=== SoundCenter-AudioClient configuration ===");
		} catch (Exception e) {
			Applet.logger.w("[ERROR] Could not save properties file:\n", e);
		}
		
		saveMutes(Client.database.mutedAreas, new File(Applet.dataFolder + "mutedAreas.txt"));
		saveMutes(Client.database.mutedBoxes, new File(Applet.dataFolder + "mutedBoxes.txt"));
		saveMutes(Client.database.mutedBiomes, new File(Applet.dataFolder + "mutedBiomes.txt"));
		saveMutes(Client.database.mutedWorlds, new File(Applet.dataFolder + "mutedWorlds.txt"));
		
		Applet.logger.i("Configuration saved.", null);
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
			Applet.logger.i("Error while writing muteList to file.", e);
		}
	}
}
