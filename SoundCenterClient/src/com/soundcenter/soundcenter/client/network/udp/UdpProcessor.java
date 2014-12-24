package com.soundcenter.soundcenter.client.network.udp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.udp.UdpOpcodes;
import com.soundcenter.soundcenter.lib.udp.UdpPacket;

public class UdpProcessor implements Runnable {

	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
	private short serverSequence = Short.MIN_VALUE;
	
	private Thread thread = null;
	private boolean exit = false;
	
	public void run() {
		thread = Thread.currentThread();
		thread.setName("UdpProcessor");
		
		while(!exit) {
			try {
				//get data from the queue and wrap it into an UdpPacket
				UdpPacket udpPacket = new UdpPacket(queue.take());
				//process the packet
				process(udpPacket);
				
			} catch(InterruptedException e) {}
		}
	}
	
	private void process(UdpPacket packet) {
		
		//App.logger.d("Received Udp-Message: IDENT: " + packet.getIdent() + " Sequence: " + packet.getSeq() + " DestUser: " + packet.getDestUserID() + " Type: " + packet.getType(), null);
		
		//return if ident number is wrong
		if (packet.getIdent() != GlobalConstants.UDP_IDENT) {
			return;
		}
		
		//return if destUserID does not equal our id
		if (packet.getDestUserID() != Client.id) {
			return;
		}
		
		//return if sequence number isn't more recent as the last one
		if (!isSequenceMoreRecent(packet.getSeq())) {
			return;
		}
		
		serverSequence = packet.getSeq();
		
		if (packet.getType() == UdpOpcodes.INFO_LOCATION) {		/* location update */
			//AppletStarter.logger.d("Location info: World: " + packet.getLocation().getWorld() + " X:" + packet.getLocation().getX() + " Y:" + packet.getLocation().getY()  + " Z:" + packet.getLocation().getZ(), null);
			Client.mainLoop.setLocation(packet.getLocation());
			
		} else if (isInGroup(packet.getType(), UdpOpcodes.GROUP_STREAM, UdpOpcodes.GROUP_END_STREAM)) {	/* music or voice stream */
			//AppletStarter.logger.d("Player info: Type: " + packet.getType() + " Creator: " + packet.getID() + " Volume: " + packet.getVolume(), null);
			App.audioManager.feedVoicePacket(packet);
			
			//volume for voice gets calculated on the server, so we have to set it here
			if (packet.getType() == UdpOpcodes.TYPE_VOICE) {
				App.audioManager.volumeManager.setPlayerVolume(packet.getType(), packet.getID(), packet.getVolume());
			}
		}				
	}
	
	private boolean isSequenceMoreRecent(short seq) {
		return (seq > serverSequence)
				|| ((serverSequence > seq) && (serverSequence - seq > Short.MAX_VALUE/2));
	}
	
	private boolean isInGroup(byte cmd, byte start, byte end) {
		return (cmd >= start && cmd <= end);
	}
	
	public void queue(byte[] data) {
		queue.add(data);
	}
	
	public void shutdown() {
		exit = true;
		if (thread != null) {
			thread.interrupt();
		}
	}	
}
