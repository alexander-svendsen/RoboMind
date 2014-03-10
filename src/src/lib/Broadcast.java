package src.lib;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Broadcast {

    public static final int port = 9200;

    //TODO: really uneffical usage of sockets..., got a problem with it at a later time
	public static void broadcast(String message) {
		DatagramSocket c = null;
		try {
		  c = new DatagramSocket();
		  c.setBroadcast(true);

		  byte[] sendData = message.getBytes();

		  //Try the 255.255.255.255 first
		  try {
		    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
		    c.send(sendPacket);
		    //System.out.println("Request packet sent to: 255.255.255.255");
		  } catch (Exception e) {
			  System.err.println("Exception send to default: " + e);
		  }

		  // Broadcast the message over all the network interfaces
		  Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		  while (interfaces.hasMoreElements()) {
		    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

		    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
		      continue; // Don't want to broadcast to the loopback interface
		    }

		    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
		      InetAddress broadcast = interfaceAddress.getBroadcast();
		      if (broadcast == null) continue;

		      // Send the broadcast packet.
		      try {
		        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
		        c.send(sendPacket);
		      } catch (Exception e) {
		    	  System.err.println("Exception sending to : " + networkInterface.getDisplayName() + " : "+ e);
		      }

		      //System.out.println("Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
		    }
		  }
		} catch (IOException ex) {
			  System.err.println("Exception opening for broadcast socket : " + ex);

		}
        finally {
            if (c != null){
                c.close();
            }
        }
    }
}
