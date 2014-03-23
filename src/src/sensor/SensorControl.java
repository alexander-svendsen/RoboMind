package src.sensor;

import lejos.hardware.port.AnalogPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.UARTPort;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3SensorConstants;
import lejos.hardware.sensor.I2CSensor;
import lejos.hardware.sensor.SensorMode;
import src.SensorEventListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

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


    public class MonitorSensorsThread extends Thread {

        private HashMap<String,String> sensorClasses = new HashMap<String,String>();

        private Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};
        private int [] currentSensorTypeArray = new int[port.length];


        //Locks for synchronization
        private final Object lock = new Object();
        public final Object noSensorLock = new Object();

        private boolean running;

        private SensorEventListener sensorEventListener;

        public void setSensorEventListener(SensorEventListener lst) {
            this.sensorEventListener = lst;
        }

        public MonitorSensorsThread(){  //Fixme
        /*
    	 * Note: For now only support the sensors we possess, left as future work to extend this.
    	 */
            sensorClasses.put("121","lejos.hardware.sensor.EV3TouchSensor"); //EV3 Touch sensor
            sensorClasses.put("119","lejos.hardware.sensor.NXTLightSensor");  //NXT analog sensor, default configured to be the light sensor
            sensorClasses.put("IR-PROX","lejos.hardware.sensor.EV3IRSensor");
            sensorClasses.put("COL-REFLECT","lejos.hardware.sensor.EV3ColorSensor");
            sensorClasses.put("GYRO-ANG", "lejos.hardware.sensor.EV3GyroSensor");
            sensorClasses.put("US-DIST-CM", "lejos.hardware.sensor.EV3UltrasonicSensor");
            sensorClasses.put("LEGOSonar", "lejos.hardware.sensor.NXTUltrasonicSensor");
            sensorClasses.put("HiTechncColor   ","lejos.hardware.sensor.HiTechnicColorSensor");
            sensorClasses.put("HITECHNCAccel.  ","lejos.hardware.sensor.HiTechnicAccelerometer");
            sensorClasses.put("HiTechncCompass ","lejos.hardware.sensor.HiTechnicCompass");
            sensorClasses.put("HiTechncIR Dir. ","lejos.hardware.sensor.HiTechnicIRSeeker");

        }

        public Object getSensorArray(int entry){
            return sensorArray[entry];
        }

        private void setSensorArray(int entry, BaseSensor object){
            sensorArray[entry] = object;
        }

        public void removeSensorAtEntry(int entry){
            setSensorArray(entry, null);
        }

        public void addSensorAtEntry(int entry, BaseSensor object){
            setSensorArray(entry, object);
            synchronized (noSensorLock){
                noSensorLock.notifyAll();
            }
        }

        public void disconnectOldSensorAtEntry(int entry){
            sensorArray[entry].close();
            sensorArray[entry] = null;
        }

        public void monitorSensorPorts(){
            int sensorType;
            UARTPort uartPort;
            I2CSensor i2CSensor;
            String key;
            AnalogPort analogPort;
            String className;
            String modeName;
            Object sensor;

            while(running) {
                for(int i = 0; i < port.length; i++) {
                    sensorType =  port[i].getPortType();
                    if (currentSensorTypeArray[i] != sensorType) {
                        System.out.println("Port " + i + " changed to " + sensorType);
                        currentSensorTypeArray[i] = sensorType;
                        disconnectOldSensorAtEntry(i);

                        switch (sensorType){
                        /*
                         * The digital EV3 sensors are discovered here. It seems the most easy to use sensors in the code examples,
                         * remain to be seen
                         */
                            case(EV3SensorConstants.CONN_INPUT_UART):
                                uartPort = port[i].open(UARTPort.class);

                                uartPort.initialiseSensor(0); // Don't get any info from the sensor if we don't initialize it first

                                //Need to trim the modeName, since it sometimes adds trailing whitespace. Seems like a bug.
                                modeName = uartPort.getModeName(0).trim();

                                className = sensorClasses.get(modeName);
                                sensor = constructSensorObject(className, UARTPort.class, uartPort);

                                sensorEventListener.newSensor(sensor, className, i);
                                addSensorAtEntry(i, (BaseSensor)sensor);
                                break;

                        /*
                         * Mainly the sensors from the HiTechn vendor gets here. These sensors has the functionality to
                         * dynamically answering who they are and who made them .
                         *
                         * But to do this they must first be opened and started, which means afterwards they must be stopped
                         * and started in the appropriate class, so it's a more slow process then the previous.
                         */
                            case(EV3SensorConstants.CONN_NXT_IIC):
                                i2CSensor = new I2CSensor(port[i]);
                                String product = i2CSensor.getProductID();
                                String vendor = i2CSensor.getVendorID();
                                i2CSensor.close();


                                //There is also lots of whitespaces here, but ignore it for now. Save resources
                                className = sensorClasses.get(vendor + product);

                                sensor = constructSensorObject(className, Port.class, port[i]);

                                sensorEventListener.newSensor(sensor, className, i);
                                addSensorAtEntry(i, (BaseSensor)sensor);
                                break;

                            case(EV3SensorConstants.CONN_NONE):
                                sensorEventListener.newInfo("no_sensor", i);
                                break;

                            case(EV3SensorConstants.CONN_ERROR):
                                sensorEventListener.newInfo("connection_error", i);
                                break;

                        /*
                         * The remaining sensors which is not an error gets here, basically the analog sensors.
                         * Many of these are indistinguishable as many uses the same underlying protocol. As of now all of these are mapped
                         * to the color sensor, where the users themselves need to re-map these sensors as there is no possibility of dynamically
                         * discovering it in the code.
                         */
                            default:
                                key = Integer.toString(sensorType);
                                analogPort = port[i].open(AnalogPort.class);

                                className = sensorClasses.get(key);
                                sensor = constructSensorObject(className, AnalogPort.class, analogPort);

                                sensorEventListener.newSensor(sensor, className, i);
                                addSensorAtEntry(i,(BaseSensor)sensor);
                                break;
                        }

                    }
                }
                // No need to constantly find new sensors, when most users tend to add them before beginning
                doSleep(5000);
            }
        }



        private Object constructSensorObject(String className, Class<?> paramClass, Object param) {
            if (className != null) {
                Class<?> c;
                try {
                    c = Class.forName(className);
                    Class<?>[] params = new Class<?>[1];
                    params[0] = paramClass;
                    Constructor<?> con = c.getConstructor(params);
                    Object[] args = new Object[1];
                    args[0] = param;
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

        private void doSleep(int milliseconds){
            synchronized (lock){
                try {
                    sleep(milliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            this.running = true;
            monitorSensorPorts();
            System.out.println("IT FINALLY CLOSED");
        }

        public void exit(){
            this.running = false;
        }

    }
}
