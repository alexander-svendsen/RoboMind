package src;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Note:
 *  - Thread or not ?
 *  - Don't think i need more then one open socket connection
 */
public class Communication {
    private ServerSocket serverSocket;
    private Socket client;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;


    public Communication(){
        try{
            serverSocket = new ServerSocket(9200);
            System.out.println(getIPAddresses());  // may need it later on the lcd

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //TODO: may not need, move upwords
    public void setUpConnection(){
        try{
            client = serverSocket.accept();
            InputStream inputStream = client.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            printWriter = new PrintWriter(client.getOutputStream());
        }catch(IOException e){
            e.printStackTrace(); //ignore
        }
    }

    //Reads a single line of string data
    public String recive(){
        try{
            return bufferedReader.readLine();
        }catch (IOException e){
            return "";
        }
    }

    public void send(String data){
        printWriter.write(data);
        printWriter.flush();
    }

    public void close(){
        try{
            client.close();
        }catch (IOException e){
            e.printStackTrace(); //ignore
        }
    }

    //The same code used in the menu of Lejos to find the different ip-addresses
    public static List<String> getIPAddresses()
    {
        List<String> result = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaces;
        try
        {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e)
        {
            System.err.println("Failed to get network interfaces: " + e);
            return null;
        }
        while (interfaces.hasMoreElements()){
            NetworkInterface current = interfaces.nextElement();
            try
            {
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
            } catch (SocketException e)
            {
                System.err.println("Failed to get network properties: " + e);
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress() || current_addr.isLinkLocalAddress())
                    continue;
                result.add(current_addr.getHostAddress());
            }
        }
        return result;
    }
}
