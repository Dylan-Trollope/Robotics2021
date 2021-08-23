package uk.ac.rhul.cs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ev3dev.actuators.Sound;

/**
 * Class to test the speaker on the EV3 brick 
 * running EV3Dev-lang-java
 */
public class SoundTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(SoundTest.class);

        if (args.length == 0) {
            logger.error(help());
            return;
        }
        switch(Integer.parseInt(args[0])) { 
            case 1: 
                testBeep();
            break;

            default:
                logger.error(help());
            

        }

        testBeep();
    }

    /**
     * Beep Once
     */
    public static void testBeep() { 
        for (int i = 0; i < 4; i++) {
            Sound.getInstance().beep();    
        }
        

    }

    public static String help() {
        return "\nPlease specify an argument\n" + 
        "1: Beep once";
        
    }

}
