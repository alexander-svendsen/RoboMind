package src.sensor;

import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.SensorMode;

import java.util.Arrays;

public class SensorTracker {
    static Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};
    BaseSensor[] sensorArray = new BaseSensor[4];
    SensorMode[] sensorMode = new SensorMode[4];

    float[][] sampleProvider = new float[4][];  //Gives us a reusable sample provider, spare some computation power
    int [] currentSensorTypeArray = new int[4];

    final Object openSensorLock = new Object();  //Fixme, should a lock per port
    final Object openLock = new Object();
    public final Object noSensorLock = new Object();

    int numberOfConnectedSensors = 0;

    public BaseSensor getSensorAtPort(int position){
        return sensorArray[position];
    }


    public void setSensorAtPort(int port, BaseSensor baseSensor){
        sensorArray[port] = baseSensor;
        currentSensorTypeArray[port] = SensorTracker.port[port].getPortType();
        numberOfConnectedSensors ++;
        synchronized (noSensorLock){
            noSensorLock.notifyAll();
        }
    }

    public void closeSensorAtPort(int position){
        if (sensorArray[position] != null){
            sensorArray[position].close();

            sensorArray[position] = null;
            sensorMode[position] = null;
        }
        numberOfConnectedSensors --;
    }

    public void reset() {
        for (BaseSensor sensor : sensorArray){
            if (sensor != null){
                sensor.close();
            }
        }
        Arrays.fill(sensorArray, null);
        Arrays.fill(sensorMode, null);
        Arrays.fill(currentSensorTypeArray, 0);
    }
}
