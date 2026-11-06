package modelocliente;

import java.util.Locale;

/**
 * Tarifa que se puede contratar.
 */
public class Tarifa {

    private String id;
    private String nombreTarifa;
    private double precio;
    private String detalles;
    private String tipoTarifa;
    private boolean activa;

    /**
     * Crea una tarifa con todos sus datos.
     *
     * @param id id del documento
     * @param nombreTarifa nombre de la tarifa
     * @param precio precio al mes en euros
     * @param detalles descripción de la tarifa
     * @param tipoTarifa tipo de servicio (Fibra, Móvil...)
     * @param activa true si los clientes pueden contratarla
     */
    public Tarifa(String id, String nombreTarifa, double precio, String detalles, String tipoTarifa, boolean activa) {
        this.id = id;
        this.nombreTarifa = nombreTarifa;
        this.precio = precio;
        this.detalles = detalles;
        this.tipoTarifa = tipoTarifa;
        this.activa = activa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreTarifa() {
        return nombreTarifa;
    }

    public void setNombreTarifa(String nombreTarifa) {
        this.nombreTarifa = nombreTarifa;
    }

    public double getPrecio() {
        return precio;
    }

    /**
     * Precio con formato para la tabla, por ejemplo "40,00 €".
     *
     * @return el precio con formato, por ejemplo "40,00 €"
     */
    public String getPrecioTexto() {
        return String.format(Locale.forLanguageTag("es-ES"), "%.2f €", precio);
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public String getTipoTarifa() {
        return tipoTarifa;
    }

    public void setTipoTarifa(String tipoTarifa) {
        this.tipoTarifa = tipoTarifa;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    /**
     * Crea una tarifa nueva (todavía sin id).
     *
     * @param nombreTarifa nombre de la tarifa
     * @param precio precio al mes en euros
     * @param detalles descripción de la tarifa
     * @param tipoTarifa tipo de servicio (Fibra, Móvil...)
     * @param activa true si los clientes pueden contratarla
     */
    public Tarifa(String nombreTarifa, double precio, String detalles, String tipoTarifa, boolean activa) {
        this(null, nombreTarifa, precio, detalles, tipoTarifa, activa);
    }

    public boolean getActiva() {
        return activa;
    }
}
