package src.util;

import java.io.*;
import java.nio.charset.Charset;

public class HostName {

    public static String hostname = null;
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
