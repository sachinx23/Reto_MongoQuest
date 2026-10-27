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
    
    int id;
    String nombreTarifa;
    double precio;
    String detalles;
    String tipoTarifa;
    double activa;

    public Tarifa(int id, String nombreTarifa, double precio, String detalles, String tipoTarifa, double activa) {
        this.id = id;
        this.nombreTarifa = nombreTarifa;
        this.precio = precio;
        this.detalles = detalles;
        this.tipoTarifa = tipoTarifa;
        this.activa = activa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public double getActiva() {
        return activa;
    }

    public void setActiva(double activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return "Tarifa{" + "id=" + id + ", nombreTarifa=" + nombreTarifa + ", precio=" + precio + ", detalles=" + detalles + ", tipoTarifa=" + tipoTarifa + ", activa=" + activa + '}';
    }
    
    
}
