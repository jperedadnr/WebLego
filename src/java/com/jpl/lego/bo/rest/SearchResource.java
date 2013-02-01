package com.jpl.lego.bo.rest;

import data.CData;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnectionManager;
import lejos.pc.comm.NXTInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * REST Web Service
 *
 * @author José Pereda Llamas
 * Created on 10-nov-2012 - 15:34:27
 */
@Path("search")
public class SearchResource {

    /**
     * Retrieves representation of an instance of com.jpl.lego.bo.rest.SearchResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        
        NXTConnectionManager man= new NXTConnectionManager();
        
        NXTInfo[] search = man.search();
        
        JSONObject json = null;
        try{
            
            JSONArray arr=new JSONArray();        

            for(NXTInfo info:search){
                System.out.println("info: "+info.deviceAddress+" "+info.name+" "+
                    (info.protocol==NXTCommFactory.USB?"USB":"BT")+" "+info.connectionState.name());
                JSONObject element = new JSONObject();
                element.put(CData.JSON_RESPONSE_DEVICENAME, info.name);
                element.put(CData.JSON_RESPONSE_DEVICEADDRESS, info.deviceAddress);
                if(info.protocol==NXTCommFactory.USB){
                    element.put(CData.JSON_RESPONSE_PROTOCOL, "USB");
                } else if(info.protocol==NXTCommFactory.BLUETOOTH){
                    element.put(CData.JSON_RESPONSE_PROTOCOL, "BLUETOOTH");
                }
                arr.put(element);
            }
            json = new JSONObject();
            json.put(CData.JSON_RESPONSE_DEVICES, arr);
        } catch(JSONException jse){
            return "{}";
        }
        return json.toString();
    }
}
