package src;

import lejos.hardware.DeviceException;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

//TODO look into if we should block this thread until there is sensor data available, may minimize cpu load
public class SampleThread extends Thread {

    MonitorSensorsThread monitor;
    float[][] sampleArray;
    private SensorEventListener sensorEventListener;
    private boolean running = true;
    private final Object lock = new Object();


    public SampleThread(MonitorSensorsThread monitor){
        this.monitor = monitor;
        this.sampleArray = new float[monitor.getSensorArrayLength()][];
    }

    public void setListener(SensorEventListener lst) {
        this.sensorEventListener = lst;
    }

    //TODO minmize the object creation
    public void startSampling(){
        int length = monitor.getSensorArrayLength();

            while(running) {

                synchronized (monitor.noSensorLock){
                    if(monitor.getNumberOfConnectedSensors() == 0){
                        try {
                            monitor.noSensorLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                for(int i = 0; i < length; i++) {

                    // start sampling
                    if (monitor.getSensorArray(i) != null) {
                        try {
                            SensorModes sensorModes = (SensorModes) monitor.getSensorArray(i);
                            SampleProvider sampleProvider = sensorModes.getMode(0); //Default mode in all cases

                            if (sampleArray[i] == null){
                                sampleArray[i] = new float[sampleProvider.sampleSize()];
                            }
                            sampleProvider.fetchSample(sampleArray[i], 0);
                            sensorEventListener.fetchedSamples(sampleArray);
    //                        System.out.println(Arrays.deepToString(sampleArray));
                        }catch (DeviceException e){
                            //Sensor left, but we don't really care, ignore it
                        }
                    }
                    else{
                        sampleArray[i] = null;
                    }
                }
                synchronized (lock){
                    try {
                        sleep(5); // may not need, but look into it, should be wait actually
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
