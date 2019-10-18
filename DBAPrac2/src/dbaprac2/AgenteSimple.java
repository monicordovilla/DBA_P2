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
     * @author Pablo
     *
     * //Función para refuel
     * //Depende de la dirección
     *
            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[0][0]>0)||(fuel<=5&&gps.z-radar[4][4]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;


            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[0][5]>0)||(fuel<=5&&gps.z-radar[4][5]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;

            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[0][10]>0)||(fuel<=5&&gps.z-radar[4][6]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;


            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[5][0]>0)||(fuel<=5&&gps.z-radar[5][4]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;

            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[5][10]>0)||(fuel<=5&&gps.z-radar[5][6]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;


            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[10][0]>0)||(fuel<=5&&gps.z-radar[6][4]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;

            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[5][10]>0)||(fuel<=5&&gps.z-radar[5][6]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;


            else if(fuel<=10)
                if((fuel<=10&&gps.z-radar[10][10]>0)||(fuel<=5&&gps.z-radar[6][6]>0)||(fuel==0&&radar[5][5]==gps.z))
                    return refuel;

     //Condición para bajar
     //Dependede de dirección


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[4][4]<=radar[5][5]) && (radar[3][3]<=radar[5][5]) && (radar[2][2]<=radar[5][5]) && (radar[1][1]<=radar[5][5]) && (radar[0][0]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[5][4]<=radar[5][5]) && (radar[5][3]<=radar[5][5]) && (radar[5][2]<=radar[5][5]) && (radar[5][1]<=radar[5][5]) && (radar[5][0]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[6][4]<=radar[5][5]) && (radar[7][3]<=radar[5][5]) && (radar[8][2]<=radar[5][5]) && (radar[9][1]<=radar[5][5]) && (radar[10][0]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[5][4]<=radar[5][5]) && (radar[5][3]<=radar[5][5]) && (radar[5][2]<=radar[5][5]) && (radar[5][1]<=radar[5][5]) && (radar[5][0]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[6][5]<=radar[5][5]) && (radar[7][5]<=radar[5][5]) && (radar[8][5]<=radar[5][5]) && (radar[9][5]<=radar[5][5]) && (radar[10][5]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[4][6]<=radar[5][5]) && (radar[3][7]<=radar[5][5]) && (radar[2][8]<=radar[5][5]) && (radar[1][9]<=radar[5][5]) && (radar[0][10]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[5][6]<=radar[5][5]) && (radar[5][7]<=radar[5][5]) && (radar[5][8]<=radar[5][5]) && (radar[5][9]<=radar[5][5]) && (radar[5][10]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;


            else if(gonio.distancia>fuel)
                if((gps.z-radar[5][5]==fuel) && (radar[6][6]<=radar[5][5]) && (radar[7][7]<=radar[5][5]) && (radar[8][8]<=radar[5][5]) && (radar[9][9]<=radar[5][5]) && (radar[10][10]<=radar[5][5]) && (gps.z-5 >= 0))
                    return moveDW;
     */

    /**
    *
    * @author Ana
    */
    private Accion comprobarAccion(){
      Accion accion = siguienteAccion();
      int x, y;

      switch(accion) {
        case moveNW: x = 4; y = 4; break; //Comprobación del movimiento NW
        case moveN: x = 4; y = 5; break;//Comprobación del movimiento N
        case moveNE: x = 4; y = 6; break; //Comprobación del movimiento NE
        case moveW: x = 5; y = 4; break; //Comprobación del movimiento W
        case moveE: x = 5; y = 6; break; //Comprobación del movimiento E
        case moveSW: x = 6; y = 4; break; //Comprobación del movimiento SW
        case moveS: x = 6; y = 5; //Comprobación del movimiento S
        case moveSE: x = 6; y = 6; //Comprobación del movimiento SE
      }

      if(radar[x][y]==0);
          //Mensaje de error
      else if(radar[x][y] <= gps.z)
        return siguienteAccion();
      else if(radar[x][y] > gps.z && (gps.z+5 <= 255))
        return moveUP;
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
        JsonObject gps_json = new JsonObject();
        gps_json.add("x", gps.x);
        gps_json.add("y", gps.y);
        gps_json.add("z", gps.z);
        a.add("gps", gps_json);

        a.add("fuel", fuel);

        JsonObject gonio_json = new JsonObject();
        gonio_json.add("distance", gonio.distancia);
        gonio_json.add("angle", gonio.angulo);
        a.add("gonio", gonio_json);

        JsonArray radar_json = new JsonArray();
        for(int i=0; i<radar.length; i++){
            for(int j=0; j<radar.length; j++){
                radar_json.add(radar[i][j]);
            }
        }
        a.add("radar", radar_json);

        //Serializar el objeto en un String
        String resultado = a.toString();

        return resultado;
    }

    /**
    *
    * @author Monica
    */
    private void JSONDecode(String mensaje){//Decodifidar variables en JSON
        //Obtiene la informacion de GPS, fuel, gonio, radar, goal y crash
        JsonObject a = new JsonObject();

        //Parsear el Strin original y almacenarlo en un objeto
        a = Json.parse(mensaje).asObject();

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
        crash = a.get("crash").asBoolean();
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
    * @author Kieran, Ana
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
                accion = comprobarAccion();

            }

        }

    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
