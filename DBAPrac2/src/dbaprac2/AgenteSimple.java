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

/**
 *
 * @author Kieran
 */
public class AgenteSimple extends SuperAgent{
    
    //  Añadir string para guardar el mensaje anteriormente recibido aqui
    /*
        Añadir estructuras de datos para info de sensores aqui
    */
    
    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
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
        //codificar el mensaje inicial JSON aqui
        comunicar("nombre", "mensaje");
        while(true/*si el mensaje anterior es valido*/)
        {
            //comprobar si se esta en la meta aqui
            //funcion de utilidad/comprobar mejor casilla aqui
            //codificar respuesta JSON aqui
            comunicar("nombre", "mensaje");
        }
    }
    
    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
