package com.soundcenter.soundcenter.client.network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.udp.UdpOpcodes;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class UdpClient implements Runnable {
	
	private InetAddress addr;
	private DatagramSocket datagramSocket;
	private int port = 4224;
	private UdpProcessor udpProcessor = new UdpProcessor();
	
	private boolean exit = false;
	private boolean active = false;
	private short sequence = Short.MIN_VALUE;
	
	public UdpClient(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
		
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(10000);
			new Thread(udpProcessor).start();
			
			AppletStarter.logger.i("UDP-Client started.", null);
			
		} catch (SocketException e) {
			AppletStarter.logger.w("Error while creating UDP-Socket. Is another instance of SoundCenter running?:", e);
			exit = true;
		}
	}
	
	public void run() {
		Thread.currentThread().setName("UdpClient");
		
		if (active) {
			AppletStarter.logger.i("Cannot start a new UDP-Client session while another is active.", null);
			return;
		}
		
		active = true;
		
		while(!exit) {
			byte[] data = new byte[GlobalConstants.STREAM_PACKET_SIZE];
			try {
				//receive packet and pass the data to the processing thread
				DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
				datagramSocket.receive(receivedPacket);
				udpProcessor.queue(receivedPacket.getData());
			} catch (SocketTimeoutException e) {
				if (Client.initialized) {
					AppletStarter.logger.w("Client is not receiving UDP-Packets!", null);					
				}
				// send a udp heartbeat packet
				byte[] packetData = new byte[1];
				Client.udpClient.sendData(packetData, UdpOpcodes.TYPE_HEARTBEAT);
			} catch (IOException e) {
				if (!exit) {
					AppletStarter.logger.i("Error while receiving UDP-Packet:", e);
					Client.reconnect = true;
				}
				exit = true;
			}
		}
		
		active = false;
		Client.shutdown();
		AppletStarter.logger.i("UDP-Client was shut down!", null);
	}
	
	public void sendData(byte[] data, byte type) {
		UdpPacket udpPacket = new UdpPacket(UdpPacket.HEADER_SIZE + data.length);
		udpPacket.setIdent(GlobalConstants.UDP_IDENT);
		udpPacket.setSeq(sequence);
		sequence++;
		udpPacket.setID(Client.id);
		udpPacket.setType(type);
		udpPacket.setStreamData(data);
		DatagramPacket packet = new DatagramPacket(udpPacket.getData(), udpPacket.getLength(), addr, port);
		try {
			if (datagramSocket != null) {
				datagramSocket.send(packet);
			}
		} catch (IOException e) {
			AppletStarter.logger.i("Error while sending UDP-Packet:", e);
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void shutdown() {
		exit = true;
		if (datagramSocket != null && !datagramSocket.isClosed()) {
			datagramSocket.close();
			datagramSocket = null;
		}
		udpProcessor.shutdown();
	}
}
