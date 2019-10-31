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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kieran, Monica, Ana
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
    Accion accion_anterior; //Acción anterior
    String clave;   //Clave que hay que enviar con cada comando que se envía

    int min_x;
    int max_x;
    int min_y;
    int max_y;
    int min_z;
    int max_z;

    int unidades_updown; //Unidades que consume las bajadas y subidas
    double consumo_fuel; //Consumo de fuel por movimiento

    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
        radar = new int[tamanio_radar][tamanio_radar];
        gonio = new Gonio();
        gps = new GPS();
        min_x = 0;
        min_y = 0;
        min_z = 0;
        max_z = 255;
        unidades_updown = 5;
        consumo_fuel = 0.5;
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
        if(gonio.angulo>=337.5 || gonio.angulo<22.5)
            return moveN;
        return logout;

    }


    /**
    *
    * @author Ana, Kieran
    * Se comprueba si se puede realizar la acción más prometedora
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
       //No quería quitar esta`parte
      //if(necesitaRepostar() || comprobarMeta()) { // Se comprueba si se puede repostar o se ha llegado a la meta
      if(necesitaRepostar(accion) || comprobarMeta()) { // Se comprueba si se puede repostar o se ha llegado a la meta
          if(gps.z == radar[5][5]){
            return ((comprobarMeta())?logout:refuel);
          }
          return moveDW;
      }

      if(radar[x][y]==0)
          return logout;
      else if(radar[x][y] <= gps.z) //No hay obstaculos y se puede realizar la acción más prometedora
        return accion;
      else if(radar[x][y] > gps.z && (gps.z+5 <= max_z)) //Hay obstaculos y necesitamos superarlos
        return moveUP;

      return logout;
    }

    /**
    *
    * @author Ana, Kieran
    * Calcula cuantos movimientos de bajada vamos a necesitar para llegar al suelo
    */
    private int unidadesBajada(){
      int movs;
      movs = gps.z - radar[5][5]; //Cada bajada conlleva 5 unidades. Calculamos en funcion de la altura cuantos movimientos necesitamos para llegar al suelo
      return movs;
    }

    
    /**
    *
    * @author Pablo
    */
    private boolean necesitaRepostar(Accion accion){//Toma como parámtro la acción inmediatamente anterior, para comprobar la dirección a la que se dirige
        //Suponiendo que seguirá la dirección que tomo en el último movimiento, el agente comprueba si tiene combustible suficiente para repostar en cualquiera de los 5 puntos más próximos en esa dirección, si están demasiado altos o demasiado bajos no podrá repostar
        boolean necesita=true;
        int i,j;
        switch(accion) {
        case moveNW:
            i=0;
            j=0;
            while (necesita==true&&i<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            ++i;
            ++j;
            }
            break; //Comprueba si podría repostar si continuase en dirección NW
        case moveN: 
            i=0;
            j=5;
            while (necesita==true&&i<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            ++i;
            }
            break; //Comprueba si podría repostar si continuase en dirección N
        case moveNE: 
            i=0;
            j=10;
            while (necesita==true&&i<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            ++i;
            --j;
            }
            break; //Comprueba si podría repostar si continuase en dirección NE
        case moveW: 
            i=5;
            j=0;
            while (necesita==true&&j<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            ++j;
            }
            break; //Comprueba si podría repostar si continuase en dirección W
        case moveE: 
            i=5;
            j=10;
            while (necesita==true&&j>5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            --j;
            }
            break; //Comprueba si podría repostar si continuase en dirección E
        case moveSW: 
            i=10;
            j=0;
            while (necesita==true&&j<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            --i;
            ++j;
            }
            break; //Comprueba si podría repostar si continuase en dirección SW
        case moveS: 
            i=10;
            j=5;
            while (necesita==true&&i>5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            --i;
            }
            break; //Comprueba si podría repostar si continuase en dirección S
        case moveSE: 
            i=10;
            j=10;
            while (necesita==true&&i<5){
            if (((fuel>(6*consumo_fuel)+((gps.z-radar[i][j])*consumo_fuel))||(fuel>(6*consumo_fuel)+((radar[i][j]-gps.z)*consumo_fuel)))&&(radar[i][j]>=max_z));
                necesita=false;
            --i;
            --j;
            }
            break; //Comprueba si podría repostar si continuase en dirección SE
      }
        if(fuel<=(unidadesBajada()*consumo_fuel)+consumo_fuel)necesita=false;//Aunque no tenga combustible suficiente para seguir avanzando, si no tiene suficiente para bajarm que siga hacia adelante
        return necesita;
    }
    
    
    /**
    *
    * @author Kieran, Ana
    */
    /*
    private boolean necesitaRepostar(){ //Mira si hace falta repostar el agente, 5 uds de altura gasta 0.5 uds de fuel, 1u altura = 0.1u fuel
       return (fuel <= (unidadesBajada() * consumo_fuel) + 2*consumo_fuel*unidades_updown); //En la altura a la que estamos el fuel necesario para llegar al suelo sin problema.
       
    }*/

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
        a = mensaje.get("perceptions").asObject();


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
    private JsonObject escuchar(){
        return escuchar(false);
    }
    
    /**
    *
    * @author Kieran
    */
    private JsonObject escuchar(boolean echo) {
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return null;
        }
        String mensaje = inbox.getContent();
        if(echo) System.out.println("Mensaje recibido:\n" + mensaje);
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

        JsonObject respuesta = escuchar(true);
        if(validarRespuesta(respuesta)){
            respuesta = escuchar(true);
            guardarTraza(respuesta);
        }
    }
    
    /**
    *
    * @author Kieran
    */
    private void guardarTraza(JsonObject respuesta){
        FileOutputStream fos = null;
        try {
            JsonArray ja = respuesta.get("trace").asArray();
            byte data[] = new byte [ja.size()];
            for(int i=0; i<data.length;i++){
                data[i] = (byte) ja.get(i).asInt();
            }
            fos = new FileOutputStream("trace.png");
            fos.write(data);
            fos.close();
            System.out.println("Traza guardada");
        } catch (Exception ex) {
            Logger.getLogger(AgenteSimple.class.getName()).log(Level.SEVERE, null, ex);
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

            respuesta = escuchar(true);
            JSONDecode(respuesta);

            accion_anterior = command;
            command = comprobarAccion(); //funcion de utilidad/comprobar mejor casilla aqui

            System.out.println(command.toString());

            if(goal || command == logout){
                break; //Hemos acabado
            }

            mensaje = JSONEncode(); //codificar respuesta JSON aqui
            comunicar("Izar", mensaje);
            respuesta = escuchar(true);
        }

        logout();
    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
