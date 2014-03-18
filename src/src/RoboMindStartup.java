package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.sensor.SensorModes;
import src.motor.MotorControl;
import src.sensor.SensorControl;
import src.util.Request;
import src.util.Response;

import java.io.IOException;
import java.util.HashMap;

/**
 * The starting point of the program
 */
public class RoboMindStartup {

    static Gson gson = new GsonBuilder().create();
    static boolean running = true;
    static Communication communication;

    public static void main(String[] args) throws IOException {
        System.out.println("RoboMind started");

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


//        new TuneThread().start(); //Tune to know that the program has started

//        SensorTests s = new SensorTests();
        SensorControl sensorControl = new SensorControl();
        communication = new Communication();
//        MonitorSensorsThread monitorSensorsThread = new MonitorSensorsThread();
//        monitorSensorsThread.setSensorEventListener(sensorEventListener);

//        SampleThread sampleThread = new SampleThread(monitorSensorsThread);
//        sampleThread.setListener(sensorEventListener);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Button.LEDPattern(0);
            }
        });
        MotorControl mc = new MotorControl();

        Button.LEDPattern(9);
        communication.setUpConnection();
        Button.LEDPattern(1);
//        monitorSensorsThread.start();
//        sampleThread.start();
//        sensorEventListener.initialize();  // force the objects into memory, quicker building later

        //TODO do while?
        //TODO remove data field if there is no data
        String command;
        Request data;
        Response response = new Response();
        while(running){
            try {
                command = communication.receive();
                if (command == null){
                    throw new IOException();
                }
            } catch (Exception e) {
                Button.LEDPattern(9);
                mc.reset();
                sensorControl.reset();
                communication.close();
                communication.setUpConnection();
                Button.LEDPattern(1);
                continue;
            }

            System.out.println(command);
            data = gson.fromJson(command, Request.class);

            response.reset();
            response.seq = data.seq;
            response.msg = "response";
            if (data.cla.equals("motor")){
                if (data.cmd.equals("forward")){
                    mc.forward(data.motor_port);
                }
                else if (data.cmd.equals("backward")){
                    mc.backward(data.motor_port);
                }
                else if (data.cmd.equals("stop")){
                    mc.stop(data.motor_port, data.immediate);
                }
                else if (data.cmd.equals("rotate")){
                    mc.rotate(data.motor_port, data.degrees, data.immediate);
                }
                else if (data.cmd.equals("rotate_to")){
                    mc.rotateTo(data.motor_port, data.degrees, data.immediate);
                }
                else if (data.cmd.equals("set_speed")){
                    mc.setSpeed(data.motor_port, data.speed);
                }
                else if (data.cmd.equals("set_acceleration")){
                    mc.setAcceleration(data.motor_port, data.acceleration);
                }
                else if (data.cmd.equals("set_stalled_threshold")){
                    mc.setStallhreshold(data.motor_port, data.error, data.time);
                }
                else if (data.cmd.equals("reset_tacho_count")){
                    mc.resetTachoCount(data.motor_port);
                }
                else if (data.cmd.equals("set_float_mode")){
                    mc.setFloatMode(data.motor_port);
                }
                else if (data.cmd.equals("get_tacho_count")){
                    response.data = mc.getTachoCount(data.motor_port);
                }
                else if (data.cmd.equals("get_position")){
                    response.data = mc.getPosition(data.motor_port);

                }
                else if (data.cmd.equals("is_moving")){
                    response.data = mc.isMoving(data.motor_port) ? 1 : 0;

                }
                else if (data.cmd.equals("is_stalled")){
                    response.data = mc.isStalled(data.motor_port) ? 1 : 0;

                }
                else if (data.cmd.equals("get_max_speed")){
                    response.data = mc.getMaxSpeed(data.motor_port);

                }
                else{
                    throw new IOException("Invalid command");
                }
            }
            else if (data.cla.equals("sensor")){
                if (data.cmd.equals("open_sensor")){
                    response.data = sensorControl.openSensorByNameOnPort(data.sensor_class_name, data.sensor_port) ? 1 : 0;
                }
                else if (data.cmd.equals("close")){
                    sensorControl.close(data.sensor_port);
                }
                else if (data.cmd.equals("set_mode")){
                    sensorControl.setSensorModes(data.sensor_port, data.mode);
                }
                else if(data.cmd.equals("fetch_sample")){
                    response.sample = sensorControl.fetchSample(data.sensor_port);
                }
                else if(data.cmd.equals("call_method")){
                    response.data = sensorControl.callMethod(data.sensor_port, data.method) ? 1 : 0;
                }
                else if(data.cmd.equals("get_sensor_type")){
                    response.sample_string = sensorControl.getSensorAtPort(data.sensor_port);
                }
                else{
                    throw new IOException("Invalid command");
                }

            }
            else if (data.cla.equals("status")){
                if (data.cmd.equals("battery")){
                    response.data = Battery.getVoltageMilliVolt();
                }

            }
            else{
                throw new IOException("Invalid class");
            }
            communication.send(gson.toJson(response));
        }
    }
}
