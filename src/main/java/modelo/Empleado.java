package modelo;

public class Empleado extends Usuario {

    // Puede ser null si el empleado no tiene sucursal
    private String idSucursal;
    private String rol;
    private String direccion;

    public Empleado(String id, String nombre, String nombreUsuario, String contrasena, String correo,
            String cuentaBancaria, String idSucursal) {
        super(id, nombre, nombreUsuario, contrasena, correo, cuentaBancaria);
        this.idSucursal = idSucursal;
    }

    public String getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    // Puesto que se enseña en la tabla de la plantilla
    public String getRol() {
        if (rol != null && !rol.isEmpty()) {
            return rol;
        }
        return "Empleado";
    }

    // Constructor que usa la parte de administrador (plantilla de empleados)
    public Empleado(String id, String nombreUsuario, String nombre, String apellidos, String contrasena, String direccion,
            String correo, String rol, String cuentaBancaria, String idSucursal) {
        super(id, nombre, nombreUsuario, contrasena, correo, cuentaBancaria);
        setApellidos(apellidos);
        this.direccion = direccion;
        this.rol = rol;
        this.idSucursal = idSucursal;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
