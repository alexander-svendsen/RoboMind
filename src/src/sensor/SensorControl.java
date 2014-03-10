package src.sensor;

import lejos.hardware.sensor.BaseSensor;

import java.util.HashMap;

/**
 * Categorize the different sensors and will give the correct sensor if only a analog is discovered
 * Can discover dynamicly the sensor type
 * Can set the specific type as well, altough an error would be returned if a mistake
 */
public class SensorControl {
    private HashMap<String,String> sensorClasses = new HashMap<String,String>();

    //Not really a fan of hard-coded values, but EV3 always has 4 sensors ports anyway
    private BaseSensor[] sensorArray = new BaseSensor[4];
    SensorDiscovery sensorDiscovery = new SensorDiscovery();

    public String getSensorAtPort(int portNumber){
        return sensorDiscovery.getSensorClassName(portNumber);
    }

    public String getAllConnections(){
        return "";
    }

    public boolean openSensorByNameOnPort(String sensorName, int portNumber){
        sensorArray[portNumber] = (BaseSensor)sensorDiscovery.constructSensorObject(sensorName, portNumber);
        return sensorArray[portNumber] == null;
    }


    public void reset() {
        for (BaseSensor sensor : sensorArray){
            if (sensor != null){
                sensor.close();
            }
        }
    }
}
