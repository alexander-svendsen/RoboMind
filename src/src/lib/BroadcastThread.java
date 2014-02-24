package src.lib;

import lejos.utility.Delay;

public class BroadcastThread extends Thread {

    String hostname;

    //Fixme what is the hostname?
    public BroadcastThread(String hostname){
        this.hostname = hostname;
    }

    @Override
    public synchronized void run()
    {
        while(true) {
            Broadcast.broadcast(this.hostname);
            Delay.msDelay(1000);
        }
    }
}
