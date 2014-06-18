package src.sensor;

import lejos.hardware.port.Port;
import lejos.hardware.port.UARTPort;
import lejos.hardware.sensor.EV3SensorConstants;
import lejos.hardware.sensor.I2CSensor;
import src.SensorEventListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Thread for dynamic discovering what sensors are connected to which port
 * Solution is based on code taken from the leJOS source git project: EV3SensorMonitor.
 * It has been redesigned to be used in this project.
 */
public class SensorDiscovery extends Thread{

    private HashMap<String,String> mapFromModeNameTosensorClasses = new HashMap<String,String>();

    int sensorType;
    UARTPort uartPort;
    I2CSensor i2CSensor;
    String modeName;
    SensorTracker sensorTracker;
    private boolean running;
    SensorEventListener sensorEventListener;

    public SensorDiscovery(SensorTracker sensorTracker, SensorEventListener sensorEventListener){
        /*
    	 * Note: For now only support the sensors we possess, left as future work to extend this.
    	 */
        mapFromModeNameTosensorClasses.put("IR-PROX", "EV3IRSensor");
        mapFromModeNameTosensorClasses.put("COL-REFLECT", "EV3ColorSensor");
        mapFromModeNameTosensorClasses.put("GYRO-ANG", "EV3GyroSensor");
        mapFromModeNameTosensorClasses.put("US-DIST-CM", "EV3UltrasonicSensor");
        mapFromModeNameTosensorClasses.put("LEGOSonar", "NXTUltrasonicSensor");
        mapFromModeNameTosensorClasses.put("HiTechncColor   ", "HiTechnicColorSensor");
        mapFromModeNameTosensorClasses.put("HITECHNCAccel.  ", "HiTechnicAccelerometer");
        mapFromModeNameTosensorClasses.put("HiTechncCompass ", "HiTechnicCompass");
        mapFromModeNameTosensorClasses.put("HiTechncIR Dir. ", "HiTechnicIRSeeker");
        this.sensorTracker = sensorTracker;
        this.sensorEventListener = sensorEventListener;
    }

    public void sendNewConnectedSensors(){
        String className;
        for(int i = 0; i < SensorTracker.port.length; i++) {
            synchronized (sensorTracker.openSensorLock){
                sensorType =  SensorTracker.port[i].getPortType();
                if (sensorTracker.currentSensorTypeArray[i] != sensorType) {
                    System.out.println("Port " + i + " changed to " + sensorType);
                    sensorTracker.currentSensorTypeArray[i] = sensorType;
                    className = getSensorClassName(i, sensorType);
                    sensorEventListener.newSensor(className, i);
                }
            }
        }
    }

    public String getSensorClassName(int portNumber, int sensorType){
        switch (sensorType){

            case(EV3SensorConstants.CONN_INPUT_UART):
                uartPort = SensorTracker.port[portNumber].open(UARTPort.class);
                uartPort.initialiseSensor(0); // Don't get any info from the sensor if we don't initialize it first
                //Need to trim the modeName, since it sometimes adds trailing whitespace. Seems like a bug.
                modeName = uartPort.getModeName(0).trim();
                System.out.println(uartPort.getModeName(0).trim());
                uartPort.close();
                return mapFromModeNameTosensorClasses.get(modeName);

            case(EV3SensorConstants.CONN_NXT_IIC):
                i2CSensor = new I2CSensor(SensorTracker.port[portNumber]);
                String product = i2CSensor.getProductID();
                String vendor = i2CSensor.getVendorID();
                i2CSensor.close();

                //There is also lots of whitespaces here, but ignore it for now. Save resources
                return mapFromModeNameTosensorClasses.get(vendor + product);

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
        sensorType =  SensorTracker.port[portNumber].getPortType();
        return getSensorClassName(portNumber, sensorType);
    }

    public Object constructSensorObject(String className, int portNumber) {
        if (className != null) {
            Class<?> klass;
            try {
                klass = Class.forName("lejos.hardware.sensor." + className);
                Class<?>[] params = new Class<?>[1];
                params[0] = Port.class;
                Constructor<?> con = klass.getConstructor(params);
                Object[] args = new Object[1];
                args[0] = SensorTracker.port[portNumber];
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

    @Override
    public void run() {
        try {
            running = true;
            while(running){
                sendNewConnectedSensors();
                sleep(5000);
            }
        }
        catch (InterruptedException e) {
            //don't care about interrupts
        }
    }

    public void exit(){
        running = false;
        interrupt();
    }
}
