package uk.ac.rhul.cs.robotics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import ev3dev.actuators.LCD;
import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.sensors.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.MotorPort;
import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;

import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.DestinationUnreachableException;

import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
import lejos.robotics.pathfinding.PathFinder;
import lejos.robotics.pathfinding.ShortestPathFinder;


public class EV3Mapper {
  private final static String BASE_IP = "10.0.1."; // Check this from the PC application.
  private final static int PORT = 2468; // Yu can choose any port, but it must be the same on the
  
  private static GraphicsLCD lcd = LCD.getInstance();

  public static enum COMMANDS {
    POSE('P'), DESTINATION('D'), START('B'), STOP('E'), EXIT('X'), MAP('M');

    private final char keyCode;

    private COMMANDS(char k) {
      keyCode = k;
    }

    public final char getCode() {
      return keyCode;
    }
  }

  public static void writeMessage(String message) { 
    lcd.setColor(Color.BLACK);
    lcd.drawString(message, 50, 50, 0);
    lcd.refresh();
  }

  public static void clear() { 
    lcd.setColor(Color.WHITE);
    lcd.fillRect(0, 0, lcd.getWidth(), lcd.getHeight());
  }
  

  public static void main(String[] args) {
    // server
    DataInputStream in = null;
    DataOutputStream out = null;
    Timer sender = null;
    
    LCD.getInstance().drawString("Use up/down", 0, 0, 0);
    LCD.getInstance().drawString("to set IP", 2, 1, 0);
    LCD.getInstance().drawString("ENTER to finish", 2, 2, 0);
    LCD.getInstance().refresh();

    int ip_addr = 2;
    LCD.getInstance().drawString("IP " + BASE_IP + ip_addr + "   ", 0, 4, 0);
    int id = Button.waitForAnyPress();
    while (id != Button.ENTER.getId()) {

      if (id == Button.UP.getId()) { 
        ip_addr++;
      } else if (id == Button.DOWN.getId()) {
        ip_addr--;
      }

      ip_addr = Math.min(254, ip_addr);
      ip_addr = Math.max(1, ip_addr);
      LCD.getInstance().drawString("IP " + BASE_IP + ip_addr + "   ", 0, 4, 0);
      id = Button.waitForAnyPress();
    }

    LCD.getInstance().clear();
    LCD.getInstance().drawString("Server::" + BASE_IP + ip_addr + "   ", 0, 0, 0);
    LCD.getInstance().drawString("Connecting ...", 0, 1, 0);
    SocketAddress sa = new InetSocketAddress(BASE_IP + ip_addr, PORT);
    Socket connection = null;
    try {
      connection = new Socket();

      connection.connect(sa, 1500); // Timeout possible
      in = new DataInputStream(connection.getInputStream());
      out = new DataOutputStream(connection.getOutputStream());
      LCD.getInstance().drawString("Connected to Server", 0, 1,0);

    } catch (Exception ex) {
      // Could be Timeout or just a normal IO exception
      LCD.getInstance().drawString(ex.getMessage(), 0, 6, 0);
      connection = null;
    }

    LineMap map = null;
    Navigator navigator = getNavigator();
    Waypoint destination = new Waypoint(0,0);
    PathFinder pf = null;
    final int CLOSED = -1;

    while (connection != null) {
      try {
        int command = in.readChar();
        LCD.getInstance().clear();
        if (command == CLOSED) {
          LCD.getInstance().drawString("Remote close", 0, 3, 0);
          connection = null;
        }
        if (command == COMMANDS.MAP.getCode()) {
          LCD.getInstance().drawString("(M)AP", 0, 3, 0);
          map = new LineMap();
          map.loadObject(in);
          if (map != null) {
            pf = new ShortestPathFinder(map);
            // System.out.println(map.getBoundingRect().getX() + ", " + map.getBoundingRect().getY() + ", " + map.getBoundingRect().getWidth() + ", " + map.getBoundingRect().getHeight());
          }
        }
        if (command == COMMANDS.EXIT.getCode()) {
          LCD.getInstance().drawString("E(X)IT", 0, 3, 0);
          connection = null;
        }
        if (command == COMMANDS.POSE.getCode()) {
          Pose from = new Pose();
          from.loadObject(in);
          navigator.getPoseProvider().setPose(from);
          // System.out.println("POSE: Recvd: " + from.getX() + ", " + from.getY() + ", " + from.getHeading());
        }
        if (command == COMMANDS.DESTINATION.getCode()) {
          LCD.getInstance().drawString("(D)EST.", 0, 3, 0);
          if (pf != null) {
            navigator.stop();
            try {
              Pose start = navigator.getPoseProvider().getPose();
              // To avoid nav bugs move the start in the current direction of the robot first
              start.moveUpdate(1);
              destination.loadObject(in);
              Path route = pf.findRoute(start, destination);
              // System.out.println("DEST: Start: " + start.getX() + ", " + start.getY() + ", " + start.getHeading());
              // System.out.println("Dest: END: " + end.getX() + ", " + end.getY());
              // for (int index = 0 ; index < route.size() ; index++) {
              //   System.out.println("DEST: Path (point): " + route.get(index).getX() + ", " + route.get(index).getY());
              // }
              out.writeChar('R');
              route.dumpObject(out);
              navigator.setPath(route);
            } catch (DestinationUnreachableException e) {
              LCD.getInstance().drawString("POSE UNREACHABLE", 0, 3, 0);
            }
          }
        }
        if (command == COMMANDS.START.getCode()) { // We must not get asked to send anything except Pose's until stop is called.
          // Pose p = navigator.getPoseProvider().getPose();
          // System.out.println("NAV: Start Pose: " + p.getX() + ", " + p.getY() + ", " + p.getHeading());
          // IF we are very close to the destination then we do not bother to actually navigate to it.
          if (navigator.getPoseProvider().getPose().distanceTo(destination) > 1) {
            navigator.followPath();
          }
          LCD.getInstance().drawString("(B)EGIN", 0, 3, 0);
          sender = new Timer(true); // make sure to always set Timer to use Daemon thread.
          TimerTask repeatSend = new SenderTask(out, navigator); 
          sender.schedule(repeatSend, 0, 200);
        }
        if (command == COMMANDS.STOP.getCode()) {
          navigator.stop();
          LCD.getInstance().drawString("(E)ND", 0, 3, 0);
          sender.cancel();
        }
      } catch (IOException e) {
        // Just end the program if we get a broken file read
        connection = null;
      }
    }
    LCD.getInstance().clear();
    LCD.getInstance().drawString("Exiting - press ENTER", 0, 5, 0);
    Button.ENTER.waitForPressAndRelease();
  }

  private static Navigator getNavigator() {
    RegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.B); // These are swapped since there is no "reverse"
    RegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.A); // flag when creating the CHassis version of a pilot
    Wheel wheelLeft = WheeledChassis.modelWheel(left, 5.6f).offset(-7.0f);
    Wheel wheelRight = WheeledChassis.modelWheel(right, 5.6f).offset(7.0f);
    Chassis chassis =
        new WheeledChassis(new Wheel[] {wheelRight, wheelLeft}, WheeledChassis.TYPE_DIFFERENTIAL);
    MovePilot robot = new MovePilot(chassis);
    robot.setLinearSpeed(1.0); // Since we are measuring the robot in cm this is 5mm per second.
    robot.setAngularSpeed(10.0); // This is a rotational velocity of 10 degrees per second.
    return new Navigator(robot); // Use default OdometryPoseProvider
  }

  private static class SenderTask extends TimerTask {
    private DataOutputStream server;
    private Navigator nav;
    private Pose lastSent;

    public SenderTask(DataOutputStream server, Navigator nav) {
      this.server = server;
      this.nav = nav;
    }

    public void run() {
      try {
        if (nav != null) {
          server.writeChar(COMMANDS.POSE.getCode());
          Pose toSend = nav.getPoseProvider().getPose();
          toSend.dumpObject(server);
          if (lastSent != null && lastSent.distanceTo(toSend.getLocation()) > 5) {
            // Wow - we seem to have jumped! - wtf - this happens and is hard to understand.
            LCD.getInstance().drawString("JUMPING??", 0,5,0);
          }
          LCD.getInstance().clear();
          LCD.getInstance().drawString("Sent:" + (int) toSend.getX() + "," + (int) toSend.getY()  + "    ", 0,4,0);
          lastSent = toSend;
        }
        server.flush();
      } catch (IOException e) {
        return;
      }
    }
  }
}
