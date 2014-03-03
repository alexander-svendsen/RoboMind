package src;

import lejos.hardware.port.IOPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import src.sensor.SensorDiscovery;

import java.util.HashMap;

/**
 * Categorize the different sensors and will give the correct sensor if only a analog is discovered
 * Can discover dynamicly the sensor type
 * Can set the specific type as well, altough an error would be returned if a mistake
 */
public class SensorControl {
    private HashMap<String,String> sensorClasses = new HashMap<String,String>();

    //Not really a fan of hard-coded values, but EV3 always has 4 sensors ports anyway
    private Object[] sensorArray = new Object[4];

    //keeps track of the open ports, mainly used to close the connection when they changes
    private IOPort[] connectedPortsArray = new IOPort[4];

    private Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};

    SensorDiscovery sensorDiscovery;


    public SensorControl(){
        sensorDiscovery = new SensorDiscovery();
        getGyroSensor(3);
    }


    public String getTouchSensor(int portNumber){

        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        if (sensorClass.equals("AnalogSensor")){
            sensorDiscovery.constructSensorObject("NXTTouchSensor", portNumber);
        }
        else if (sensorClass.equals("EV3TouchSensor")){
            sensorDiscovery.constructSensorObject("EV3TouchSensor", portNumber);
        }
        return sensorClass;
    }

    public String getGyroSensor(int portNumber){
        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        if (sensorClass.equals("AnalogSensor")){
            sensorDiscovery.constructSensorObject("HiTechnicGyro", portNumber);
        }
        else if (sensorClass.equals("EV3GyroSensor")){
            sensorDiscovery.constructSensorObject("EV3GyroSensor", portNumber);
        }
        return sensorClass;
    }

    public String getUltraSonicSensor(int portNumber){
        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        return sensorClass;

    }

    public String getSoundSensor(int portNumber){
        //Only analog
        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        return sensorClass;

    }

    public String getLightSensor(int portNumber){
        //Only analog
        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        return sensorClass;

    }

    public String getColorSensor(int portNumber){
        //two
        String sensorClass = sensorDiscovery.getSensorClassName(portNumber);
        System.out.println(sensorClass);

        return sensorClass;

    }



}
