/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpl.lego.bo.rest;

import com.jpl.lego.bo.nxt.LegoNXT;
import data.CData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
@Path("motor")
public class MotorResource {

    
    private LegoNXT nxt = null;
    /**
     * Retrieves representation of an instance of com.jpl.lego.bo.rest.MotorResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("Left") @DefaultValue("A") String motorLeft,
                          @QueryParam("Right") @DefaultValue("C") String motorRight,
                          @QueryParam("Test") @DefaultValue("0") int test) {
        
        String result = null;

        if (nxt == null) {
            nxt = LegoNXT.getSingleton();
        }

        if (! nxt.isInitiliazed()) {
            try {
                nxt.initialize(motorLeft,motorRight,2,4);

            } catch (Exception e ) {

                String msg = "MUST send and initialization order first";
                Logger.getLogger(MotorResource.class.getName()).log(Level.WARNING, msg);
                result = "{ \"status\" : \"" + msg + "\" }";
                return result;
            }
        }
        
        if(test==1){
            int cont=0;
            do{
                nxt.travel(100,true);
                while (nxt.isMoving()) {
                  if (nxt.getTouchValue(1)) {
                        nxt.stop();
                        nxt.rotateLeft();
                        nxt.travel(-10);
                        nxt.rotateRight();
                        cont=cont+1;
                    }
                }
            }while(cont<3);
            
            
        }
            /*
             * {"initialization":{"wheelDiameter":"4.96","wheelDistance":"13.0"}, "commands":[{"command":"travel","distance":3}, {"command":"stop"},{"command":"forward","speed":10},{"command":"disconnect"}]}
             */
        try {

            //robot.connect();

            result = new JSONStringer()
                    .object()
                    .key(CData.JSON_RESPONSE_STATUS)
                    .value("OK")
                    .endObject()
                    .toString();

            //robot.disconnect();

        } catch (Exception ex) {
            String msg = "Can not read Pilot value";
            Logger.getLogger(MotorResource.class.getName()).log(Level.WARNING, msg, ex);
            result = "{ \"status\" : \"" + msg + "\" }";
            nxt = null;
        }

        return result;
    }

    /**
     * PUT method for updating or creating an instance of MotorResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putJson(String content) {
        
    }
}
