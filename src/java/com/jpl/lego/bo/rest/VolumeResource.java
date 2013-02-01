package com.jpl.lego.bo.rest;

import com.jpl.lego.bo.nxt.LegoNXT;
import data.CData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

/**
 * REST Web Service
 *
 * @author José Pereda Llamas
 * Created on 10-nov-2012 - 15:34:27
 */
@Path("volume")
public class VolumeResource {

    private LegoNXT nxt = null;
    /**
     * PUT method for updating or creating an instance of VolumeResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public String putJson(String content) {
        String result = "{ \"status\" : \"OK\" }";

        try {
            JSONObject json = (JSONObject)new JSONTokener(content).nextValue();

            nxt = LegoNXT.getSingleton();
                     
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
                Logger.getLogger(VolumeResource.class.getName()).log(Level.INFO, "Command = {0}", json.toString());

                int value = (int)json.getInt(CData.JSON_COMMAND_VOLUME);
                Logger.getLogger(VolumeResource.class.getName()).log(Level.INFO, "Command = {0}", value);
          
                nxt.connect(json.getString(CData.JSON_RESPONSE_DEVICENAME),
                            json.getString(CData.JSON_RESPONSE_PROTOCOL));
                if(!nxt.isLegoConnected()){
                    return "{ \"status\" : \"Device not connected\" }";
                }
                int vol=nxt.setVolume((byte)(value));
                result = "{ \"status\" : \"OK\",\"volume\" : "+vol+" }";
            } catch (JSONException e) {
                String msg = "No command to execute";
                Logger.getLogger(VolumeResource.class.getName()).log(Level.INFO, msg);
                result = "{ \"status\" : \"" + msg + "\",\"volume\" : -1 }";
            }

        } catch (Exception ex) {
            String msg = "Error whil eparsing JSON request - or - communicating with NXT";
            Logger.getLogger(VolumeResource.class.getName()).log(Level.SEVERE, msg, ex);
            result = "{ \"status\" : \"" + msg + "\",\"volume\" : -1 }";
        } 

        nxt.disconnect();

        return result;
    }
}
