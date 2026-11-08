package modelo;


/**
 *
 * @author eneko
 */
public class Valoracion {
    private String id;           
    private int puntuacion;      
    private String texto;        
    private String idCliente;    
    private String idTarifa;  
    private String nombreCliente;
    private String nombreTarifa;

    public Valoracion() {}

    public Valoracion(String id, int puntuacion, String texto, String idCliente, String idTarifa, String nombreCliente, String nombreTarifa) {
        this.id = id;
        this.puntuacion = puntuacion;
        this.texto = texto;
        this.idCliente = idCliente;
        this.idTarifa = idTarifa;
        this.nombreCliente = nombreCliente;
        this.nombreTarifa = nombreTarifa;
    }

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdTarifa() {
        return idTarifa;
    }

    public void setIdTarifa(String idTarifa) {
        this.idTarifa = idTarifa;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreTarifa() {
        return nombreTarifa;
    }

    public void setNombreTarifa(String nombreTarifa) {
        this.nombreTarifa = nombreTarifa;
    }

   

    @Override
    public String toString() {
        return puntuacion + "/5 ⭐ - " + texto;
    }
}