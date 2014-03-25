package src.sensor;

import lejos.hardware.sensor.BaseSensor;
import src.SensorEventListener;


public class SensorControl {
    SensorTracker sensorTracker = new SensorTracker();
    SensorEventListener sensorEventListener;
    SensorDiscovery sensorDiscovery;
    SampleThread sampleThread;

    public SensorControl(SensorEventListener sensorEventListener){
        this.sensorEventListener = sensorEventListener;
        this.sensorDiscovery = new SensorDiscovery(sensorTracker, sensorEventListener);
        this.sampleThread = new SampleThread(sensorTracker, sensorEventListener);
    }

    public String dynamicallyDiscoverSensorNameAtPort(int portNumber){
        return sensorDiscovery.getSensorClassName(portNumber);
    }

    public boolean openSensorByNameOnPort(String sensorName, int portNumber){
        synchronized (sensorTracker.openSensorLock){
            BaseSensor temp = (BaseSensor)sensorDiscovery.constructSensorObject(sensorName, portNumber);
            if (temp !=null){
                sensorTracker.setSensorAtPort(portNumber, temp);
                setSensorModes(portNumber, 0);
                return true;
            }
            return false;
        }
    }

    public void setSensorModes(int port, int mode){
        sensorTracker.sensorMode[port] = sensorTracker.getSensorAtPort(port).getMode(mode);
        sensorTracker.sampleProvider[port] =  new float[sensorTracker.sensorMode[port].sampleSize()];
    }

    public float[] fetchSample(int portNumber){
        sensorTracker.sensorMode[portNumber].fetchSample(sensorTracker.sampleProvider[portNumber],0);
        return sensorTracker.sampleProvider[portNumber];
    }

    public void reset() {
        if (sensorDiscovery.isAlive()){
            sensorDiscovery.exit();
            try {
                sensorDiscovery.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sensorDiscovery = new SensorDiscovery(sensorTracker, sensorEventListener);
        }
        if (sampleThread.isAlive()){
            sampleThread.exit();
            try {
                sampleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sampleThread = new SampleThread(sensorTracker, sensorEventListener);
        }
        sensorTracker.reset();
    }

    public void close(int port){
        sensorTracker.closeSensorAtPort(port);
    }

    public boolean callMethod(int sensorPort, String methodName) {
        java.lang.reflect.Method method;
        try {
            method = sensorTracker.sensorArray[sensorPort].getClass().getMethod(methodName);
            method.invoke(sensorTracker.sensorArray[sensorPort]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void startMonitorThread(){
        sensorDiscovery.start();
    }

    public void startSampleThread() {
        sampleThread.start();
    }
}
