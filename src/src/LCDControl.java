package src;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.TextLCD;

import java.util.List;

public class LCDControl {
    protected static GraphicsLCD graphicsLCD = LocalEV3.get().getGraphicsLCD();
    private static TextLCD lcd = LocalEV3.get().getTextLCD();

    public static void showIp(List<String> ips){
        graphicsLCD.clear();
        int row = 1;
        for(String ip: ips) {
            lcd.drawString(ip,8 - ip.length()/2,row++);
        }
    }
}
