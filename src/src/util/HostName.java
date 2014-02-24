package src.util;

import java.io.*;
import java.nio.charset.Charset;

public class HostName {

    public static String getHostName(){
        try {
            InputStream fis = new FileInputStream("/etc/hostname");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            return br.readLine();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to write to /etc/hostname: " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
