package src.sensor;

import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3SensorConstants;
import lejos.hardware.sensor.I2CSensor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Class for dynamic discovering what sensors are connected to which port
 */
public class SensorDiscovery{

    private HashMap<String,String> sensorClasses = new HashMap<String,String>();

    //keeps track of the open ports, mainly used to close the connection when they changes
    private IOPort[] connectedPortsArray = new IOPort[4];

    private Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};


    int sensorType;
    UARTPort uartPort;
    I2CSensor i2CSensor;
    String key;
    AnalogPort analogPort;
    String className;
    String modeName;
    Object sensor;


    public SensorDiscovery(){
        /*
    	 * Note: For now only support the sensors we possess, left as future work to extend this.
    	 */
        sensorClasses.put("121", "EV3TouchSensor");
        sensorClasses.put("119","AnalogSensor");
        sensorClasses.put("IR-PROX","EV3IRSensor");
        sensorClasses.put("COL-REFLECT","EV3ColorSensor");
        sensorClasses.put("GYRO-ANG", "EV3GyroSensor");
        sensorClasses.put("US-DIST-CM", "EV3UltrasonicSensor");
        sensorClasses.put("LEGOSonar", "NXTUltrasonicSensor");
        sensorClasses.put("HiTechncColor   ","HiTechnicColorSensor");
        sensorClasses.put("HITECHNCAccel.  ","HiTechnicAccelerometer");
        sensorClasses.put("HiTechncCompass ","HiTechnicCompass");
        sensorClasses.put("HiTechncIR Dir. ","HiTechnicIRSeeker");
    }
//    //TODO
//    // NOTE: caching ?
//    public void getAllConnectedSensors(){
//        for(int i = 0; i < port.length; i++) {
//            sensorType =  port[i].getPortType();
//            if (currentSensorTypeArray[i] != sensorType) {
//                System.out.println("Port " + i + " changed to " + sensorType);
//                currentSensorTypeArray[i] = sensorType;
//                disconnectOldSensorAtEntry(i);
//
//            }
//        }
//    }


    public String getSensorClassName(int portNumber, int sensorType){
        switch (sensorType){

            case(EV3SensorConstants.CONN_INPUT_UART):

                uartPort = port[portNumber].open(UARTPort.class);

                uartPort.initialiseSensor(0); // Don't get any info from the sensor if we don't initialize it first

                //Need to trim the modeName, since it sometimes adds trailing whitespace. Seems like a bug.
                modeName = uartPort.getModeName(0).trim();
                System.out.println(uartPort.getModeName(0).trim());
                uartPort.close();
                return sensorClasses.get(modeName);

            case(EV3SensorConstants.CONN_NXT_IIC):
                i2CSensor = new I2CSensor(port[portNumber]);
                String product = i2CSensor.getProductID();
                String vendor = i2CSensor.getVendorID();
                i2CSensor.close();
                System.out.println(vendor + product + "%");

                //There is also lots of whitespaces here, but ignore it for now. Save resources
                return sensorClasses.get(vendor + product);

            case(EV3SensorConstants.CONN_NONE):
                return "None";

            case(EV3SensorConstants.CONN_ERROR):
                return "Error";

            case (EV3SensorConstants.CONN_INPUT_DUMB):
                return "EV3TouchSensor";

            default:
                return "AnalogSensor";
        }
    }

    public String getSensorClassName(int portNumber){
        sensorType =  port[portNumber].getPortType();
        return getSensorClassName(portNumber, sensorType);
    }


    public Object constructSensorObject(String className, int portNumber) {
        if (className != null) {
            Class<?> c;
            try {
                c = Class.forName("lejos.hardware.sensor." + className);
                Class<?>[] params = new Class<?>[1];
                params[0] = Port.class;
                Constructor<?> con = c.getConstructor(params);
                Object[] args = new Object[1];
                args[0] = port[portNumber];
                return con.newInstance(args);
            }
            catch (InvocationTargetException e){
                System.out.println( e.getCause());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
