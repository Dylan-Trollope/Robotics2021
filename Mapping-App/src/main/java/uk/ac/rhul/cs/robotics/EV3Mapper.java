package uk.ac.rhul.cs.robotics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Parameters for networking
     * ip_addr is changed on screen to the correct IP
     */
    private final static String BASE_IP = "10.42.0."; // Check this from the PC application.
    private static int ip_addr;
    private final static int PORT = 2468; // You can choose any port, but it must be the same on the


    /**
     * SOcket and Data objects that are used for communication
     */
    static Socket connection = null;
    static DataInputStream in = null;
    static DataOutputStream out = null;


    /**
     * Parameters for writing to the screen
     */
    private static GraphicsLCD lcd = LCD.getInstance(); // 178 x 128
    private final static int LINE_HEIGHT = 15;
    private final static int MARGIN = 0;


    /**
     * Logger useful for logging output to the remote terminal for debugging
     */
    static Logger logger = LoggerFactory.getLogger(EV3Mapper.class);


    /**
     * Write to the screen based on line number 
     * 8 lines max (I think)
     * This is better than specifying x/y coordinates everytime
     * @param i gets the line number to display
     * @return the pixel location to display at
     */
    public static int getLine(int i) {
        return i * LINE_HEIGHT;
    }


    /**
     * Writes a message to the screen and takes care of the boiler plate
     * @param message The message to write
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void writeMessage(String message, int x, int y) {
        lcd.setColor(Color.BLACK);
        lcd.drawString(message, x, y, 0);
        lcd.refresh();
    }


    /**
     * Allows the user to set the last quarter of the IP address on the screen
     * Assumes that IP address starts with 10.42.0.
     * @return the last quarter of the IP address as int
     */
    public static int setIPAddress() { 
        ip_addr = 0;

        writeMessage("Use UP/DOWN to set IP", MARGIN, getLine(1));
        writeMessage("ENTER to finish", MARGIN, getLine(2));
        writeMessage("IP " + BASE_IP + ip_addr + "   ", MARGIN, getLine(3));

        Button.waitForAnyPress();

        while (!Button.ENTER.isDown()) {

            if (Button.UP.isDown()) {
                ip_addr++;
            } else if (Button.DOWN.isDown()) {
                ip_addr--;
            }

            ip_addr = Math.min(254, ip_addr);
            ip_addr = Math.max(1, ip_addr);

            lcd.clear();

            writeMessage("Use up/down to set IP", 0, 10);
            writeMessage("ENTER to finish", 0, 20);
            writeMessage("IP " + BASE_IP + ip_addr + "   ", 0, 30);
            Button.waitForAnyPress();
        }
        return ip_addr;
    }

    /**
     * Screen that is diaplayed waiting for connection while connection is made
     */
    public static void confirmIP() { 
        lcd.clear();
        writeMessage("Server::" + BASE_IP + ip_addr + "    ", MARGIN, getLine(1));
        writeMessage("Connecting ...", MARGIN, getLine(2));
    }


    /**
     * Initialises socket to make connection to the PC 
     */
    public static void makeConnection() { 

        SocketAddress sa = new InetSocketAddress(BASE_IP + ip_addr, PORT);
        
        try {
            connection = new Socket();

            connection.connect(sa, 1500); // Timeout possible
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());
            LCD.getInstance().drawString("Connected to Server", 0, 1, 0);
            writeMessage("Connected to Server", MARGIN, getLine(3));

        } catch (Exception ex) {
            // Could be Timeout or just a normal IO exception
            writeMessage(ex.getMessage(), MARGIN, getLine(3));
            logger.error(ex.toString());

            connection = null;
        }

    }
    

    public static void main(String[] args) {
        // server
        
        Timer sender = null;
        LineMap map = null;
        Navigator navigator = getNavigator();
        Waypoint destination = new Waypoint(0, 0);
        PathFinder pf = null;
        final int CLOSED = -1;


        ip_addr = setIPAddress();
        confirmIP();
        makeConnection();

        while (connection != null) {
            try {
                int command = in.readChar();
                LCD.getInstance().clear();
                if (command == CLOSED) {
                    LCD.getInstance().drawString("Remote close", MARGIN, getLine(3), 0);
                    connection = null;
                }

                if (command == Commands.MAP.getCode()) {
                    LCD.getInstance().drawString("(M)AP", MARGIN, getLine(3), 0);
                    map = new LineMap();
                    map.loadObject(in);
                    if (map != null) {
                        pf = new ShortestPathFinder(map);
                        // System.out.println(map.getBoundingRect().getX() + ", " +
                        // map.getBoundingRect().getY() + ", " + map.getBoundingRect().getWidth() + ", "
                        // + map.getBoundingRect().getHeight());
                    }
                }

                if (command == Commands.EXIT.getCode()) {
                    LCD.getInstance().drawString("E(X)IT", MARGIN, getLine(3), 0);
                    connection = null;
                }
                if (command == Commands.POSE.getCode()) {
                    Pose from = new Pose();
                    from.loadObject(in);
                    navigator.getPoseProvider().setPose(from);
                    // System.out.println("POSE: Recvd: " + from.getX() + ", " + from.getY() + ", "
                    // + from.getHeading());
                }
                if (command == Commands.DESTINATION.getCode()) {
                    LCD.getInstance().drawString("(D)EST.", 0, 3, 0);
                    if (pf != null) {
                        navigator.stop();
                        try {
                            Pose start = navigator.getPoseProvider().getPose();
                            // To avoid nav bugs move the start in the current direction of the robot first
                            start.moveUpdate(1);
                            destination.loadObject(in);
                            Path route = pf.findRoute(start, destination);
                            // System.out.println("DEST: Start: " + start.getX() + ", " + start.getY() + ",
                            // " + start.getHeading());
                            // System.out.println("Dest: END: " + end.getX() + ", " + end.getY());
                            // for (int index = 0 ; index < route.size() ; index++) {
                            // System.out.println("DEST: Path (point): " + route.get(index).getX() + ", " +
                            // route.get(index).getY());
                            // }
                            out.writeChar('R');
                            route.dumpObject(out);
                            navigator.setPath(route);
                        } catch (DestinationUnreachableException e) {
                            LCD.getInstance().drawString("POSE UNREACHABLE", 0, 3, 0);
                        }
                    }
                }
                if (command == Commands.START.getCode()) { // We must not get asked to send anything except Pose's until
                                                           // stop is called.
                    // Pose p = navigator.getPoseProvider().getPose();
                    // System.out.println("NAV: Start Pose: " + p.getX() + ", " + p.getY() + ", " +
                    // p.getHeading());
                    // IF we are very close to the destination then we do not bother to actually
                    // navigate to it.
                    if (navigator.getPoseProvider().getPose().distanceTo(destination) > 1) {
                        navigator.followPath();
                    }
                    LCD.getInstance().drawString("(B)EGIN", 0, 3, 0);
                    sender = new Timer(true); // make sure to always set Timer to use Daemon thread.
                    TimerTask repeatSend = new SenderTask(out, navigator);
                    sender.schedule(repeatSend, 0, 200);
                }
                if (command == Commands.STOP.getCode()) {
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
        LCD.getInstance().drawString("Exiting - press ENTER", MARGIN, getLine(5), 0);
        Button.ENTER.waitForPressAndRelease();
    }


    private static Navigator getNavigator() {
        RegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.B); // These are swapped since there is no "reverse"
        RegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.A); // flag when creating the CHassis version of a
                                                                        // pilot
        Wheel wheelLeft = WheeledChassis.modelWheel(left, 5.6f).offset(-7.0f);
        Wheel wheelRight = WheeledChassis.modelWheel(right, 5.6f).offset(7.0f);
        Chassis chassis = new WheeledChassis(new Wheel[] { wheelRight, wheelLeft }, WheeledChassis.TYPE_DIFFERENTIAL);
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
                    server.writeChar(Commands.POSE.getCode());
                    Pose toSend = nav.getPoseProvider().getPose();
                    toSend.dumpObject(server);
                    if (lastSent != null && lastSent.distanceTo(toSend.getLocation()) > 5) {
                        // Wow - we seem to have jumped! - wtf - this happens and is hard to understand.
                        LCD.getInstance().drawString("JUMPING??", 0, 5, 0);
                    }
                    LCD.getInstance().clear();
                    LCD.getInstance().drawString("Sent:" + (int) toSend.getX() + "," + (int) toSend.getY() + "    ", 0,
                            4, 0);
                    lastSent = toSend;
                }
                server.flush();
            } catch (IOException e) {
                e.printStackTrace();;
            }
        }
    }
}
