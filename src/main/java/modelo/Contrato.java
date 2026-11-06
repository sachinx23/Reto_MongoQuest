package modelo;

import java.util.Locale;

public class Contrato {

    private String id;
    private String telefono;
    private EstadoContrato estado;
    private String idCliente;
    private String idTarifa;
    private String idEmpleado;
    private String fechaComienzo;
    private String fechaFinalizacion;

    // Datos de la tarifa embebida y del cliente, para enseñarlos en las tablas
    private String nombreTarifa;
    private double precio;
    private String nombreCliente;
    private String dniCliente;

    public Contrato(String id, String telefono, EstadoContrato estado, String idCliente, String idTarifa, String idEmpleado, String fechaComienzo, String fechaFinalizacion) {
        this.id = id;
        this.telefono = telefono;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idTarifa = idTarifa;
        this.idEmpleado = idEmpleado;
        this.fechaComienzo = fechaComienzo;
        this.fechaFinalizacion = fechaFinalizacion;
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

    public EstadoContrato getEstado() {
        return estado;
    }

    public void setEstado(EstadoContrato estado) {
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

    // Fecha con formato para la tabla, por ejemplo "30/10/2026"
    public String getFechaComienzoTexto() {
        // En MongoDB se guarda como "2026-10-30" y en la tabla se enseña como "30/10/2026"
        String[] partes = fechaComienzo.split("-");
        if (partes.length != 3) {
            return fechaComienzo;
        }
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    public void setFechaComienzo(String fechaComienzo) {
        this.fechaComienzo = fechaComienzo;
    }

    public String getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    // Fecha con formato para la tabla, por ejemplo "30/10/2027"
    public String getFechaFinalizacionTexto() {
        // En MongoDB se guarda como "2026-10-30" y en la tabla se enseña como "30/10/2026"
        String[] partes = fechaFinalizacion.split("-");
        if (partes.length != 3) {
            return fechaFinalizacion;
        }
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    public void setFechaFinalizacion(String fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }
}
