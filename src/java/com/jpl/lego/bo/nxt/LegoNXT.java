package com.jpl.lego.bo.nxt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTCommandConnector;
import lejos.pc.comm.NXTConnector;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.PilotProps;

/**
 *
 * @author José Pereda Llamas
 * Created on 10-nov-2012 - 15:34:27
 * Based on the previous work of Sébastien Stormacq
 */
public class LegoNXT {

    private static LegoNXT singleton = null;

    private static boolean legoBlocked=false;
    private static boolean legoConnected=false;
    
    public static LegoNXT getSingleton() {

        final Object lock = LegoNXT.class;

        if (singleton == null) {
            synchronized(lock) {
                singleton = new LegoNXT();                
            }
        }
        return singleton;
    }

    private LegoNXT() {}
    
    public void connect(String NXTName, String Protocol) {
        final String connectString = Protocol.concat("://").concat(NXTName); 
        legoBlocked=true;
        legoConnected=false;
        try {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Initializing communication with NXT");
            NXTConnector cmd = new NXTConnector();
            cmd.addLogListener(new NXTCommLogListener() {
                @Override public void logEvent(String message) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, message);
                }
                @Override public void logEvent(Throwable throwable) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while talking to NXT brick", throwable);
                }
            });
            
	    if (!cmd.connectTo(connectString, NXTComm.LCP)) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect");
                legoBlocked=false;
                return;
            }

            NXTCommandConnector.setNXTCommand(new NXTCommand(cmd.getNXTComm()));
            Logger.getLogger(getClass().getName()).log(Level.FINE, "Done");
            legoConnected=true;

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while initializing connection", e);
            legoBlocked=false;
        }
    }

    public void disconnect() {
        try {
            NXTCommandConnector.close();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        legoConnected=false;
        legoBlocked=false;
    }
    
    public boolean isLegoBlocked() { return legoBlocked; }
    public boolean isLegoConnected(){ return legoConnected; }
    /*
     * NXT METHODS 
     */
    public String getDeviceName() throws IOException {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Device information");
        String result = "";
        try{
            result=NXTCommandConnector.getSingletonOpen().getFriendlyName();
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to DeviceName");
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Connected to NXT named : {0}", result);

        return result;
    }

    public int getDeviceBateryLevel() throws IOException {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Device information");
        int result = 0;
        try{
            result=NXTCommandConnector.getSingletonOpen().getBatteryLevel();
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to BatteryLevel");
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Device Batery Level : {0}", result);

        return result;
    }

    public String getDeviceBluetoothAddress() throws IOException {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Device Bluetooth information");
        String result = "";
        try{
            result=NXTCommandConnector.getSingletonOpen().getDeviceInfo().bluetoothAddress;
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to DeviceInfo");
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Device BlueTooth Address : {0}", result);

        return result;
    }

    public String getDeviceFirmwareVersion() throws IOException {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Device information");
        String result = "";
        try{
            result=NXTCommandConnector.getSingletonOpen().getFirmwareVersion().firmwareVersion;
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to Firmware");
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Device Batery Level : {0}", result);

        return result;
    }

    private SensorPort getPortWithNumber(int portNumber) {

        SensorPort result;

        switch(portNumber) {
            case 1: result = SensorPort.S1;
                    break;
            case 2: result = SensorPort.S2;
                    break;
            case 3: result = SensorPort.S3;
                    break;
            default:
                result = SensorPort.S1;
        }

        return result;
    }

    public String getDistanceName(int portNumber) {

        SensorPort port = getPortWithNumber(portNumber);

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Color Name from port {0}", portNumber);
        UltrasonicSensor distanceSensor = new UltrasonicSensor(port);
        return distanceSensor.getUnits();
//        ColorLightSensor lightSensor = new ColorLightSensor(port, ColorLightSensor.TYPE_COLORFULL);
//        Colors.Color color = lightSensor.readColor();

//        return color.name();
    }

    public int getDistanceValue(int portNumber) {

        SensorPort port = getPortWithNumber(portNumber);

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Color Value from port {0}", portNumber);
        //ColorLightSensor lightSensor = new ColorLightSensor(port, ColorLightSensor.TYPE_COLORFULL);
        UltrasonicSensor distanceSensor = new UltrasonicSensor(port);
        distanceSensor.continuous();
        return distanceSensor.getDistance(); //lightSensor.readValue();
    }

    public boolean getTouchValue(int portNumber) {

        SensorPort port = getPortWithNumber(portNumber);

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Touch Name from port {0}", portNumber);
        TouchSensor touchSensor = new TouchSensor(port);
        return touchSensor.isPressed();
    }
    
    public int getVolume(){
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting Device Volume");
        int result = -1;
        try{
            result=NXTCommandConnector.getSingletonOpen().getVolume();
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to Volume");
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Device Volume : {0}", result);

        return result;
    }
    
    public int setVolume(byte volume){
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Setting Device Volume");
        int result = -1;
        try{
            int status=NXTCommandConnector.getSingletonOpen().setVolume(volume);
            if(status==0){
                NXTCommandConnector.getSingletonOpen().playTone(400, 500);
                result=NXTCommandConnector.getSingletonOpen().getVolume();
            }
        } catch(IOException e){
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to connect to Volume: "+e);
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Device Volume : {0}", result);
        return result;
    }
    
    
    /*
     * MOTORS
     */
    private DifferentialPilot pilot = null;
    private static final float ROTATION_ANGLE = 90.0f;

    public void initialize(String motorLeft, String motorRight, float wheelDiameter, float wheelDistance) {
        PilotProps pp = new PilotProps();
    	RegulatedMotor leftMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, motorLeft));
    	RegulatedMotor rightMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, motorRight));
    	
        pilot = new DifferentialPilot(wheelDiameter, wheelDistance, leftMotor, rightMotor);
    }
    public boolean isInitiliazed() {
        return pilot != null;
    }

    public void rotateLeft() {
        rotate(0-ROTATION_ANGLE);
    }

    public void rotateRight() {
        rotate(ROTATION_ANGLE);

    }

    public void rotate(float angle) {
        pilot.rotate(angle);
    }

    public void travel(float distance) {
        pilot.travel(distance);
    }

    public void travel(float distance, boolean inmediateReturn) {
        pilot.travel(distance, inmediateReturn);
    }

    public void forward() {
        pilot.forward();
    }

    public void stop() {
        pilot.stop();
    }

    public void backward() {
        pilot.backward();
    }

    public void setSpeed(float speed) {
        pilot.setTravelSpeed(speed);
    }

    public float getSpeed() {
        return (float)pilot.getTravelSpeed();
    }

    public float getMaxSpeed() {
        return (float)pilot.getMaxTravelSpeed();
    }
    
    public boolean isMoving() {
        return pilot.isMoving();
    }
}
