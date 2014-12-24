package com.soundcenter.soundcenter.client.network.tcp;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.data.Station;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;
import com.soundcenter.soundcenter.lib.tcp.TcpPacket;
import com.soundcenter.soundcenter.lib.udp.UdpOpcodes;

public class TcpProtocol {

	public static boolean processPacket(TcpPacket packet) {

		byte cmd = packet.getType();

		// AppletStarter.logger.d("Received Tcp-Message: Type: " + cmd + " Key: " + packet.getKey() + " Value: " + packet.getValue(), null);

		if (!Client.connectionAccepted) { /* server hasn't accepted the connection yet, do the handshaking */

			if (cmd == TcpOpcodes.CL_CON_REQ_VERSION) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CON_INFO_VERSION, App.version, null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_REQ_NAME) {
				Client.tcpClient.sendPacket(TcpOpcodes.SV_CON_INFO_NAME, App.gui.controller.getName(), null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_INFO_ACCEPTED) {
				Client.id = (Short) packet.getKey();
				Client.connectionAccepted = true;
				App.gui.controller.setConnectionStatus("Connection accepted");

				// start to send udp heartbeat
				byte[] data = new byte[1];
				Client.udpClient.sendData(data, UdpOpcodes.TYPE_HEARTBEAT);

				return true;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_USER_CAP) {
				App.logger.w("Connection refused: Server is full or has its bandwidth limit reached.", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_VERSION) {
				App.logger.w("Connection refused: Incompatible version.\n" + " Required versions: "
						+ (Double) packet.getKey() + " - " + packet.getValue(), null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_NAME) {
				App.logger.w("Connection refused: Invalid name.", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_ALREADY_CONNECTED) {
				App.logger.w("Connection refused: User already connected.", null);
				return false;

			}

		} else if (!Client.initialized) {

			if (cmd == TcpOpcodes.CL_CON_INFO_NOT_ONLINE) {
				App.logger.i("Connection accepted! \nJoin the server within 2minutes or use "
						+ "\"/sc init\" to initialize the client.", null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_INFO_INITIALIZED) {
				Client.initialized = true;
				Client.reconnectTries = 0;
				App.logger.i("SoundCenter initialized!", null);
				App.gui.controller.setConnectionStatus("Initialized");
				Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_REQ_INFODATA, null, null);
				return true;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_PERMISSION_INIT) {
				App.logger.w("Initialization failed! \nMissing permission: sc.init", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_IP) {
				App.logger.w("Initialization failed! \nIP-Verification was not successfull.\n"
						+ "Is SoundCenter runnng on the same computer as your Minecraft client?", null);
				return false;

			} else if (cmd == TcpOpcodes.CL_CON_DENY_INIT_TIMEOUT) {
				App.logger.w("Initialization failed! \nYou must login within 2 minutes.", null);
				return false;
			}

		} else {

			if (isInGroup(cmd, TcpOpcodes.CL_GROUP_INFODATA, TcpOpcodes.CL_GROUP_END_INFODATA)) {

				if (cmd == TcpOpcodes.CL_INFODATA_START) {
					App.gui.controller.setLoading(true);
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_END) {
					App.gui.controller.chooseOwnPlayer();
					App.gui.controller.setLoading(false);
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_AVAILABLE_WORLD) {
					Client.database.addAvailableWorld((String) packet.getKey());
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_AVAILABLE_BIOME) {
					Client.database.addAvailableBiome((String) packet.getKey());
					return true;

				} else if (cmd == TcpOpcodes.CL_INFODATA_PERMISSION) {
					Client.database.addPermission((String) packet.getKey());
					return true;
				}

			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_DATA, TcpOpcodes.CL_GROUP_END_DATA)) {

				if (cmd == TcpOpcodes.CL_DATA_STATION) {
					Station station = (Station) packet.getKey();
					Client.database.removeStation(station.getType(), station.getId(), false);
					App.audioManager.stopPlayer(station.getType(), station.getId(), true);
					Client.database.addStation(station);
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_SONG) {
					Song song = (Song) packet.getKey();

					Client.database.addSong(song);
					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_CMD_DELETE_STATION) {
					byte type = (Byte) packet.getKey();
					short id = (Short) packet.getValue();

					App.audioManager.stopPlayer(type, id, true);
					Client.database.removeStation(type, id, true);

					return true;

				} else if (cmd == TcpOpcodes.CL_DATA_CMD_DELETE_SONG) {
					Song song = (Song) packet.getKey();
					Client.database.removeSong(song);
					return true;
				}


			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_CMD, TcpOpcodes.CL_GROUP_END_CMD)) {

				if (cmd == TcpOpcodes.CL_CMD_CHANGE_VOLUME) {
					byte value = (Byte) packet.getKey();
					App.gui.controller.setMasterVolume(value, true);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_MUTE_MUSIC) {
					App.audioManager.setMusicActive(false);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_UNMUTE_MUSIC) {
					App.audioManager.setMusicActive(true);
					return true;
					
				} else if (cmd == TcpOpcodes.CL_CMD_MUTE_VOICE) {
					App.audioManager.setVoiceActive(false);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_UNMUTE_VOICE) {
					App.audioManager.setVoiceActive(true);
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_START_RECORDING) {
					App.audioManager.recorder.start();
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_STOP_RECORDING) {
					App.audioManager.recorder.stop();
					return true;

				} else if (cmd == TcpOpcodes.CL_CMD_PLAY_GLOBAL) {
					Song song = (Song) packet.getKey();
					App.audioManager.playGlobal(song);
					return true;
					
				} else if (cmd == TcpOpcodes.CL_CMD_STOP_GLOBAL) {
					App.audioManager.stopPlayer(GlobalConstants.TYPE_GLOBAL, (short) 1, false);
					return true;	
				}

			} else if (isInGroup(cmd, TcpOpcodes.CL_GROUP_ERR, TcpOpcodes.CL_GROUP_END_ERR)) {

				if (cmd == TcpOpcodes.CL_ERR_PLAY_GLOBAL_PERMISSION) {
					App.logger.w("Failed to play song globally. \nMissing permission: sc.play.global", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_OTHERS_EDIT_PERMISSION) {
					App.logger
							.w("Failed to edit other player's station. \nMissing permission: sc.others.edit", null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_EDIT_RANGE) {
					App.logger.w("Could not edit box: Maximum range is" + (Integer) packet.getKey(), null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_OTHERS_DELETE_PERMISSION) {
					App.logger.w("Failed to delete other player's station. \nMissing permission: sc.others.delete",
							null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_CREATE_PERMISSION) {
					App.logger
							.w("Failed to create station. \nMissing permission: " + (String) packet.getKey(), null);
					return true;

				} else if (cmd == TcpOpcodes.CL_ERR_ALREADY_EXISTS) {
					App.logger.w("Failed to create station: \nStation with same ID already exists.", null);
					return true;

				}
			}

		}

		if (cmd == TcpOpcodes.CL_CON_DENY_PROTOCOL) {
			App.logger.w("Client is not following the protocol.", null);
			return false;

		} else if (cmd == TcpOpcodes.CL_CON_INFO_DISCONNECT) {
			Client.quitReason = "Server closed the connection.";
			return false;

		} else if (cmd == TcpOpcodes.CL_ERR_NOT_INITIALIZED) {
			App.logger.w("Client is not initialized.", null);
			return false;

		} else if (cmd == TcpOpcodes.CL_ERR_UNKNOWN) {
			if (packet.getKey() != null) {
				String msg = (String) packet.getKey();
				App.logger.i("Could not " + msg + ", due to an unkown error.", null);
			} else {
				App.logger.w("An unknown error has occured.", null);
			}
			return true;
		}

		return true;
	}

	private static boolean isInGroup(byte cmd, byte start, byte end) {
		return (cmd >= start && cmd <= end);
	}
}
