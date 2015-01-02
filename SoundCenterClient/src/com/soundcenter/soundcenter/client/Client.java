package com.soundcenter.soundcenter.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.soundcenter.soundcenter.client.data.Database;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.network.tcp.TcpClient;
import com.soundcenter.soundcenter.client.network.udp.UdpClient;

public class Client {

	public static Database database = new Database();
	public static MainLoop mainLoop = null;
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
			App.logger.d("Cannot start a new client-instance while another is active.", null);
			return;
		}
		
		if (!reconnect && App.gui.generalTab.logArea.getText().length()> 100) {
			App.gui.generalTab.logArea.setText("");
		}			
		
		App.gui.controller.setConnectButtonEnabled(false);
		App.gui.controller.setConnectButtonText("Disconnect");
		App.gui.controller.disableConnectionDataFields();
		
		reconnect = false;
		active = true;
		id = 0;
		userName = name;
		connectionAccepted = false;
		initialized = false;
		quitReason = "Unknown";
		
		try {
			InetAddress addr = InetAddress.getByName(ip);
			tcpClient = new TcpClient(addr, port);
			udpClient = new UdpClient(addr, port);
			
			new Thread(tcpClient).start();
			new Thread(udpClient).start();
			mainLoop = new MainLoop();
			new Thread(mainLoop).start();
			
		} catch (UnknownHostException e) {
			App.logger.w("Host unavailable: " + ip + ":", e);
			shutdown();
		}
		
	}
	
	public static void shutdown() {
		
		if (!active) { //shutdown has already been called
			return;
		}
		
		App.gui.controller.setConnectButtonEnabled(false);
		
		exit = true;
		
		if (tcpClient != null) {
			tcpClient.shutdown();
		}
		if (udpClient != null)
			udpClient.shutdown();
		if (mainLoop != null)
			mainLoop.shutdown();
		App.audioManager.stopAll();
		
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
			
		App.gui.controller.setLoading(false);
		active = false;
		userName = "";
		
		App.gui.controller.enableConnectionDataFields();
		App.gui.controller.setConnectButtonText("Connect");
		App.gui.controller.setConnectButtonEnabled(true);
		
		if (!active && App.gui.controller.isAutoReconnectActive() && reconnect) {
			if (reconnectTries < 1) {
				//wait a second before reconnecting
				try { Thread.sleep(1000); } catch(InterruptedException e) {}
				App.logger.i("Reconnecting...", null);
				GeneralTabActions.connectButtonPressed();
				reconnectTries ++;
				
				return;
			}
			App.logger.i("Client will not reconnect", null);
		}
		
	}
}
