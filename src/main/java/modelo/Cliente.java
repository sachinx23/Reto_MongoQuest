package modelo;

import java.util.List;

/**
 * Modelo de datos para la entidad Cliente.
 * @author eneko
 */
public class Cliente {
    private String id;
    private String cuentaBancaria;
    private String nombreUsuario;
    private String nombre;
    private String apellidos;
    private String contrasena;
    private String correo;
    private String dni;
    private String telefono;
    private String direccion;
    private List<String> ticketSoporte;
    private List<Contrato> contrato;
    private List<Valoracion> valoraciones;

    // --- Constructor Vacío (Obligatorio para el DAO) ---
    public Cliente() {
    }

    // --- Constructor para el método listarClientes() ---
    public Cliente(String nombreUsuario, String nombre, String apellidos, String correo, String dni, String telefono, String direccion) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // --- Constructor Completo ---
    public Cliente(String id, String cuentaBancaria, String nombreUsuario, String nombre, String apellidos, String contrasena, String correo, String dni, String telefono, String direccion) {
        this.id = id;
        this.cuentaBancaria = cuentaBancaria;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasena = contrasena;
        this.correo = correo;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getTicketSoporte() {
        return ticketSoporte;
    }

    public void setTicketSoporte(List<String> ticketSoporte) {
        this.ticketSoporte = ticketSoporte;
    }

    public List<Contrato> getContrato() {
        return contrato;
    }

    public void setContrato(List<Contrato> contrato) {
        this.contrato = contrato;
    }

    public List<Valoracion> getValoraciones() {
        return valoraciones;
    }

    public void setValoraciones(List<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
    }

    @Override
    public String toString() {
        return "Cliente{" + "nombreUsuario=" + nombreUsuario + ", nombre=" + nombre + ", correo=" + correo + '}';
    }
}