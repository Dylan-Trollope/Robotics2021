package uk.ac.rhul.cs.sensors;



import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class ColourSensor {

    private static SampleProvider sp;
    
    private static EV3ColorSensor colSens = new EV3ColorSensor(SensorPort.S1);

    static Logger logger = LoggerFactory.getLogger(ColourSensor.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            logger.error(help());
        }
     
        switch(Integer.parseInt(args[0])) {

            case 1:
                testRedMode();
            break;

            case 2: 
                testColourIDMode();
            break;

            case 3: 
                testRGBMode();
            break;
            
            case 4: 
                testFloodlight();
            break;

            default:
                logger.error(help());
            break;


        }
       
    }


    public static void testFloodlight() { 
        colSens.setFloodlight(true);
        Delay.msDelay(5000);
        colSens.setFloodlight(false);
        Delay.msDelay(5000);
    }


    public static void testRedMode() { 

        sp = colSens.getRedMode();
        int sampleSize = sp.sampleSize();
        float[] sample = new float[sampleSize];

        for (int i = 0; i < 10; i++) {

            sp.fetchSample(sample, 0);
            logger.info((int) i + ": Sample = {}", sample[0]);
            Delay.msDelay(1000);
        }

    }

    public static void testColourIDMode() {

        sp = colSens.getColorIDMode();
        int sampleSize = sp.sampleSize();
        float[] sample = new float[sampleSize];

        for (int i = 0; i < 10; i++) {
            sp.fetchSample(sample, 0);
            logger.info((int) i + ": Sample = {}", sample[0]);
            Delay.msDelay(1000);
        }
    }


    public static void testRGBMode() { 

        sp = colSens.getRGBMode();
        int sampleSize = sp.sampleSize();
        float[] sample = new float[sampleSize];

        for (int i = 0; i < 10; i++) {
            sp.fetchSample(sample, 0);
            logger.info((int) i + ": R = {}, G = {}, B = {}", sample[0], sample[1], sample[2]);
            Delay.msDelay(1000);
        }

    }




    public static String help() { 
        return "\nPlease specify an argument\n" + 
        "1: Red Mode\n" + 
        "2: ColourID Mode";
    }

    
}
