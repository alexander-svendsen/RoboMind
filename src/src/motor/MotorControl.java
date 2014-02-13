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


//        nxtRegulatedMotor.rotate(360);
//        nxtRegulatedMotor.setSpeed(720);
//
//        nxtRegulatedMotor.forward();
//
//        Delay.msDelay(10000);
//        nxtRegulatedMotor.resetTachoCount();
//
//        System.out.println(nxtRegulatedMotor.getPosition());
//        nxtRegulatedMotor.stop();
    }

    public void forward(String motorPort){
        motors.get(motorPort).forward();
    }

    public void stop(String motorPort) {
        motors.get(motorPort).stop();
    }
}
