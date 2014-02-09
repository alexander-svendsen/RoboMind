package src;

import lejos.hardware.Device;
import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3SensorConstants;
import lejos.hardware.sensor.I2CSensor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MonitorSensorsThread extends Thread {

    private HashMap<String,String> sensorClasses = new HashMap<String,String>();
    private Object[] sensorArray = new Object[4]; //Not really a fan of hard-coded values, but EV3 always has 4 sensors ports anyway
    private IOPort[] livePortArray = new IOPort[4]; //keeps track of the open ports, mainly used to close the connection when they changes
    private Port[] port = {SensorPort.S1, SensorPort.S2, SensorPort.S3, SensorPort.S4};
    private int [] current = new int[port.length];
    private boolean running = true;
    private final Object lock = new Object();

    private int sensorCounter = 0;
    public final Object noSensorLock = new Object();


    private SensorEventListener sensorEventListener;

    public MonitorSensorsThread(){


        /*
    	 * Note: For now only support the sensors we possess, left as future work to extend this.
    	 * The hash table uses the default mode names for the different sensors,
    	 * if another mode is started instead when the sensor connects it will not be found
    	 */
        sensorClasses.put("121","lejos.hardware.sensor.EV3TouchSensor"); //EV3 Touch sensor
        sensorClasses.put("119","lejos.hardware.sensor.NXTLightSensor");  //NXT analog sensor, default configured to be the light sensor
        sensorClasses.put("IR-PROX","lejos.hardware.sensor.EV3IRSensor");
        sensorClasses.put("COL-REFLECT","lejos.hardware.sensor.EV3ColorSensor");
        sensorClasses.put("GYRO-ANG", "lejos.hardware.sensor.EV3GyroSensor");
        sensorClasses.put("US-DIST-CM", "lejos.hardware.sensor.EV3UltrasonicSensor");
        sensorClasses.put("HiTechncColor   ","lejos.hardware.sensor.HiTechnicColorSensor");
        sensorClasses.put("HITECHNCAccel.  ","lejos.hardware.sensor.HiTechnicAccelerometer");
        sensorClasses.put("HiTechncCompass ","lejos.hardware.sensor.HiTechnicCompass");
    }

    public Object getSensorArray(int entry){
        return sensorArray[entry];
    }

    private void setSensorArray(int entry, Object object){
        sensorArray[entry] = object;
    }

    public void removeSensorAtEntry(int entry){
        sensorCounter --;
        setSensorArray(entry, null);
    }

    public void addSensorAtEntry(int entry, Object object){
        sensorCounter ++;
        setSensorArray(entry, object);
        synchronized (noSensorLock){
            noSensorLock.notifyAll();
        }
    }

    public int getNumberOfConnectedSensors(){ return sensorCounter; }

    public int getSensorArrayLength(){
        return sensorArray.length;
    }

    // Monitor the sensor ports
    public void monitorSensorPorts(){
        int typ;
        UARTPort uartPort;
        I2CSensor i2CSensor;
        String key;
        AnalogPort analogPort;
        String className;
        String modeName;
        Object sensor;

        while(running) { //Note may slow it down here, shouldn't change that often really
            for(int i = 0; i < port.length; i++) {
                typ =  port[i].getPortType();
                if (current[i] != typ) {
                    System.out.println("Port " + i + " changed to " + typ);
                    current[i] = typ;
                    
                    if (sensorArray[i] != null){
                        livePortArray[i].close();
                        ((Device) getSensorArray(i)).close();
                        removeSensorAtEntry(i);
                        System.out.println("Removed old entry at port: " + (i + 1));
                    }
                    
                    /**
                     * The digital EV3 sensors are discovered here. It seems the most easy to use sensors in the code examples,
                     * remain to be seen
                     */
                    if (typ == EV3SensorConstants.CONN_INPUT_UART) {
                    	uartPort = port[i].open(UARTPort.class);

                        uartPort.initialiseSensor(0); // Don't get any info from the sensor if we don't initialize it first
                    
                        //Need to trim the modeName, since it sometimes adds trailing whitespace. Seems like a bug.
                        modeName = uartPort.getModeName(0).trim();
                        
                        System.out.println("Uart sensor: " + modeName);
                        className = sensorClasses.get(modeName);
                        System.out.println("Sensor class for " + modeName + " is " + className);

                        sensor = callGetMethods(className, UARTPort.class, uartPort);

                        sensorEventListener.newSensor(sensor, className, i);
                        addSensorAtEntry(i, sensor);
                        livePortArray[i] = uartPort;
                    } 
                    
                    /**
                     * Mainly the sensors from the HiTechn vendor gets here. These sensors has the functionality to
                     * dynamically answering who they are and who made them .
                     * 
                     * But to do this they must first be opened and started, which means afterwards they must be stopped
                     * and started in the appropriate class, so it's a more slow process then the previous.
                     */
                    else if (typ == EV3SensorConstants.CONN_NXT_IIC){
                    	
                        i2CSensor = new I2CSensor(port[i]);
                        String product = i2CSensor.getProductID();
                        String vendor = i2CSensor.getVendorID();
                        i2CSensor.close();

                        System.out.println("I2c sensor: " + vendor + " " + product);
                        
                        //There is also lots of whitespaces here, but ignore it for now. Save resources
                        className = sensorClasses.get(vendor + product);
                        System.out.println("Sensor class for " + vendor + product + " is " + className);

                        sensor = callGetMethods(className, Port.class, port[i]);

                        sensorEventListener.newSensor(sensor, className, i);
                        addSensorAtEntry(i, sensor);
                        livePortArray[i] = i2CSensor.getPort();
                    }

                    else if (typ == EV3SensorConstants.CONN_NONE){
                        sensorEventListener.newInfo("noSensor", i);

                    }

                    else if (typ == EV3SensorConstants.CONN_ERROR){
                        sensorEventListener.newInfo("connectionError", i);
                    }

                    /**
                     * The remaining sensors which is not an error gets here, basically the analog sensors. 
                     * Many of these are indistinguishable as many uses the same underlying protocol. As of now all of these are mapped
                     * to the color sensor, where the users themselves need to re-map these sensors as there is no possibility of dynamically
                     * discovering it in the code.
                     */
                    else{
                    	key = Integer.toString(typ);
                    	analogPort = port[i].open(AnalogPort.class);
                		
                    	className = sensorClasses.get(key);
                		System.out.println("Sensor class is " + className);

                        sensor = callGetMethods(className, AnalogPort.class, analogPort);

                        sensorEventListener.newSensor(sensor, className, i);
                        addSensorAtEntry(i, sensor);
                        livePortArray[i] = analogPort;
                    } 
                }
            }
            synchronized (lock){
            try {
                sleep(5000); // No need to constantly find new sensors, when most users tend to add them before beginning
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    // Construct an instance of the class with a single parameter, and call its parameterless get and is methods
    private Object callGetMethods(String className, Class<?> paramClass, Object param) {
    	if (className != null) {
        	Class<?> c;
        	
        	try {
				c = Class.forName(className);
				Class<?>[] params = new Class<?>[1];
				params[0] = paramClass;
				Constructor<?> con = c.getConstructor(params);
				Object[] args = new Object[1];
				args[0] = param;
				Object o = con.newInstance(args);
				System.out.println("New object created for " + className);
				return o;
								
			}
        	catch (InvocationTargetException e){
        		System.out.println( e.getCause());
        	}
        	catch (Exception e) {  //TODO kinda bad to catch all here
				e.printStackTrace();
			} 
        }
		return null;
    }

    public void setListener(SensorEventListener lst) {
        this.sensorEventListener = lst;
    }

    @Override
    public void run() {
        monitorSensorPorts();



        // get a port instance
//        Port port = LocalEV3.get().getPort("S1");
////
//
//        EV3GyroSensor gyro = new EV3GyroSensor(port);
//        SensorModes modes = (SensorModes)gyro;
//        System.out.println(modes.getAvailableModes());
        //HiTechnicAccelerometer a = new HiTechnicAccelerometer(port);
//    	I2CPort ii = SensorPort.S4.open(I2CPort.class);
//    	I2CSensor s = new I2CSensor(ii); // NOTE CAN't use this ii, must use the standart port to get the right one, initilize it and such
//    	System.out.println(s.getProductID());

    }


    public void exit(){
        this.running = false;
    }

}
