package src.sensor;

import lejos.hardware.sensor.BaseSensor;
import src.SensorEventListener;

/**
 * Categorize the different sensors and will give the correct sensor if only a analog is discovered
 * Can discover dynamically the sensor type
 * Can set the specific type as well, altough an error would be returned if a mistake
 */
public class SensorControl {
    //Not really a fan of hard-coded values, but EV3 always has 4 sensors ports anyway
    SensorTracker sensorTracker = new SensorTracker();
    SensorEventListener sensorEventListener;
    SensorDiscovery sensorDiscovery;
    public SensorControl(SensorEventListener sensorEventListener){
        this.sensorEventListener = sensorEventListener;
        this.sensorDiscovery = new SensorDiscovery(sensorTracker, sensorEventListener);
    }

    public String getSensorAtPort(int portNumber){
        return sensorDiscovery.getSensorClassName(portNumber);
    }

    public boolean openSensorByNameOnPort(String sensorName, int portNumber){ //Fixme: should detect wheter the type is the same as the one there and simply open it
        synchronized (sensorTracker.openSensorLock){
            if (sensorTracker.sensorArray[portNumber] != null){
                return ((sensorName).equals(sensorTracker.sensorArray[portNumber].getClass().getSimpleName()));
            }

            BaseSensor temp = (BaseSensor)sensorDiscovery.constructSensorObject(sensorName, portNumber);
            if (temp !=null){
                sensorTracker.sensorArray[portNumber] = temp;
                setSensorModes(portNumber, 0);
                sensorTracker.currentSensorTypeArray[portNumber] = SensorTracker.port[portNumber].getPortType();
                return true;
            }
            return false;
        }
    }

    public void setSensorModes(int port, int mode){
        sensorTracker.sensorMode[port] = sensorTracker.sensorArray[port].getMode(mode);
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
        sensorTracker.reset();
    }

    public void close(int port){
        if (sensorTracker.sensorArray[port] != null){
            sensorTracker.sensorArray[port].close();
            sensorTracker.sensorArray[port] = null;
            sensorTracker.sensorMode[port] = null;
        }
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
}
