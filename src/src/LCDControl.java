package src;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.TextLCD;

public class LCDControl {
    protected static GraphicsLCD graphicsLCD = LocalEV3.get().getGraphicsLCD();
    private static TextLCD lcd = LocalEV3.get().getTextLCD();

    public void LCDControl(){
        graphicsLCD.clear();
        int height = graphicsLCD.getHeight();
        int width = graphicsLCD.getWidth();
        int color_pix = 0;
        for (int i = 0; i < height; i ++){
            for (int j = 0; i < width; j++){
                graphicsLCD.setPixel(i, j, color_pix);
                color_pix = color_pix == 0 ? 1 : 0;
            }
        }
    }
}
