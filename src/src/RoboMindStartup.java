package src;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lejos.hardware.Battery;
import lejos.hardware.Button;
import src.motor.MotorControl;
import src.sensor.SensorControl;
import src.util.HostName;
import src.util.Request;
import src.util.Response;

import java.io.IOException;

/**
 * The starting point of the program
 */
public class RoboMindStartup {

    static Gson gson = new GsonBuilder().create();
    static boolean running = true;
    static Communication communication;

    static SensorEventListener sensorEventListener = new SensorEventListener() {
        Response res = new Response();

        public void sendData(){
            communication.send(gson.toJson(res));
        }

        @Override
        public void newSensor(String sensorClassName, int portNumber) {
            res.reset();
            res.msg = "sensor_info";
            res.sample_string = sensorClassName;
            res.data = portNumber;
            sendData();
        }

        @Override
        public void newSamples(float[][] sampleArray) {
            res.reset();
            res.msg = "samples";
            res.samples =  sampleArray;
            sendData();
        }
    };

    public static void startUpCommunication(){
        Button.LEDPattern(9);
        communication.setUpConnection();
        Button.LEDPattern(1);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("RoboMind started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Button.LEDPattern(0);
            }
        });

//        new TuneThread().start(); //Tune to know that the program has started

        communication = new Communication();
        SensorControl sensorControl = new SensorControl(sensorEventListener);
        MotorControl motorControl = new MotorControl();

        startUpCommunication();

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
                motorControl.reset();
                sensorControl.reset();
                communication.close();
                startUpCommunication();
                continue;
            }

            System.out.println(command);
            data = gson.fromJson(command, Request.class);

            response.reset();
            response.seq = data.seq;
            response.msg = "response";
            if (data.cla.equals("motor")){
                if (data.cmd.equals("forward")){
                    motorControl.forward(data.motor_port);
                }
                else if (data.cmd.equals("backward")){
                    motorControl.backward(data.motor_port);
                }
                else if (data.cmd.equals("stop")){
                    motorControl.stop(data.motor_port, data.immediate);
                }
                else if (data.cmd.equals("rotate")){
                    motorControl.rotate(data.motor_port, data.degrees, data.immediate);
                }
                else if (data.cmd.equals("rotate_to")){
                    motorControl.rotateTo(data.motor_port, data.degrees, data.immediate);
                }
                else if (data.cmd.equals("set_speed")){
                    motorControl.setSpeed(data.motor_port, data.speed);
                }
                else if (data.cmd.equals("set_acceleration")){
                    motorControl.setAcceleration(data.motor_port, data.acceleration);
                }
                else if (data.cmd.equals("set_stalled_threshold")){
                    motorControl.setStallhreshold(data.motor_port, data.error, data.time);
                }
                else if (data.cmd.equals("reset_tacho_count")){
                    motorControl.resetTachoCount(data.motor_port);
                }
                else if (data.cmd.equals("set_float_mode")){
                    motorControl.setFloatMode(data.motor_port);
                }
                else if (data.cmd.equals("get_tacho_count")){
                    response.data = motorControl.getTachoCount(data.motor_port);
                }
                else if (data.cmd.equals("get_position")){
                    response.data = motorControl.getPosition(data.motor_port);

                }
                else if (data.cmd.equals("is_moving")){
                    response.data = motorControl.isMoving(data.motor_port) ? 1 : 0;

                }
                else if (data.cmd.equals("is_stalled")){
                    response.data = motorControl.isStalled(data.motor_port) ? 1 : 0;

                }
                else if (data.cmd.equals("get_max_speed")){
                    response.data = motorControl.getMaxSpeed(data.motor_port);

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
                    response.sample_string = sensorControl.dynamicallyDiscoverSensorNameAtPort(data.sensor_port);
                }
                else{
                    throw new IOException("Invalid command");
                }
            }
            else if (data.cla.equals("status")){
                response.data = Battery.getVoltageMilliVolt();
                response.sample_string = HostName.getHostName();
            }
            else if (data.cla.equals("subscribe")){
                if (data.cmd.equals("subscribe_on_sensor_changes")){
                    sensorControl.startMonitorThread();
                }
                else if(data.cmd.equals("subscribe_on_stream_data")){
                    sensorControl.startSampleThread();
                }
                else if(data.cmd.equals("close")){
                    sensorControl.resetThreads();
                }
            }
            else{
                throw new IOException("Invalid class");
            }
            communication.send(gson.toJson(response));
        }
    }
}
