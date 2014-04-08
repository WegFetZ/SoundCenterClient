package com.soundcenter.soundcenter.client.network.tcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.tcp.MidiNotificationPacket;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;
import com.soundcenter.soundcenter.lib.tcp.TcpPacket;
import com.soundcenter.soundcenter.lib.udp.UdpOpcodes;
import com.soundcenter.soundcenter.lib.util.FileOperation;

public class TcpProtocol {
	
	private static FileOutputStream transferredFileStream = null;

	public static boolean processPacket(TcpPacket packet) {

		byte cmd = packet.getType();

		// AppletStarter.logger.d("Received Tcp-Message: Type: " + cmd + " Key: " + packet.getKey() + " Value: " + packet.getValue(), null);

		if (!Client.connectionAccepted) { /* server hasn't accepted the connection yet, do the handshaking */

			if (cmd == TcpOpcodes.CL_CON_REQ_VERSION) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CON_INFO_VERSION, AppletStarter.version, null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_REQ_NAME) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CON_INFO_NAME, AppletStarter.gui.controller.getName(), null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_INFO_ACCEPTED) {
				Client.id = (Short) packet.getKey();
				Client.connectionAccepted = true;
				AppletStarter.gui.controller.setConnectionStatus("Connection accepted");

				// send udp packet to say hello to the udp server
				byte[] data = new byte[1];
				Client.udpClient.sendData(data, UdpOpcodes.TYPE_GREET_PACKET);

				return true;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_USER_CAP) {
				AppletStarter.logger.w("Connection refused: Server is full.", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_VERSION) {
				AppletStarter.logger.w("Connection refused: Incompatible version.\n" + " Required versions: "
						+ (Double) packet.getKey() + " - " + packet.getValue(), null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_NAME) {
				AppletStarter.logger.w("Connection refused: Invalid name.", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_ALREADY_CONNECTED) {
				AppletStarter.logger.w("Connection refused: User already connected.", null);
				return false;

			}

		} else if (!Client.initialized) {

			if (cmd == TcpOpcodes.CL_CON_INFO_NOT_ONLINE) {
				AppletStarter.logger.i("Connection accepted! \nJoin the server within 2minutes or use "
						+ "\"/sc init\" to initialize the client.", null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_INFO_INITIALIZED) {
				Client.initialized = true;
				Client.reconnectTries = 0;
				AppletStarter.logger.i("SoundCenter initialized!", null);
				AppletStarter.gui.controller.setConnectionStatus("Initialized");
				Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_REQ_INFODATA, null, null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_PERMISSION_INIT) {
				AppletStarter.logger.w("Initialization failed! \nMissing permission: sc.init", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_IP) {
				AppletStarter.logger.w("Initialization failed! \nIP-Verification was not successfull.\n"
						+ "Is SoundCenter runnng on the same computer as your Minecraft client?", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_INIT_TIMEOUT) {
				AppletStarter.logger.w("Initialization failed! \nYou must login within 2 minutes.", null);
				return false;
			}

		} else {

			if (isInGroup(cmd, TcpOpcodes.CL_GROUP_INFODATA, TcpOpcodes.CL_GROUP_END_INFODATA)) {

				if (cmd == TcpOpcodes.CL_INFODATA_START) {
					AppletStarter.gui.controller.setLoading(true);
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_END) {
					AppletStarter.gui.controller.chooseOwnPlayer();
					AppletStarter.gui.controller.setLoading(false);
					Client.database.deleteOldSongs();
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_AVAILABLE_WORLD) {
					Client.database.addAvailableWorld((String) packet.getKey());
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_AVAILABLE_BIOME) {
					Client.database.addAvailableBiome((String) packet.getKey());
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_SONG) {
					Song song = (Song) packet.getKey();

					File file = new File(AppletStarter.dataFolder + "musicdata" + File.separator + song.getPath());

					// check if file is midi and request download if it isn't
					// already existant
					if (FileOperation.getExtension(song.getTitle()).equalsIgnoreCase("midi")
							|| FileOperation.getExtension(song.getTitle()).equalsIgnoreCase("mid")) {
						if (!file.exists() || (file.exists() && file.length() != song.getSize())) {
							Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_REQ_SONG, song, null);
							return true;
						}
					}

					Client.database.addSong(song);
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_PERMISSION) {
					Client.database.addPermission((String) packet.getKey());
					return true;

				}

			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_DATA, TcpOpcodes.CL_GROUP_END_DATA)) {

				if (cmd == TcpOpcodes.CL_DATA_STATION) {
					Station station = (Station) packet.getKey();
					AppletStarter.audioManager.stopPlayer(station.getType(), station.getId());
					Client.database.addStation(station);
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_SONG_CHUNK) {
					byte[] chunk = (byte[]) packet.getKey();
					try {
						if (transferredFileStream != null) {
							transferredFileStream.write(chunk);
						}
					} catch(IOException e) {
						AppletStarter.logger.d("Error while writing a chunk of songdata.", e);
					}
					
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_CMD_DELETE_STATION) {
					byte type = (Byte) packet.getKey();
					short id = (Short) packet.getValue();

					AppletStarter.audioManager.stopPlayer(type, id);
					Client.database.removeStation(type, id, true);
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_CMD_DELETE_SONG) {
					Song song = (Song) packet.getKey();
					Client.database.removeSong(song.getPath(), true);
					File file = new File(AppletStarter.dataFolder + "musicdata" + File.separator + song.getPath());
					if (file.exists()) {
						file.delete();
					}
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_SONG_TRANSFER_START) {
					Song song = (Song) packet.getKey();
					
					AppletStarter.logger.i("Downloading song: " + song.getTitle(), null);
					try {
						File file = new File(AppletStarter.dataFolder + "musicdata" + File.separator + song.getPath());
						if (file.exists()) {
							file.delete();
						} else {
							file.getParentFile().mkdirs();
						}
						file.createNewFile();
						
						transferredFileStream = new FileOutputStream(file);
					} catch (IOException e) {
						AppletStarter.logger.i("Error while creating the FileOutputStream for midi transfer.", e);
					}
					return true;
					
				} else if (cmd == TcpOpcodes.CL_DATA_SONG_TRANSFER_END) {
					Song song = (Song) packet.getKey();
					Client.database.addSong(song);
					
					AppletStarter.logger.i("Download of " + song.getPath() + " finished.", null);
					try {
						if (transferredFileStream != null) {
							transferredFileStream.close();
						}
					} catch (IOException e) {
							AppletStarter.logger.i("Error while closing the FileOutputStream for midi transfer.", e);
					}
					return true;
					
				} else if (cmd == TcpOpcodes.CL_DATA_SONG_UPLOAD_START) {
					Client.tcpClient.approveUpload();
					return true;
					
				} else if (cmd == TcpOpcodes.CL_DATA_SONG_UPLOAD_DONE) {
					Client.tcpClient.uploadDone();
					return true;
				}

			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_CMD, TcpOpcodes.CL_GROUP_END_CMD)) {

				if (cmd == TcpOpcodes.CL_CMD_CHANGE_VOLUME) {
					byte value = (Byte) packet.getKey();
					AppletStarter.gui.controller.setMasterVolume(value, true);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_MUTE_MUSIC) {
					AppletStarter.audioManager.setMusicActive(false);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_UNMUTE_MUSIC) {
					AppletStarter.audioManager.setMusicActive(true);
					return true;
					
				} else if (cmd == TcpOpcodes.CL_CMD_MUTE_VOICE) {
					AppletStarter.audioManager.setVoiceActive(false);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_UNMUTE_VOICE) {
					AppletStarter.audioManager.setVoiceActive(true);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_START_RECORDING) {
					AppletStarter.audioManager.recorder.start();
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_STOP_RECORDING) {
					AppletStarter.audioManager.recorder.stop();
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_PLAY_MIDI) {
					MidiNotificationPacket notification = (MidiNotificationPacket) packet.getKey();
					
					AppletStarter.audioManager.playMidi(notification, true);
					return true;
				
				} else if (cmd == TcpOpcodes.CL_CMD_STOP_GLOBAL) {
					AppletStarter.audioManager.stopPlayer(GlobalConstants.TYPE_GLOBAL, (short) 1);
					return true;
					
				}

			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_ERR, TcpOpcodes.CL_GROUP_END_ERR)) {

				if (cmd == TcpOpcodes.CL_ERR_UPLOAD_PERMISSION) {
					AppletStarter.logger.i("Upload failed. \n" + "Missing permission: sc.upload." + (String) packet.getKey(),
							null);
					Client.tcpClient.skipUpload();
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_UPLOAD_MAX_STORAGE) {
					AppletStarter.logger.i("Upload failed: \nYou have only " + (Long) packet.getKey()
							+ "KB storage remaining.", null);
					Client.tcpClient.skipUpload();
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_FILE_NOT_EXISTANT) {
					AppletStarter.logger.i("Error: Requested file " + (String) packet.getKey() + "does not exist.", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_STREAM_SERVER_LOAD) {
					AppletStarter.logger.i("Cannot start new stream: Server is out of streaming-capacity.", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_STREAM_NO_SONGS) {
					String station = "station";
					switch ((Byte) packet.getKey()) {
					case GlobalConstants.TYPE_AREA:
						station = "area";
						break;
					case GlobalConstants.TYPE_BOX:
						station = "box";
						break;
					case GlobalConstants.TYPE_BIOME:
						station = "biome";
						break;
					case GlobalConstants.TYPE_WORLD:
						station = "world";
					}
					AppletStarter.logger.i("Cannot start stream for " + station + " ID: " + (Short) packet.getValue()
							+ ":\n Songs not found on the server.", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_PLAY_GLOBAL_PERMISSION) {
					AppletStarter.logger.w("Failed to play song globally. \nMissing permission: sc.play.global", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_OTHERS_EDIT_PERMISSION) {
					AppletStarter.logger
							.w("Failed to edit other player's station. \nMissing permission: sc.others.edit", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_EDIT_RANGE) {
					AppletStarter.logger.w("Could not edit box: Maximum range is" + (Integer) packet.getKey(), null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_OTHERS_DELETE_PERMISSION) {
					AppletStarter.logger.w("Failed to delete other player's station. \nMissing permission: sc.others.delete",
							null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_CREATE_PERMISSION) {
					AppletStarter.logger
							.w("Failed to create station. \nMissing permission: " + (String) packet.getKey(), null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_ALREADY_EXISTS) {
					AppletStarter.logger.w("Failed to create station: \nStation with same ID already exists.", null);
					return true;

				}
			}

		}

		if (cmd == TcpOpcodes.CL_CON_DENY_PROTOCOL) {
			AppletStarter.logger.w("Client is not following the protocol.", null);
			return false;

		} else if (cmd == TcpOpcodes.CL_CON_INFO_DISCONNECT) {
			Client.quitReason = "Server closed the connection.";
			return false;

		} else if (cmd == TcpOpcodes.CL_ERR_NOT_INITIALIZED) {
			AppletStarter.logger.w("Client is not initialized.", null);
			return false;

		} else if (cmd == TcpOpcodes.CL_ERR_UNKNOWN) {
			if (packet.getKey() != null) {
				String msg = (String) packet.getKey();
				AppletStarter.logger.i("Could not " + msg + ", due to an unkown error.", null);
			} else {
				AppletStarter.logger.w("An unknown error has occured.", null);
			}
			return true;
		}

		return true;
	}

	private static boolean isInGroup(byte cmd, byte start, byte end) {
		return (cmd >= start && cmd <= end);
	}
}
