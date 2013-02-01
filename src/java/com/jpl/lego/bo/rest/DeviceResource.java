package com.jpl.lego.bo.rest;

import com.jpl.lego.bo.nxt.LegoNXT;
import data.CData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.codehaus.jettison.json.JSONStringer;

/**
 * REST Web Service
 *
 * @author José Pereda Llamas
 * Created on 10-nov-2012 - 15:34:27
 */
@Path("device")
public class DeviceResource {

    private LegoNXT nxt = null;

    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("NXTName") @DefaultValue("NXT") String NXTName,
                          @QueryParam("Protocol") @DefaultValue("usb") String Protocol) {
        String result = null;
        if (nxt == null) {
            nxt = LegoNXT.getSingleton();
        }
        int cont=0;
        while(nxt.isLegoBlocked()){
            try{
                Thread.sleep(100);
                cont+=1;
            } catch(InterruptedException ie) {}
            if(cont>CData.TIME_OUT){ 
                String msg = "Device was blocked for more than 10 seconds";
                Logger.getLogger(getClass().getName()).log(Level.WARNING, msg);
                result = "{ \"status\" : \"" + msg + "\" }";
                nxt = null; 
                return result;
            }
        }
        
        try {
            nxt.connect(NXTName,Protocol);
            if(!nxt.isLegoConnected()){
                return "{ \"status\" : \"Device not connected\" }";
            }
            
            result = new JSONStringer().object()
                    .key(CData.JSON_RESPONSE_DEVICENAME)
                    .value(nxt.getDeviceName())
                    .key(CData.JSON_RESPONSE_BATTERYLEVEL)
                    .value((double)nxt.getDeviceBateryLevel())
                    .key(CData.JSON_RESPONSE_DEVICEADDRESS)
                    .value(nxt.getDeviceBluetoothAddress())
                    .key(CData.JSON_RESPONSE_FIRMWAREVERSION)
                    .value(nxt.getDeviceFirmwareVersion())
                    .key(CData.JSON_RESPONSE_VOLUME)
                    .value(nxt.getVolume())
                    .key(CData.JSON_RESPONSE_STATUS)
                    .value("OK")
                    .key(CData.JSON_RESPONSE_TIME)
                    .value(((double)cont)/10d)
                    .endObject().toString();
            nxt.disconnect();
        } catch (Exception ex) {
            String msg = "Can not get device information";
            Logger.getLogger(getClass().getName()).log(Level.WARNING, msg, ex);
            result = "{ \"status\" : \"" + msg + "\" }";
            nxt = null;
        }
        return result;
    }
}
