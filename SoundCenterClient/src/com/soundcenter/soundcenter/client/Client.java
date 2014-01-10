package com.soundcenter.soundcenter.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.soundcenter.soundcenter.client.data.Database;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.network.tcp.TcpClient;
import com.soundcenter.soundcenter.client.network.udp.UdpClient;

public class Client {

	public static Database database = new Database();
	public static StatusUpdater statusUpdater = null;
	public static TcpClient tcpClient = null;
	public static UdpClient udpClient = null;
	
	public static short id = 0;
	public static String userName = "";
	public static boolean connectionAccepted = false;
	public static boolean initialized = false;
	public static String quitReason = "Unknown";
	public static boolean exit = false;
	public static boolean active = false;
	public static boolean reconnect = false;
	public static int reconnectTries = 0;
	
	
	public static void start(String ip, int port, String name) {
		if (active) {
			Applet.logger.d("Cannot start a new client-instance while another is active.", null);
			return;
		}
		
		Applet.logger.lineBreak(2);
		
		Applet.gui.controller.setConnectButtonEnabled(false);
		Applet.gui.controller.setConnectButtonText("Disconnect");
		Applet.gui.controller.disableConnectionDataFields();
		Applet.gui.controller.setConnectionStatus("Connecting...");
		
		reconnect = false;
		active = true;
		id = 0;
		userName = name;
		connectionAccepted = false;
		initialized = false;
		quitReason = "Unknown";
		
		try {
			InetAddress addr = InetAddress.getByName(ip);
			udpClient = new UdpClient(addr, port);
			tcpClient = new TcpClient(addr, port);
			
			new Thread(udpClient).start();
			statusUpdater = new StatusUpdater();
			new Thread(statusUpdater).start();
			new Thread(tcpClient).start();
			
		} catch (UnknownHostException e) {
			Applet.logger.w("Host unavailable: " + ip + ":", e);
			shutdown();
		}
		
	}
	
	public static void shutdown() {
		
		if (!active) { //shutdown has already been called
			return;
		}
		
		Applet.gui.controller.setConnectButtonEnabled(false);
		Applet.gui.controller.setConnectionStatus("Disconnecting...");
		
		exit = true;
		
		if (tcpClient != null) {
			tcpClient.shutdown();
		}
		if (udpClient != null)
			udpClient.shutdown();
		if (statusUpdater != null)
			statusUpdater.shutdown();
		Applet.audioManager.stopAll();
		
		database.reset();
		
		if (tcpClient != null) {
			while(tcpClient.isActive()) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e){}
			}
		}
		if (udpClient != null) {
			while(udpClient.isActive()) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e){}
			}
		}
			
		Applet.gui.controller.setLoading(false);
		active = false;
		userName = "";
		
		Applet.gui.controller.enableConnectionDataFields();
		Applet.gui.controller.setConnectionStatus("Disconnected");
		Applet.gui.controller.setConnectButtonText("Connect");
		Applet.gui.controller.setConnectButtonEnabled(true);
		
		if (!active && Applet.gui.controller.isAutoReconnectActive() && reconnect) {
			if (reconnectTries < 1) {
				Applet.logger.i("Reconnecting...", null);
				GeneralTabActions.connectButtonPressed();
				reconnectTries ++;
			}
		}
	}
}
