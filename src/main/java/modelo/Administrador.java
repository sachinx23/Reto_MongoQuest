    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author eneko
 */
public class Administrador extends Empleado{
    
    String gestionarSucursal;
    String crearSucursal;
    String crearEmpleado;
    String ascenderAdmin;
    String borrarCuentaEmpleado;

    public Administrador(String gestionarSucursal, String crearSucursal, String crearEmpleado, String ascenderAdmin, String borrarCuentaEmpleado, int id, String nombreUsuario, String nombre, String apellidos, String rol, String contrasena, String correo, String cuentaBancaria, int idSucursal) {
        super(id, nombreUsuario, nombre, apellidos, rol, contrasena, correo, cuentaBancaria, idSucursal);
        this.gestionarSucursal = gestionarSucursal;
        this.crearSucursal = crearSucursal;
        this.crearEmpleado = crearEmpleado;
        this.ascenderAdmin = ascenderAdmin;
        this.borrarCuentaEmpleado = borrarCuentaEmpleado;
    }

    public String getGestionarSucursal() {
        return gestionarSucursal;
    }

    public void setGestionarSucursal(String gestionarSucursal) {
        this.gestionarSucursal = gestionarSucursal;
    }

    public String getCrearSucursal() {
        return crearSucursal;
    }

    public void setCrearSucursal(String crearSucursal) {
        this.crearSucursal = crearSucursal;
    }

    public String getCrearEmpleado() {
        return crearEmpleado;
    }

    public void setCrearEmpleado(String crearEmpleado) {
        this.crearEmpleado = crearEmpleado;
    }

    public String getAscenderAdmin() {
        return ascenderAdmin;
    }

    public void setAscenderAdmin(String ascenderAdmin) {
        this.ascenderAdmin = ascenderAdmin;
    }

    public String getBorrarCuentaEmpleado() {
        return borrarCuentaEmpleado;
    }

    public void setBorrarCuentaEmpleado(String borrarCuentaEmpleado) {
        this.borrarCuentaEmpleado = borrarCuentaEmpleado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    @Override
    public String toString() {
        return "Administrador{" + "gestionarSucursal=" + gestionarSucursal + ", crearSucursal=" + crearSucursal + ", crearEmpleado=" + crearEmpleado + ", ascenderAdmin=" + ascenderAdmin + ", borrarCuentaEmpleado=" + borrarCuentaEmpleado + '}';
    }

   
}
