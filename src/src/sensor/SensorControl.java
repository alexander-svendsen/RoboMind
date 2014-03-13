package src.sensor;

import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.SensorMode;

import java.util.Arrays;

/**
 * Categorize the different sensors and will give the correct sensor if only a analog is discovered
 * Can discover dynamically the sensor type
 * Can set the specific type as well, altough an error would be returned if a mistake
 */
public class SensorControl {
    //Not really a fan of hard-coded values, but EV3 always has 4 sensors ports anyway
    SensorDiscovery sensorDiscovery = new SensorDiscovery();

    BaseSensor[] sensorArray = new BaseSensor[4];
    SensorMode[] sensorMode = new SensorMode[4];
    float[][] sampleProvider = new float[4][];  //Gives us a reusable sample provider, spare some computation power

    public String getSensorAtPort(int portNumber){
        return sensorDiscovery.getSensorClassName(portNumber);
    }

    public String getAllConnections(){
        return "";
    }

    public boolean openSensorByNameOnPort(String sensorName, int portNumber){
        BaseSensor temp = (BaseSensor)sensorDiscovery.constructSensorObject(sensorName, portNumber);
        if (temp !=null){
            sensorArray[portNumber] = temp;
            setSensorModes(portNumber, 0);
            return true;
        }
        return false;
    }

    public void setSensorModes(int port, int mode){
        sensorMode[port] = sensorArray[port].getMode(mode);
        sampleProvider[port] = new float[sensorMode[port].sampleSize()];
    }

    public float[] fetchSample(int portNumber){
        sensorMode[portNumber].fetchSample(sampleProvider[portNumber], 0);
        return sampleProvider[portNumber];
    }

    public void reset() {
        for (BaseSensor sensor : sensorArray){
            if (sensor != null){
                sensor.close();
            }
        }
        Arrays.fill(sensorArray, null);
        Arrays.fill(sensorMode, null);
    }

    public void close(int port){
        if (sensorArray[port] != null){
            sensorArray[port].close();
            sensorArray[port] = null;
            sensorMode[port] = null;
        }
    }

    public boolean callMethod(int sensorPort, String methodName) {
        java.lang.reflect.Method method;
        try {
            method = sensorArray[sensorPort].getClass().getMethod(methodName);
            method.invoke(sensorArray[sensorPort]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
