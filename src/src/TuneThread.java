package src;

import lejos.hardware.Sound;

public class TuneThread extends Thread {

        @Override
        public void run()
        {
            playTune();
        }

        /**
         * Play the leJOS startup tune.
         */
        static void playTune()
        {
            Sound.setVolume(1);

            //Super mario theme, found http://www.autoitscript.com/forum/topic/40848-beep-music-mario-bros-theme/
            Sound.playTone(1568, 200);
            Sound.playTone(1568, 200);
            Sound.playTone(1568, 200);
            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(370, 200);
            Sound.playTone(392, 200);
            Sound.playTone(370, 200);
            Sound.playTone(392, 200);
            Sound.playTone(392, 400);
            Sound.playTone(250, 400);

            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(740, 200);
            Sound.playTone(250, 200);
            Sound.playTone(880, 200);
            Sound.playTone(830, 200);
            Sound.playTone(880, 200);
            Sound.playTone(988, 400);

            Sound.playTone(880, 200);
            Sound.playTone(784, 200);
            Sound.playTone(698, 200);
            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(784, 200);
            Sound.playTone(740, 200);
            Sound.playTone(784, 200);
            Sound.playTone(880, 200);
            Sound.playTone(830, 200);
            Sound.playTone(880, 200);
            Sound.playTone(988, 200);


        }

}

