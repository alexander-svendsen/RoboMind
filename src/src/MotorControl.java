package src;

import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;

public class MotorControl {
    public NXTRegulatedMotor nxtRegulatedMotor = Motor.A;
    public MotorControl(){
        //nxtRegulatedMotor.rotate(360);
        //nxtRegulatedMotor.setSpeed(720);

        //nxtRegulatedMotor.forward();

        Delay.msDelay(5000);
        nxtRegulatedMotor.resetTachoCount();

        System.out.println(nxtRegulatedMotor.getPosition());
        nxtRegulatedMotor.stop();
    }
}
