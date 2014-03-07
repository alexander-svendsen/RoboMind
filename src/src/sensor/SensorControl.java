package src.sensor;

import lejos.hardware.port.IOPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
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

    //keeps track of the open ports, mainly used to close the connection when they changes
    private IOPort[] connectedPortsArray = new IOPort[4];

    private Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};

    SensorDiscovery sensorDiscovery = new SensorDiscovery();

    public String getSensorAtPort(int portNumber){
        return sensorDiscovery.getSensorClassName(portNumber);
    }

    public String getAllConnections(){
        return "";
    }

    public boolean openSensorByNameOnPort(String sensorName, int portNumber){
        sensorArray[portNumber] = (BaseSensor)sensorDiscovery.constructSensorObject(sensorName, portNumber);
        connectedPortsArray[portNumber] = (IOPort)port[portNumber];
        return sensorArray[portNumber] == null;
    }


    public void reset() {
        for (BaseSensor sensor : sensorArray){
            if (sensor != null){
                sensor.close();
            }
        }

        for (IOPort port : connectedPortsArray){
            port.close();
        }

        // REVIEW: cleanup the array if needed ?
    }
}
