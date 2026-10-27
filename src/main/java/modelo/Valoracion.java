package modelo;

/**
 * Representa una valoración de la colección <b>Valoraciones</b>.
 *
 * @author eneko
 */
public class Valoracion {

    /** Identificador (ObjectId en hexadecimal). */
    private String id;
    /** Puntuación de 1 a 5. */
    private int puntuacion;
    /** Opinión escrita por el cliente. */
    private String texto;
    /** Id del cliente que valora. */
    private String idCliente;
    /** Id de la tarifa valorada. */
    private String idTarifa;

    public Valoracion(String id, int puntuacion, String texto, String idCliente, String idTarifa) {
        this.id = id;
        this.puntuacion = puntuacion;
        this.texto = texto;
        this.idCliente = idCliente;
        this.idTarifa = idTarifa;
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

    @Override
    public String toString() {
        return "Valoracion{" + "id=" + id + ", puntuacion=" + puntuacion + ", texto=" + texto + ", idCliente=" + idCliente + ", idTarifa=" + idTarifa + '}';
    }
}
