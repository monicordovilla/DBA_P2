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
import static dbaprac2.Accion.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

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
    GPS gps = new GPS();
    Gonio gonio =new Gonio();
    float fuel;
    int[][] radar;
    boolean goal;
    String status;
    Accion command; //Siguiente accion que tiene que hacer el agente
    String clave;   //Clave que hay que enviar con cada comando que se envía

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
        if(gonio.angulo>=337.5 && gonio.angulo<22.5)
            return moveN;
        return logout;

    }


    /**
    *
    * @author Ana
    */
    private Accion comprobarAccion(){
      Accion accion = siguienteAccion();
      int x=5, y=5;

      switch(accion) {
        case moveNW: x = 4; y = 4; break; //Comprobación del movimiento NW
        case moveN: x = 4; y = 5; break;//Comprobación del movimiento N
        case moveNE: x = 4; y = 6; break; //Comprobación del movimiento NE
        case moveW: x = 5; y = 4; break; //Comprobación del movimiento W
        case moveE: x = 5; y = 6; break; //Comprobación del movimiento E
        case moveSW: x = 6; y = 4; break; //Comprobación del movimiento SW
        case moveS: x = 6; y = 5; break;//Comprobación del movimiento S
        case moveSE: x = 6; y = 6; break;//Comprobación del movimiento SE
      }

      if(radar[x][y]==0)
          return logout;
      else if(radar[x][y] <= gps.z)
        return accion;
      else if(radar[x][y] > gps.z && (gps.z+5 <= 255))
        return moveUP;
      
      return logout;
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
    private String JSONEncode(){ //Codificar variable en JSON
        //Pasa la informacion de GPS, fuel, gonio y radar
        JsonObject a = new JsonObject();

        //Añadimos pares <clave, valor>
        a.add("command", command.toString());
        a.add("key", clave);

        //Serializar el objeto en un String
        String resultado = a.toString();

        return resultado;
    }

    /**
    *
    * @author Monica
    */
    private void JSONDecode(String mensaje){//Decodifidar variables en JSON
        //Obtiene la informacion de GPS, fuel, gonio, radar, goal y status
        JsonObject a;

        //Parsear el Strin original y almacenarlo en un objeto
        a = Json.parse(mensaje).asObject().get("perceptions").asObject();
        

        //Extraer los valores asociados a cada clave
        gps.x = a.get("gps").asObject().get("x").asInt();
        gps.y = a.get("gps").asObject().get("y").asInt();
        gps.z = a.get("gps").asObject().get("z").asInt();

        fuel = a.get("fuel").asFloat();
        
        gonio.angulo = a.get("gonio").asObject().get("angle").asFloat();
        gonio.distancia = a.get("gonio").asObject().get("distance").asFloat();
        

        JsonArray vector_radar = a.get("radar").asArray();
        for(int i=0; i<radar.length; i++){
            for(int j=0; j<radar.length; j++){
                radar[i][j] = vector_radar.get(j+i*radar.length).asInt();
            }
        }
        

        goal = a.get("goal").asBoolean();
        status = a.get("status").toString();
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
    * @author Monica
    */
    private String MensajeInicialJSON(String mapa){
        JsonObject a = new JsonObject();
        //Iniciamos y mandamos el mapa que queremos
        a.add("command", "login");
        a.add("map", mapa);

        //Solicitamos los sensores de los que queremos informacion
        a.add("radar", true);
        a.add("elevation", false);
        a.add("magnetic", true);
        a.add("gps", true);
        a.add("fuel", true);
        a.add("gonio", true);

        //Mandamos nuestro usuario y contraseña
        a.add("user", "Ibbotson");
        a.add("password", "oLARuosE");

        String mensaje = a.toString();
        return mensaje;
    }

    /**
    *
    * @author Kieran, Ana, Celia
    */
    @Override
    public void execute() {
        String mapa = seleccionarMapa();
        
        //codificar el mensaje inicial JSON aqui
        String mensaje = MensajeInicialJSON(mapa);
        comunicar("Izar", mensaje);
        
        String respuesta = escuchar();
        JsonObject a = Json.parse(respuesta).asObject();
        
        clave = a.get("key").asString();
        
        
        while(a.get("result").asString().equals("ok"))
        {
            //comprobar si se esta en la meta aqui
            //funcion de utilidad/comprobar mejor casilla aqui
            //codificar respuesta JSON aqui
            
            respuesta = escuchar();
            JSONDecode(respuesta);
            
            if(!goal)
                command = comprobarAccion();
            else
                command = logout;
                
          
            
            System.out.println(command.toString());
            
            mensaje = JSONEncode();
            comunicar("Izar", mensaje);
            respuesta = escuchar();
            a = Json.parse(respuesta).asObject();
        }
        
        respuesta = escuchar();

    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
