package src.sensor;

import lejos.hardware.DeviceException;
import lejos.robotics.SampleProvider;
import src.SensorEventListener;

public class SampleThread extends Thread {

    private float[][] sampleArray;

    private boolean running;
    SensorTracker sensorTracker;
    SensorEventListener sensorEventListener;

    public SampleThread(SensorTracker sensorTracker, SensorEventListener sensorEventListener){
        this.sensorTracker = sensorTracker;
        this.sensorEventListener = sensorEventListener;
        this.sampleArray = new float[sensorTracker.sensorArray.length][];
    }

    public void startSampling(){
        try{
            while(running) {
                waitUntilThereAreSensorsConnected();
                getSamples();
                sensorEventListener.newSamples(sampleArray);
                sleep(5); // may not need, but look into it, should be wait actually
            }
        }catch (InterruptedException e){
            //wait was interrupted, but there is no real problem with that
        }catch (DeviceException e){
            //the sensor we tried to measure on left, so we simply ignore it
        }
    }

    private void waitUntilThereAreSensorsConnected() throws InterruptedException {
        synchronized (sensorTracker.noSensorLock){
            if(sensorTracker.numberOfConnectedSensors == 0){
                sensorTracker.noSensorLock.wait();
            }
        }
    }

    private void getSamples() {
        SampleProvider sampleProvider;
        for(int i = 0; i < sensorTracker.sensorArray.length; i++) {
            if (sensorTracker.sensorArray[i] != null) {
                    sampleProvider = sensorTracker.sensorMode[i]; //FIXME: THINK A RACE CONDITION HAPPENS HERE
                    if (sampleArray[i] == null){
                        sampleArray[i] = new float[sampleProvider.sampleSize()];
                    }
                    sampleProvider.fetchSample(sampleArray[i], 0);
            }else{
                sampleArray[i] = null;
            }
        }
    }

    @Override
    public void run() {
        running = true;
        startSampling();
    }

    public void exit(){
        this.running = false;
        interrupt();
    }
}
