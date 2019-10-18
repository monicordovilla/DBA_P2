/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author Kieran
 */
public class DBAPrac2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AgentsConnection.connect(
                "isg2.ugr.es",  //Host: localhost si se quiere probar en nuestra propia maquina
                6000,           //Puerto: por defecto 5672
                "Practica2",        //VHOST
                "Ibbotson",        //Usuario
                "oLARuosE",       //Contraseña
                false           //SSL
        );
        
        AgenteSimple Smith;
        
        try {
            Smith = new AgenteSimple(new AgentID("GI_tIgnore"));
        } catch (Exception ex) {
            System.err.println("already on the platform, goofy");
            return;
        }
        
        Smith.start();
    }
    
}