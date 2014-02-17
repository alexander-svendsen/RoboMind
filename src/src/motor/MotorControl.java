package src.motor;

import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;

import java.util.HashMap;
import java.util.Map;

public class MotorControl {

    private Map<String, NXTRegulatedMotor> motors = new HashMap<String, NXTRegulatedMotor>();

    public MotorControl(){
        motors.put("A", Motor.A);
        motors.put("B", Motor.B);
        motors.put("C", Motor.C);
        motors.put("D", Motor.D);
    }

    public void forward(String motorPort){
        motors.get(motorPort).forward();
    }

    public void backward(String motorPort){
        motors.get(motorPort).backward();
    }

    public void stop(String motorPort) {
        motors.get(motorPort).stop(true);
    }

    public void rotate(String motorPort, int degrees, boolean immediateReturn){
//        System.out.println(motors.get(motorPort).getLimitAngle());
        motors.get(motorPort).rotate(degrees, immediateReturn);
//        System.out.println(motors.get(motorPort).getLimitAngle());
    }

    public void rotateTo(String motorPort, int angle, boolean immediateReturn){
//        System.out.println(motors.get(motorPort).getLimitAngle());
        motors.get(motorPort).rotateTo(angle, immediateReturn);
//        System.out.println(motors.get(motorPort).getLimitAngle());
    }

    public void setSpeed(String motorPort, float speed){
        motors.get(motorPort).setSpeed(speed);
    }

    public void setAcceleration(String motorPort, int acceleration){
        motors.get(motorPort).setAcceleration(acceleration);
    }

    public void setStallhreshold(String motorPort, int error, int time){
        motors.get(motorPort).setStallThreshold(error, time);
    }

    public void resetTachoCount(String motorPort){
        motors.get(motorPort).resetTachoCount();
    }

    public void setFloatMode(String motorPort){
        motors.get(motorPort).flt();
    }

    public int getTachoCount(String motorPort){
        return motors.get(motorPort).getTachoCount();
    }

    public int getPosition(String motorPort){
        return motors.get(motorPort).getPosition();
    }

    public boolean isMoving(String motorPort){
        return motors.get(motorPort).isMoving();
    }

    public boolean isStalled(String motorPort){
        return motors.get(motorPort).isStalled();
    }

    public float getMaxSpeed(String motorPort){
        return motors.get(motorPort).getMaxSpeed();
    }

}
