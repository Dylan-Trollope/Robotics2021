package uk.ac.rhul.cs.sensors;

import ev3dev.actuators.LCD;
import ev3dev.sensors.Button;
import ev3dev.sensors.ev3.EV3ColorSensor;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.SensorPort;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class Normalise {

    static GraphicsLCD screen = LCD.getInstance();
    public static void main(String[] args) {

        EV3ColorSensor cs = new EV3ColorSensor(SensorPort.S1);
        SampleProvider sp = cs.getAmbientMode();
        float[] samples = new float[3];

        writeMessage("Dark:", 0, 10);
        Button.ENTER.waitForPressAndRelease();
        sp.fetchSample(samples, 0);

        writeMessage("Ambient:", 0, 20);
        Button.ENTER.waitForPressAndRelease();
        sp.fetchSample(samples, 1);

        writeMessage("Light:", 0, 30);
        Button.ENTER.waitForPressAndRelease();
        sp.fetchSample(samples, 2);

        float normalised = (samples[1] - samples[0]) / (samples[2] - samples[0]);


        screen.clear();
        screen.refresh();

        writeMessage("Lowest = " + samples[0], 0, 10);
        writeMessage("Actual = " + samples[1], 0, 20);
        writeMessage("Highest = " + samples[2], 0, 30);


        writeMessage("Normalised val = " + normalised, 0, 50);

        
    }

    public static void writeMessage(String message, int x, int y) {

        screen.setColor(Color.BLACK);
        screen.drawString(message, x, y, 0);
        screen.refresh();

    }
    
    



}
