package modelo;

/**
 *
 * @author eneko
 */
public class TicketSoporte {
    private String id;          
    private String asunto;
    private String descripcion;
    private String tipo; // incidencia o contrato
    private String fechaCreacion;
    private String estado;       // "pendiente", "en proceso", "resuelto"
    private String idCliente;  
    private String idEmpleado;  
    private String idContrato;

    public TicketSoporte() {}

    public TicketSoporte(String id, String asunto, String descripcion, String tipo, String fechaCreacion, String estado, String idCliente, String idEmpleado, String idContrato) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.idContrato = idContrato;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public String getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(String idContrato) {
        this.idContrato = idContrato;
    }

    @Override
    public String toString() {
        return "TicketSoporte{" + "id=" + id + ", asunto=" + asunto + ", descripcion=" + descripcion + ", tipo=" + tipo + ", fechaCreacion=" + fechaCreacion + ", estado=" + estado + ", idCliente=" + idCliente + ", idEmpleado=" + idEmpleado + ", idContrato=" + idContrato + '}';
    }

    
}