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

    public Contrato() {
    }

    // La parte de administrador trabaja con el estado como texto ("pendiente", "activo"...)
    public void setEstado(String estado) {
        this.estado = EstadoContrato.desdeTexto(estado);
    }

    public String getFechaFin() {
        return fechaFinalizacion;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFinalizacion = fechaFin;
    }

    // Estados por los que pasa un contrato
    public enum EstadoContrato {
        PENDIENTE("pendiente"),
        ACTIVO("activo"),
        BAJA_PENDIENTE("baja pendiente"),
        CANCELADO("cancelado"),
        // Estados que pone la parte de administrador
        RECHAZADO("rechazado"),
        RESCINDIDO("rescindido");

        // Texto que se guarda en MongoDB y que se ve en la tabla
        private final String texto;

        EstadoContrato(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }

        // Pasa el texto guardado en MongoDB ("baja pendiente") a su valor del enum (BAJA_PENDIENTE)
        public static EstadoContrato desdeTexto(String texto) {
            for (EstadoContrato estado : values()) {
                if (estado.texto.equals(texto)) {
                    return estado;
                }
            }
            return null;
        }
    }
}
