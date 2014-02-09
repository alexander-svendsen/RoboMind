package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lejos.hardware.sensor.SensorModes;

import java.util.HashMap;

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

            //TODO prolbems with sending whole shit, may because of the unsynch problems shit
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


        communication = new Communication();
        MonitorSensorsThread monitorSensorsThread = new MonitorSensorsThread();
        monitorSensorsThread.setSensorEventListener(sensorEventListener);

        SampleThread sampleThread = new SampleThread(monitorSensorsThread);
        sampleThread.setListener(sensorEventListener);

        communication.setUpConnection();
        monitorSensorsThread.start();
        sampleThread.start();
        sensorEventListener.initialize();  // force the objects into memory, quicker building later

        while(running);

    }
}
