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
import java.util.Arrays;
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
    boolean[][] memoria;
    String clave;   //Clave que hay que enviar con cada comando que se envía
    
    boolean modo_mano_dcha;
    
    boolean hecho_logout; //si se ha hecho 

    //dimensiones del mundo en el que se ha logueado, se asigna valor en el JSONDecode_Inicial
    int max_x;
    int max_y;
    //ahora mismo no se usa, por si queremos evitar bordes
    int min_x;
    int min_y;
    //altura mínima y máxima a las que el drone puede volar, se asigna valor en el JSONDecode_Inicial
    int min_z;
    int max_z;
    int pasos = 0;
    int max_pasos = 750;

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
        modo_mano_dcha = false;
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
    * @author Kieran
    * Comprueba si se puede mover a la casilla a la que nos llevaria sigAccion
    */
    private boolean puedeMover(Accion sigAccion) {
        int x=5,y=5,z=0;
        switch(sigAccion) {
                case moveNW: x = 4; y = 4; break; //Comprobación del movimiento NW
                case moveN: x = 4; y = 5; break;//Comprobación del movimiento N
                case moveNE: x = 4; y = 6; break; //Comprobación del movimiento NE
                case moveW: x = 5; y = 4; break; //Comprobación del movimiento W
                case moveE: x = 5; y = 6; break; //Comprobación del movimiento E
                case moveSW: x = 6; y = 4; break; //Comprobación del movimiento SW
                case moveS: x = 6; y = 5; break;//Comprobación del movimiento S
                case moveSE: x = 6; y = 6; break;//Comprobación del movimiento SE
                case moveDW: z = -5; break;
                case moveUP: z = 5; break;
              }
        return(gps.z+z >= radar[x][y] && gps.z+z <= max_z);
    }
    
    
    /**
    *
    * @author Monica, Kieran
    * Comprueba si se puede subir por encima de la casilla a la que nos llevaría sigAccion
    */
    private boolean puedeSubir(Accion sigAccion){
        boolean sube = true;
        //max_z = 180; //BORRAR DESPUES
        int x = 5;
        int y = 5;
        //System.out.println("Esto sale en pantalla\n");
        //si en la siguiente accion, la altura del drone es mayor es un obstáculo
                
        switch(sigAccion) {
            case moveNW: x = 4; y = 4; break; //Comprobación del movimiento NW
            case moveN: x = 4; y = 5; break;//Comprobación del movimiento N
            case moveNE: x = 4; y = 6; break; //Comprobación del movimiento NE
            case moveW: x = 5; y = 4; break; //Comprobación del movimiento W
            case moveE: x = 5; y = 6; break; //Comprobación del movimiento E
            case moveSW: x = 6; y = 4; break; //Comprobación del movimiento SW
            case moveS: x = 6; y = 5; break;//Comprobación del movimiento S
            case moveSE: x = 6; y = 6; break;//Comprobación del movimiento SE
          }
            
            
        if(radar[x][y] > max_z){
            sube = false;
        }
        
        return sube;
    }
    
    /**
    *
    * @author Celia, Monica, Kieran
    * siguienteAccion() renombrado
    * Copiado-pegado de rodearObstaculoAccion, ya que este simplemente selecciona la mejor opcion sin contar los invalidos.
    * Se ha de tener en cuenta de que rodearObstaculoAccion solo se lanza cuando supere la altura maxima asi que se han tenido que ajustar un par de cosas
    */
    private Accion siguienteDireccion(){
        final int dirs = 8;
        final int MAX = 999;
        final float grados_entre_dir = 45;
        
        boolean validos[] = {true,true,true,true,true,true,true,true};
        //System.out.println(accion_anterior.value);
        for(int i = 0; i < dirs; i++) { //Eliminamos direcciones imposibles de la lista. Estos incluyen aquellos que ya hemos visitado, y los que no podemos ir a, ni subir para llegar a
            if((!puedeMover(Accion.valueOfAccion(i)) && !puedeSubir(Accion.valueOfAccion(i))) /*|| estaEnMemoria(Accion.valueOfAccion(i))*/) validos[i] = false;
            //if(accion_anterior.value < 8 && (accion_anterior.value+4)%8 == i) validos[i] = false;
        }
        System.out.println(Arrays.toString(validos));
        float diff_menor = MAX;
        int indice_menor = MAX;
        for(int i = 0; i < 8; i++) {
            if(!validos[i]) continue;
            float dist_real = Math.abs(gonio.angulo-(i*grados_entre_dir))%360;
            dist_real = dist_real > 180 ? 360-dist_real : dist_real;
            if(dist_real < diff_menor){
                indice_menor = i;
                diff_menor = dist_real;
                
                System.out.println("angulo: " + gonio.angulo + "accion escogido: " + Accion.valueOfAccion(i));
                
            }
        }
        if(indice_menor == MAX) return logout;
        
        return Accion.valueOfAccion(indice_menor);
    }

    /**
    *
    * @author Ana, Celia
    * Se comprueba si ya hemos pasado por la posición a la que nos lleva la siguiente acción, devuelve TRUE si no se ha visitado ya
    */
    private boolean estaEnMemoria(Accion accion)
    {
      int x, y;

      switch(accion) {
        case moveNW: y = gps.y-1; x = gps.x-1; break; //Comprobación del movimiento NW
        case moveN: y = gps.y-1; x = gps.x; break; //Comprobación del movimiento N
        case moveNE: y = gps.y-1; x = gps.x+1; break; //Comprobación del movimiento NE
        case moveW: y = gps.y; x = gps.x-1; break; //Comprobación del movimiento W
        case moveE: y = gps.y; x = gps.x+1; break; //Comprobación del movimiento E
        case moveSW: y = gps.y+1; x = gps.x-1; break; //Comprobación del movimiento SW
        case moveS: y = gps.y+1; x = gps.x; break; //Comprobación del movimiento S
        case moveSE: y = gps.y+1; x = gps.x+1; break; //Comprobación del movimiento SE
        default: return false;
      }
      
      if(x < 0 || y < 0 || x > max_x || y > max_y) return true; //Para no salirse de la matriz

      return memoria[x][y] == true;
    }

    /**
    *
    * @author Ana, Kieran, Monica
    * Se comprueba si se puede realizar la acción más prometedora
    */
    private Accion comprobarAccion(){
      Accion accion;
              
      if(necesitaRepostar() || comprobarMeta()) { // Se comprueba si se necesita repostar o se ha llegado a la meta
          if(gps.z == radar[5][5]){
            return ((comprobarMeta())?logout:refuel);
          }
          return moveDW;
      }
      
      accion = siguienteDireccion(); //Escogemos la direccion en la que queremos ir
      if(accion_anterior.value < 8 && (accion_anterior.value+4)%8 == accion.value) { //Si estamos atrapado en un bucle, ACTIVAMOS MANO DERECHA
          modo_mano_dcha = true;
          return reglaManoDerecha();
      }
      
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

      if(radar[x][y]==0) //Si la hemos liado, salir
          return logout;
      else if(radar[x][y] <= gps.z) //Estamos a la altura de la celda a la que queremos ir o superor
        return accion;
      else if(radar[x][y] > gps.z && (gps.z+5 <= max_z) && puedeSubir(accion)) //La celda a la que queremos ir esta a una altura superior y podemos llegar a ella
        return moveUP;

      return logout;
    }
    

    /**
    *
    * @author Ana, Celia, Kieran
    */
    
    private Accion reglaManoDerecha(){
        //INSERTAR CODIGO AQUI
        if(false) { //placeholder-cambiar por condicion de desactivación despues
            modo_mano_derecha = false:
        }
        return moveDW; //placeholder - borrar ahora
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
    * @author Kieran, Ana
    */
    private boolean necesitaRepostar(){ //Mira si hace falta repostar el agente, 5 uds de altura gasta 0.5 uds de fuel, 1u altura = 0.1u fuel
       return (fuel <= (unidadesBajada() * consumo_fuel) + 2*consumo_fuel*unidades_updown); //En la altura a la que estamos el fuel necesario para llegar al suelo sin problema.
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
        memoria = new boolean[max_x][max_y]; //Arrays booleanos se inicializan a falso
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
        return escuchar(true);
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
        hecho_logout = true;
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
    * @author Kieran, Ana, Celia, Monica
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
            if(modo_mano_dcha) command = reglaManoDerecha(); //REGLA DE MANO DERECHA
            else command = comprobarAccion(); //funcion de utilidad/comprobar mejor casilla aqui

            //System.out.println(command.toString());

            if(pasos > max_pasos) {
                command = logout;
                System.out.println("Llegado a max_pasos pasos, haciendo logout:");
            }
            if(goal || command == logout){
                break; //Hemos acabado
            }
            
            memoria[gps.x][gps.y] = true; //Almacenamos la posición por la que pasa el agente

            mensaje = JSONEncode(); //codificar respuesta JSON aqui
            comunicar("Izar", mensaje);
            respuesta = escuchar(false);
            pasos++;
        }
        if(!validarRespuesta(respuesta)) { //si se sale por un resultado invalido devuelve las percepciones antes de la traza
            escuchar();
        }

        logout();
    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        if(!hecho_logout) logout();
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
