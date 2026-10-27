package modelo;

/**
 * Representa un ticket de la colección <b>TicketSoportes</b>.
 *
 * @author eneko
 */
public class TicketSoporte {

    /** Identificador (ObjectId en hexadecimal). */
    private String id;
    /** Asunto de la incidencia. */
    private String asunto;
    /** Descripción detallada de la incidencia. */
    private String descripcion;
    /** Fecha de creación (yyyy-MM-dd). */
    private String fechaCreacion;
    /** Estado: pendiente, en proceso, resuelto o cerrado. */
    private String estado;
    /** Id del cliente que abre el ticket. */
    private String idCliente;
    /** Id del empleado que lo gestiona (puede ser null). */
    private String idEmpleado;

    public TicketSoporte(String id, String asunto, String descripcion, String fechaCreacion, String estado, String idCliente, String idEmpleado) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    @Override
    public String toString() {
        return "TicketSoporte{" + "id=" + id + ", asunto=" + asunto + ", descripcion=" + descripcion + ", fechaCreacion=" + fechaCreacion + ", estado=" + estado + ", idCliente=" + idCliente + ", idEmpleado=" + idEmpleado + '}';
    }
}
