package modelo;

/**
 * Representa un contrato de la colección <b>Contratos</b>.
 *
 * @author eneko
 */
public class Contrato {

    /** Identificador (ObjectId en hexadecimal). */
    private String id;
    /** Teléfono asociado a la línea contratada. */
    private String telefono;
    /** Estado del contrato (activo, rescindido...). */
    private String estado;
    /** Id del cliente que firma el contrato. */
    private String idCliente;
    /** Id de la tarifa contratada. */
    private String idTarifa;
    /** Id del empleado que gestiona el contrato (puede ser null). */
    private String idEmpleado;
    /** Fecha de inicio (yyyy-MM-dd). */
    private String fechaComienzo;
    /** Fecha de fin (yyyy-MM-dd). */
    private String fechaFinalizacion;

    // Datos de otras colecciones (se rellenan con $lookup, no se guardan en Contratos)
    /** Nombre de la tarifa contratada. */
    private String nombreTarifa;
    /** Precio de la tarifa contratada. */
    private double precio;
    /** Nombre del cliente. */
    private String nombreCliente;
    /** DNI del cliente. */
    private String dniCliente;

    public Contrato(String id, String telefono, String estado, String idCliente, String idTarifa, String idEmpleado, String fechaComienzo, String fechaFinalizacion) {
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

    public String getFechaFinalizacion() {
        return fechaFinalizacion;
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

    @Override
    public String toString() {
        return "Contrato{" + "id=" + id + ", telefono=" + telefono + ", estado=" + estado + ", idCliente=" + idCliente + ", idTarifa=" + idTarifa + ", idEmpleado=" + idEmpleado + ", fechaComienzo=" + fechaComienzo + ", fechaFinalizacion=" + fechaFinalizacion + '}';
    }
}
