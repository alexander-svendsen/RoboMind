package src;

import lejos.hardware.DeviceException;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

import java.util.Arrays;

public class SampleThread extends Thread {

    MonitorSensorsThread monitor;
    float[][] sampleArray;

    private boolean running = true;


    public SampleThread(MonitorSensorsThread monitor){
        this.monitor = monitor;
        this.sampleArray = new float[monitor.getSensorArrayLength()][];
    }

    //TODO minmize the object creation
    public void startSampling(){

        while(running) {
            for(int i = 0; i < monitor.getSensorArrayLength(); i++) {

                // start sampling
                if (monitor.getSensorArray(i) != null) {
                    try {
                        SensorModes sensorModes = (SensorModes) monitor.getSensorArray(i);
                        SampleProvider sampleProvider = sensorModes.getMode(0); //Default mode in all cases

                        if (sampleArray[i] == null){
                            sampleArray[i] = new float[sampleProvider.sampleSize()];
                        }
                        sampleProvider.fetchSample(sampleArray[i], 0);
                        System.out.println(Arrays.deepToString(sampleArray));
                    }catch (DeviceException e){
                        //Sensor left, but we don't really care, ignore it
                    }
                }
                else{
                    sampleArray[i] = null;
                }
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
