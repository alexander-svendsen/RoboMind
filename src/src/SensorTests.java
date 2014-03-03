package src;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.HiTechnicAccelerometer;
import lejos.hardware.sensor.HiTechnicGyro;

public class SensorTests {
    public SensorTests(){
        // get a port instance
        Port port = LocalEV3.get().getPort("S4");
        System.out.println(port.getSensorType());
        System.out.println(port.getPortType());

        EV3GyroSensor gyro = new EV3GyroSensor(port);
        HiTechnicGyro g = new HiTechnicGyro(port);
        System.out.println(g.getAvailableModes());


        HiTechnicAccelerometer a = new HiTechnicAccelerometer(port);
//    	I2CPort ii = SensorPort.S4.open(I2CPort.class);
//    	I2CSensor s = new I2CSensor(ii); // NOTE CAN't use this ii, must use the standart port to get the right one, initilize it and such
//    	System.out.println(s.getProductID());

//    	SensorModes sensor = s;
//    	System.out.println(sensor.getAvailableModes());
//    	SampleProvider distance= sensor.getMode(0);
//    	float[] sample = new float[distance.sampleSize()];
//
//    	System.out.println("length of sample: " + distance.sampleSize());
//    	fetch a sample
//    	distance.fetchSample(sample, 0);
//    	System.out.println(sample[0]);
//    	System.out.println(sample.length);


    }

}
