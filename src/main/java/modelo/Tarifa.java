/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author eneko
 */
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
    
        public Tarifa(String nombreTarifa, double precio, String detalles, String tipoTarifa, boolean activa) {
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

    public boolean getActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return "Tarifa{" + "id=" + id + ", nombreTarifa=" + nombreTarifa + ", precio=" + precio + ", detalles=" + detalles + ", tipoTarifa=" + tipoTarifa + ", activa=" + activa + '}';
    }
}
