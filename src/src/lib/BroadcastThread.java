package src.lib;

import lejos.utility.Delay;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Note: Code taken from the leJOS open source git project: EV3Menu
 */
public class BroadcastThread extends Thread {

    static String hostname;

    public BroadcastThread(){
        BroadcastThread.hostname = getHostName();
    }

    @Override
    public synchronized void run()
    {
        while(true) {
            Broadcast.broadcast(BroadcastThread.hostname);
            Delay.msDelay(1000);
        }
    }

    public static String getHostName(){
        try {
            if (hostname != null){
                return hostname;
            }
            InputStream fis = new FileInputStream("/etc/hostname");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            hostname = br.readLine();
            return hostname;
        } catch (FileNotFoundException e) {
            System.err.println("Failed to write to /etc/hostname: " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
