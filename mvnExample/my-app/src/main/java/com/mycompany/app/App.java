package com.mycompany.app;






import ev3dev.actuators.LCD;
import ev3dev.actuators.Sound;
import ev3dev.actuators.ev3.EV3Led;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import lejos.hardware.lcd.*;
import lejos.utility.Delay;
import lejos.hardware.port.MotorPort;
import lejos.hardware.LED;
import lejos.hardware.Sounds;
import lejos.hardware.Audio;



public class App {

    public static void main(String[] args) {

        LEDTest();
        screenTest();
    
    }


    public static void motorTest() { 

        final EV3LargeRegulatedMotor mA = new EV3LargeRegulatedMotor(MotorPort.A);
        mA.setSpeed(500);
        mA.brake();
        mA.forward();
        Delay.msDelay(2000);
        mA.stop();
        
    }

    public static void screenTest() {
        GraphicsLCD screen = LCD.getInstance();
        screen.setColor(0,0,0);
        screen.drawRect(0,0, screen.getWidth(), screen.getHeight());
        screen.fillRect(0,0, screen.getWidth(), screen.getHeight());
        screen.setColor(255,255,255);
        screen.drawString("HELLO, WORLD!", (screen.getWidth()/ 2)-30, screen.getHeight()/2, 0);
        screen.refresh();
        Delay.msDelay(1000);

    }

    public static void soundTest() { 

        
        
    }


    public static void LEDTest() {

        LED led = new EV3Led(EV3Led.LEFT);
        LED led2 = new EV3Led(EV3Led.RIGHT);

        for (int i = 0; i < 10; i++) {
            led.setPattern(1);
            led2.setPattern(1);
            Delay.msDelay(100);
            led.setPattern(0);
            led2.setPattern(0);
            Delay.msDelay(100);
        }




    }

    



}

