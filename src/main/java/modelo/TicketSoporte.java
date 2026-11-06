package modelo;

public class TicketSoporte {

    private String id;
    private String asunto;
    private String descripcion;
    private String fechaCreacion;
    private String estado;
    private String idCliente;
    private String idEmpleado;

    // No se guarda en TicketsSoportes: se rellena al consultar con $lookup
    private String nombreCliente;
    private String tipo;
    private String idContrato;

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

    // Fecha con formato para la tabla, por ejemplo "30/10/2026"
    public String getFechaCreacionTexto() {
        // En MongoDB se guarda como "2026-10-30" y en la tabla se enseña como "30/10/2026"
        String[] partes = fechaCreacion.split("-");
        if (partes.length != 3) {
            return fechaCreacion;
        }
        return partes[2] + "/" + partes[1] + "/" + partes[0];
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

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public TicketSoporte() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(String idContrato) {
        this.idContrato = idContrato;
    }
}
