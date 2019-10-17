/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac2;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import DBA.SuperAgent;
import java.util.Scanner;
import com.eclipsesource.json.JsonObject;
import static dbaprac2.Accion.*;
        
/**
 *
 * @author Kieran, Monica
 */
public class AgenteSimple extends SuperAgent{
    
    //  Añadir string para guardar el mensaje anteriormente recibido aqui
   
    private class GPS {
        public int x;
        public int y;
        public int z;
        
        GPS() {
            x = -1; y = -1; z = -1;
        }
    }
    private class Gonio {
        public float angulo;
        public float distancia;
    }
    
    static int tamanio_radar = 11;
    GPS gps;
    Gonio gonio;
    float fuel;
    int[][] radar;
    boolean goal;
    boolean crash;
    
    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
        radar = new int[tamanio_radar][tamanio_radar];
    }
    
    /**
    *
    * @author Kieran
    */
    private String seleccionarMapa(){
        System.out.println("Inserte el nombre del mapa a probar:");
        Scanner s = new Scanner(System.in);
        String mapa_seleccionado = s.nextLine();
        return mapa_seleccionado;
    }
    
    /**
    *
    * @author Celia
    */
    private Accion siguienteAccion(){
        if(gonio.angulo>=22.5 && gonio.angulo<67.5)
            return moveNE;
        if(gonio.angulo>=67.5 && gonio.angulo<112.5)
            return moveE;
        if(gonio.angulo>=112.5 && gonio.angulo<157.5)
            return moveSE;
        if(gonio.angulo>=157.5 && gonio.angulo<202.5)
            return moveS;
        if(gonio.angulo>=202.5 && gonio.angulo<247.5)
            return moveSW;
        if(gonio.angulo>=247.5 && gonio.angulo<292.5)
            return moveW;
        if(gonio.angulo>=292.5 && gonio.angulo<337.5)
            return moveNW;
        return moveN;
        
    }    

        /**
    *
    * @author Celia
    */
    private boolean comprobarMeta(){
        return gonio.distancia==0;
    }  
    
    /**
    *
    * @author Kieran, Monica
    */
    private String JSONEncode(){
        JsonObject a = new JsonObject();
        return a.toString();
    }
    
    /**
    *
    * @author Monica
    */
    private void JSONDecode(String mensaje){
        JsonObject a = new JsonObject();
    }
    
    
    /**
    *
    * @author Kieran
    */
    private void comunicar(String nombre, String mensaje) {
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(nombre));
        outbox.setContent(mensaje);
        this.send(outbox);
    }
    
    /**
    *
    * @author Kieran
    */
    private String escuchar() {
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return "ERROR";
        }
        String mensaje = inbox.getContent();
        System.out.println("Mensaje recibido:\n" + mensaje);
        return mensaje;
    }
    
    @Override
    public void init() { //Opcional
        System.out.println("\nInicializado");
    }
    
    /**
    *
    * @author Kieran
    */
    @Override
    public void execute() {
        String mapa = seleccionarMapa();
        Accion accion;
        //codificar el mensaje inicial JSON aqui
        comunicar("nombre", "mensaje");
        while(true/*si el mensaje anterior es valido*/)
        {
            //comprobar si se esta en la meta aqui
            //funcion de utilidad/comprobar mejor casilla aqui
            //codificar respuesta JSON aqui
            comunicar("nombre", "mensaje");
           
            if(!comprobarMeta()){
                 accion=siguienteAccion();
            }
                
        }

    }
    
    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}