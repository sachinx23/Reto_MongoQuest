package modelo;

/**
 * Modelo de datos para la entidad Empleado.
 * @author eneko
 */
public class Empleado {
    private String id;
    private String nombreUsuario;
    private String nombre;
    private String apellidos;
    private String contrasena;
    private String correo;
    private String rol;
    private String cuentaBancaria;
    private String idSucursal;

    // --- ÚNICO CONSTRUCTOR ---
    public Empleado(String id, String nombreUsuario, String nombre, String apellidos, String contrasena, String correo, String rol, String cuentaBancaria, String idSucursal) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasena = contrasena;
        this.correo = correo;
        this.rol = rol;
        this.cuentaBancaria = cuentaBancaria;
        this.idSucursal = idSucursal;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    @Override
    public String toString() {
        return "Empleado{" + "nombreUsuario=" + nombreUsuario + ", nombre=" + nombre + ", rol=" + rol + '}';
    }
}