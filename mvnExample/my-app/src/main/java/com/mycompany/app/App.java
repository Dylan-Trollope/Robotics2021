package com.mycompany.app;

import org.slf4j.Logger;

import ev3dev.actuators.LCD;
import ev3dev.actuators.Sound;
import ev3dev.actuators.ev3.EV3Led;
import ev3dev.actuators.lego.motors.BaseRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.sensors.Button;
import lejos.hardware.lcd.*;
import lejos.utility.Delay;
import lejos.hardware.port.MotorPort;
import lejos.hardware.LED;
import lejos.hardware.Sounds;
import lejos.hardware.Audio;




public class App  {

    public static void main(String[] args) {
        
        squareCar();
    }

    static class Lights implements Runnable { 


        public void run() {

            LED led = new EV3Led(EV3Led.LEFT);
            LED led2 = new EV3Led(EV3Led.RIGHT);
    
            for (int i = 0; i < 10; i++) {
                led.setPattern(1);
                led2.setPattern(1);
                Delay.msDelay(100);
                led.setPattern(2);
                led2.setPattern(2);
                Delay.msDelay(100);
                led.setPattern(3);
                led2.setPattern(3);
                Delay.msDelay(100);
    
            }
        }

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

        for (;;) {
            led.setPattern(1);
            led2.setPattern(1);
            Delay.msDelay(100);
            led.setPattern(2);
            led2.setPattern(2);
            Delay.msDelay(100);
            led.setPattern(3);
            led2.setPattern(3);
            Delay.msDelay(100);

        }

    }

    

    public static void goCar() { 

        BaseRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        BaseRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.B);

        Lights l = new Lights();
        Thread thread = new Thread(l);
        thread.start();

        mLeft.setSpeed(720);
        mRight.setSpeed(720);


        mLeft.forward();
        mRight.forward();


        Button.ENTER.waitForPressAndRelease();

        mLeft.stop();
        mRight.stop();

        LCD.getInstance().drawString(""+mLeft.getTachoCount(), 100, 100, 0);
        Button.ENTER.waitForPressAndRelease();

        thread.interrupt();
        

    }

    public static void squareCar() { 
        BaseRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        BaseRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.B);

        final int NINETY_DEGREE_TURN_TIME = 625;

        mLeft.setSpeed(360);
        mRight.setSpeed(360);

        for (int i = 0; i < 4; i++) {

            mLeft.forward();
            mRight.forward();
            Delay.msDelay(3000); // go forward
    
            mLeft.stop();
            mRight.stop();
    
            mLeft.forward();
            mRight.backward();
    
            Delay.msDelay(NINETY_DEGREE_TURN_TIME);
    
            mLeft.stop();
            mRight.stop();
            
            
        }

       


    }

}

