package uk.ac.rhul.cs;



import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to show successful running of motors on EV3 brick using 
 * EV3Dev-lang-java
 */
public class MotorTest {

    
    /**
     * EV3 Motor plugged into Port A
     */
    private static EV3LargeRegulatedMotor mA;

    /**
     *  Ev3 Motor plugged into Port B 
     */ 
    private static EV3LargeRegulatedMotor mB;

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(MotorTest.class);

        if (args.length == 0) { 
            logger.info(help());
            return;
        }
        switch(Integer.parseInt(args[0])) {
            case 1: 
                singleMotor();
            break;

            case 2:
                doubleMotor();
            break;

            case 3:
                logger.error("Not implemented yet.");
            break;

            default:
                logger.info(help());
            break;

        }
    }

    /** 
     * Test with a single motor
     */
    public static void singleMotor() {
        mA = new EV3LargeRegulatedMotor(MotorPort.A);
        mA.setSpeed(500);
        mA.setAcceleration(100);
        mA.forward();
        Delay.msDelay(5000);
        mA.stop();

    }

    /**
     * Test two motors simultaneously running
     */
    public static void doubleMotor() { 
        mA = new EV3LargeRegulatedMotor(MotorPort.A);
        mB = new EV3LargeRegulatedMotor(MotorPort.B);

        mA.setSpeed(500);
        mB.setSpeed(500);
        mA.setAcceleration(100);
        mB.setAcceleration(100);
        mA.forward();
        mB.forward();
        Delay.msDelay(5000);

        mA.stop();
        mB.stop();


    }

    /**
     * Tests synchronous motors work together
     */
    public static void syncedMotors() {  

        // WIP 

    }
    
    /**
     * Returns the list of available arguments
     * @return String of available arguments
     */
    public static String help() {
        return "\nPlease give argument to perform test:\n" + 
                "1: Single Motor Test\n" + 
                "2: Double Motor Test\n" + 
                "3: Sync Motor Test";
    }

    
}
