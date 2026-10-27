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

    @Override
    public String toString() {
        return "Contrato{" + "id=" + id + ", telefono=" + telefono + ", estado=" + estado + ", idCliente=" + idCliente + ", idTarifa=" + idTarifa + ", idEmpleado=" + idEmpleado + ", fechaComienzo=" + fechaComienzo + ", fechaFinalizacion=" + fechaFinalizacion + '}';
    }
}
