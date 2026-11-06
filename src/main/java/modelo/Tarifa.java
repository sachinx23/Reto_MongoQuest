package modelo;

import java.util.Locale;

public class Tarifa {

    private String id;
    private String nombreTarifa;
    private double precio;
    private String detalles;
    private String tipoTarifa;
    private boolean activa;

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

    // Precio con formato para la tabla, por ejemplo "40,00 €"
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

    // Para la tabla del administrador: "Sí" o "No"
    public String getActivaTexto() {
        if (activa) {
            return "Sí";
        }
        return "No";
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
