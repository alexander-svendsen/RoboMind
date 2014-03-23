package src.sensor;

import lejos.hardware.DeviceException;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import src.SensorEventListener;

public class SampleThread extends Thread {

    private MonitorSensorsThread monitor;
    private float[][] sampleArray;
    private SensorEventListener sensorEventListener;
    private boolean running = true;
    private final Object lock = new Object();


    private SensorModes sensorModes;
    private SampleProvider sampleProvider;
    private int length;

    public SampleThread(MonitorSensorsThread monitor){
        this.monitor = monitor;
        this.sampleArray = new float[monitor.getSensorArrayLength()][];
    }

    public void setListener(SensorEventListener lst) {
        this.sensorEventListener = lst;
    }


    public void startSampling(){
        length = monitor.getSensorArrayLength();

        while(running) {
            try{
                waitUntilThereAreSensorsConnected();
                getSamples();
                sensorEventListener.newSamples(sampleArray);

                //slow down the sampling, don't need result each millisecond
                synchronized (lock){
                        sleep(5); // may not need, but look into it, should be wait actually
                }

            }catch (InterruptedException e){
                //wait was interrupted, but there is no real problem with that
            }catch (DeviceException e){
                //the sensor we tried to measure on left, so we simply ignore it
            }

        }

    }

    private void waitUntilThereAreSensorsConnected() throws InterruptedException {
        synchronized (monitor.noSensorLock){
            if(monitor.getNumberOfConnectedSensors() == 0){
                monitor.noSensorLock.wait();
            }
        }
    }

    private void getSamples() {
        for(int i = 0; i < length; i++) {
            if (monitor.getSensorArray(i) != null) {
                    sensorModes = (SensorModes) monitor.getSensorArray(i);
                    sampleProvider = sensorModes.getMode(0); // TODO: change ? Default mode in all cases

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
        startSampling();
    }

    public void exit(){
        this.running = false;
    }
}
