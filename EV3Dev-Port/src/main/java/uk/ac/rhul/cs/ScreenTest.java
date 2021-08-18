package uk.ac.rhul.cs;

import lejos.hardware.lcd.GraphicsLCD;

import ev3dev.actuators.LCD;

import lejos.utility.Delay;

/**
 * Class that tests the LCD screen of the EV3 brick
 * running on EV3Dev-lang-java
 */
public class ScreenTest {

    /**
     * The Graphics LCD is an instance of the LCD singleton
     */
    private static GraphicsLCD screen;
    
    public static void main(String[] args) {
        screen = LCD.getInstance();
        screen.setColor(0,0,0);
        screen.drawRect(0,0, screen.getWidth(), screen.getHeight());
        screen.fillRect(0,0, screen.getWidth(), screen.getHeight());
        screen.setColor(255,255,255);
        screen.drawString("HELLO, WORLD!", (screen.getWidth()/ 2)-30, screen.getHeight()/2, 0);
        screen.refresh();
        Delay.msDelay(1000);
    }
}
