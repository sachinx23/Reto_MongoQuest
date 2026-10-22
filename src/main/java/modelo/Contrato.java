/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author eneko
 */
public class Contrato {
    private String id;
    private String telefono;
    private String estado;
    private String idCliente;
    private String idTarifa;
    private String idEmpleado;
    private String fechaComienzo;
    private String fechaFin;

    public Contrato(String id, String telefono, String estado, String idCliente, String idTarifa, String idEmpleado, String fechaComienzo, String fechaFin) {
        this.id = id;
        this.telefono = telefono;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idTarifa = idTarifa;
        this.idEmpleado = idEmpleado;
        this.fechaComienzo = fechaComienzo;
        this.fechaFin = fechaFin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getFechaComienzo() {
        return fechaComienzo;
    }

    public void setFechaComienzo(String fechaComienzo) {
        this.fechaComienzo = fechaComienzo;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    @Override
    public String toString() {
        return "Contrato{" + "id=" + id + ", telefono=" + telefono + ", estado=" + estado + ", idCliente=" + idCliente + ", idTarifa=" + idTarifa + ", idEmpleado=" + idEmpleado + ", fechaComienzo=" + fechaComienzo + ", fechaFin=" + fechaFin + '}';
    }
    
    
}
