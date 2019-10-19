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

/**
 *
 * @author Kieran, Monica
 */
public class AgenteSimple extends SuperAgent{

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
    String status;
    Accion command; //Siguiente accion que tiene que hacer el agente
    String clave;   //Clave que hay que enviar con cada comando que se envía
    
    int min_x;
    int max_x;
    int min_y;
    int max_y;
    int min_z;
    int max_z;

    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
        radar = new int[tamanio_radar][tamanio_radar];
        gonio = new Gonio();
        gps = new GPS();
        min_x = 0;
        min_y = 0;
        min_z = 0;
        max_z = 255;
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

//METODOS DE EVALUACIÓN: La funcionalidad inteligente del agente, para decidir que hacer

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
    * @author Ana, Kieran
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
      
      if(necesitaRepostar() || comprobarMeta()) {
          if(gps.z == radar[5][5]){
            return (comprobarMeta())?refuel:logout;
          }
          return moveDW;
      }

      if(radar[x][y]==0)
          return logout;
      else if(radar[x][y] <= gps.z)
        return accion;
      else if(radar[x][y] > gps.z && (gps.z+5 <= max_z))
        return moveUP;
      
      return logout;
    }

    /**
    *
    * @author Kieran
    */
    private boolean necesitaRepostar(){ //Mira si hace falta repostar el agente, 5 uds de altura gasta 0.5 uds de fuel, 1u altura = 0.1u fuel
       return (fuel < max_z/10.0); //Provisional, max_z es la altura maxima y por tanto la maxima distancia del suelo al que puede estar el agente.
    }
    
    /**
    *
    * @author Celia, Kieran
    */
    private boolean comprobarMeta(){
        return gonio.distancia<=1;
    }

//METODOS DE JSON: Codifican y descodifican los mensajes en formato JSON para facilitar el manejo de los datos recibidos

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
    * @author Monica, Kieran
    */
    private void JSONDecode(JsonObject mensaje){//Decodifidar variables en JSON
        //Obtiene la informacion de GPS, fuel, gonio, radar, goal y status
        JsonObject a;
        a = mensaje;
        

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
    private void JSONDecode_Inicial(JsonObject mensaje){//Decodifica el primer mensaje con atributos del mapa
        max_x = mensaje.get("dimx").asInt();
        max_y = mensaje.get("dimy").asInt();
        min_z = mensaje.get("min").asInt();
        max_z = mensaje.get("max").asInt();
        clave = mensaje.get("key").asString();
    }
    /**
    *
    * @author Monica
    */
    private String JSONEncode_Inicial(String mapa){
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
    * @author Kieran 
    */
    private boolean validarRespuesta(JsonObject respuesta){
        boolean valido = respuesta.get("result").asString().equals("ok");
        if(!valido){
            System.out.println("Error in response to '" + respuesta.get("in-reply-to").asString() + "': " + respuesta.get("result").asString());
        }
        return valido;
    }

//METODOS DE COMUNICACIÓN: Mandan mensajes al agente en el lado del servidor
    
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
    private JsonObject escuchar() {
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return null;
        }
        String mensaje = inbox.getContent();
        System.out.println("Mensaje recibido:\n" + mensaje);
        return Json.parse(mensaje).asObject();
    }
    
    /**
    *
    * @author Kieran, Celia
    */
    private void logout() {
        command = logout;
        String mensaje = JSONEncode(); //codificar respuesta JSON aqui
        comunicar("Izar", mensaje);
        
        JsonObject respuesta = escuchar();
        if(validarRespuesta(respuesta)){
            respuesta = escuchar();
            System.out.println("Traza recibido: " + respuesta.get("trace").asString());
        }
    }

//METODOS DE SUPERAGENT: Métodos sobreescritos y heredados de la clase SuperAgent
    
    @Override
    public void init() { //Opcional
        System.out.println("\nInicializado");
    }

    /**
    *
    * @author Kieran, Ana, Celia
    */
    @Override
    public void execute() {
        String mapa = seleccionarMapa();
        
        //codificar el mensaje inicial JSON aqui
        String mensaje = JSONEncode_Inicial(mapa);
        comunicar("Izar", mensaje);
        
        JsonObject respuesta = escuchar();
        
        JSONDecode_Inicial(respuesta);        
        
        while(validarRespuesta(respuesta))
        {
            respuesta = escuchar();
            JSONDecode(respuesta);
            
            command = comprobarAccion(); //funcion de utilidad/comprobar mejor casilla aqui
            if(goal || command == logout){
                break; //Hemos acabado
            }
            
            System.out.println(command.toString());
            
            mensaje = JSONEncode(); //codificar respuesta JSON aqui
            comunicar("Izar", mensaje);
            respuesta = escuchar();
        }
        
        logout();
    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
