package modelocliente;

/**
 * Valoración (puntuación y opinión) de una tarifa.
 */
public class Valoracion {

    private String id;
    private int puntuacion;
    private String texto;
    private String idCliente;
    private String idTarifa;

    // No se guarda en Valoraciones: se rellena al consultar, para la tabla del administrador
    private String nombreCliente;
    private String nombreTarifa;

    /**
     * Crea una valoración con todos sus datos.
     *
     * @param id id del documento
     * @param puntuacion puntuación de 1 a 5
     * @param texto texto que se muestra
     * @param idCliente id del cliente
     * @param idTarifa id de la tarifa
     */
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /**
     * Crea una valoración vacía (se rellena con los setters).
     */
    public Valoracion() {
    }

    public String getNombreTarifa() {
        return nombreTarifa;
    }

    public void setNombreTarifa(String nombreTarifa) {
        this.nombreTarifa = nombreTarifa;
    }
}
