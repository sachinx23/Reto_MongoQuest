package modelocliente;

import java.util.Locale;

/**
 * Contrato de una tarifa. Guarda también el nombre y el precio de la tarifa para enseñarlos en las tablas.
 */
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

    /**
     * Crea un contrato con todos sus datos.
     *
     * @param id id del documento
     * @param telefono número de teléfono
     * @param estado estado del contrato
     * @param idCliente id del cliente
     * @param idTarifa id de la tarifa
     * @param idEmpleado id del empleado que lo gestiona (null si nadie)
     * @param fechaComienzo fecha de comienzo (aaaa-mm-dd)
     * @param fechaFinalizacion fecha de fin (aaaa-mm-dd)
     */
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

    /**
     * Fecha con formato para la tabla, por ejemplo "30/10/2027".
     *
     * @return la fecha en formato dd/mm/aaaa
     */
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

    /**
     * Precio con formato para la tabla.
     *
     * @return el precio con formato, por ejemplo "40,00 €"
     */
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

    /**
     * Crea un contrato vacío (se rellena con los setters).
     */
    public Contrato() {
    }

    /**
     * Cambia el estado a partir de su texto ("activo", "baja pendiente"...).
     *
     * @param estado texto del estado
     */
    public void setEstado(String estado) {
        this.estado = EstadoContrato.desdeTexto(estado);
    }

    public String getFechaFin() {
        return fechaFinalizacion;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFinalizacion = fechaFin;
    }

    /**
     * Estados por los que pasa un contrato.
     */
    public enum EstadoContrato {
        PENDIENTE("pendiente"),
        // Validada por un empleado o administrador: falta que el cliente lo firme
        PENDIENTE_FIRMA("pendiente de firma"),
        ACTIVO("activo"),
        BAJA_PENDIENTE("baja pendiente"),
        CANCELADO("cancelado"),
        // Estados que pone la parte de administrador
        RECHAZADO("rechazado"),
        RESCINDIDO("rescindido");

        // Texto que se guarda en MongoDB y que se ve en la tabla
        private final String texto;

        /**
         * Crea un estado.
         *
         * @param texto texto que se guarda en MongoDB
         */
        EstadoContrato(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }

        /**
         * Pasa el texto guardado en MongoDB ("baja pendiente") a su valor del enum (BAJA_PENDIENTE).
         *
         * @param texto texto guardado en MongoDB
         * @return el estado, o null si el texto no corresponde a ninguno
         */
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
