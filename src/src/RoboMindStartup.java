package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lejos.hardware.Button;
import lejos.hardware.sensor.SensorModes;

import java.util.HashMap;
import java.util.Map;

/**
 * The starting point of the program
 */
public class RoboMindStartup {

    static Gson gson = new GsonBuilder().create();
    static boolean running = true;
    static Communication communication;

    public static void main(String[] args) {
        System.out.println("Robomind started");

        SensorEventListener sensorEventListener = new SensorEventListener() {
            HashMap obj = new HashMap<String, String>();

            @Override
            public synchronized void initialize(){
                obj.put("test", "clear_me");
                gson.toJson(obj);
                obj.clear();
            }

            public void sendData(){
                //System.out.println(gson.toJson(obj));
                communication.send(gson.toJson(obj));
                obj.clear();
            }

            @Override
            public synchronized void newSensor(Object sensorObject, String sensorClassName, int portNumber) {
                SensorModes sensorModes = (SensorModes) sensorObject;
                obj.put("cmd", "newSensor");
                obj.put("sensorClass", sensorClassName);
                obj.put("port", portNumber);
                obj.put("availableModes", sensorModes.getAvailableModes());

                sendData();
            }

            @Override
            public synchronized void newInfo(String cmd, int portNumber) {
                obj.put("cmd", cmd);
                obj.put("port", portNumber);

                sendData();
            }

            @Override
            public synchronized void newSamples(float[][] sampleArray) {
                obj.put("cmd", "fecthedSamples");
                obj.put("samples", sampleArray);

                sendData();
            }
        };


        //new TuneThread().start(); //Tune to know that the program has started
        Button.LEDPattern(9);

        communication = new Communication();
        MonitorSensorsThread monitorSensorsThread = new MonitorSensorsThread();
        monitorSensorsThread.setSensorEventListener(sensorEventListener);

        SampleThread sampleThread = new SampleThread(monitorSensorsThread);
        sampleThread.setListener(sensorEventListener);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Button.LEDPattern(0);
            }
        });
        LCDControl.showIp(Communication.getIPAddresses());

        communication.setUpConnection();
        Button.LEDPattern(1);
        monitorSensorsThread.start();
//        sampleThread.start();
        sensorEventListener.initialize();  // force the objects into memory, quicker building later

//        new MotorControl();
        //TODO do while?
        String command;
        Map<String, String> data = new HashMap<String, String>();
        while(running){
            command = communication.recive();
            System.out.println(command);
            data = (Map<String,String>) gson.fromJson(command, data.getClass());
        }

    }

    public static void print(String s){
        System.out.println(s);
    }
}
