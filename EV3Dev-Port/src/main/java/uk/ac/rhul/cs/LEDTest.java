package uk.ac.rhul.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ev3dev.actuators.ev3.EV3Led;
import ev3dev.actuators.ev3.EV3Led.Direction;
import lejos.hardware.LED;
import lejos.utility.Delay;

/**
 * Class used to show successful usage of the LEDs on
 * EV3 brick running EV3Dev-lang-java
 */
public class LEDTest {
    /**
     * The Right side LED
     */
    static final LED ledRight = new EV3Led(Direction.RIGHT);

    /**
     * The Left side LED
     */
    static final LED ledLeft = new EV3Led(Direction.LEFT);
    

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(LEDTest.class);

        if (args.length == 0) logger.error(help());

        switch(Integer.parseInt(args[0])) {
            case 1: 
                testLeftLED();
            break;
            case 2:
                testRightLED();
            break;
            case 3:
                testSetPattern();
                break;
            default: 
                logger.error(help());

        }
        
    }

    /**
     * Sets the Left LED to red for 2 seconds
     */
    public static void testLeftLED() {

        ledLeft.setPattern(2);
        Delay.msDelay(2000);

    }

    /**
     * Sets the Right LED to red for 2 seconds
     */
    public static void testRightLED() {

        ledRight.setPattern(2);

    } 


    /**
     * Cycles both LEDs through green/orange/red 10 times
     */
    public static void testSetPattern() {

        for (int i = 0; i < 10; i++) {
            ledLeft.setPattern(0);
            ledRight.setPattern(0);
            Delay.msDelay(200);

            ledLeft.setPattern(1);
            ledRight.setPattern(1);
            Delay.msDelay(200);
            
            ledLeft.setPattern(2);
            ledRight.setPattern(2);
            Delay.msDelay(200);
            
            ledLeft.setPattern(3);
            ledRight.setPattern(3);
            Delay.msDelay(200);
            
        }

    }

    public static String help() { 
        return "\nPlease specify an argument\n" + 
               "1: Left LED\n" +
               "2: Right LED\n" + 
               "3: Test setPattern\n"; 
    }

    
}
