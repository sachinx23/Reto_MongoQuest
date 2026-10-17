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

    public Administrador(String gestionarSucursal, String crearSucursal, String crearEmpleado, String ascenderAdmin, String borrarCuentaEmpleado, int id, String nombre, String nombreUsuario, String rol, String contrasena, String correo, String cuentaBancaria, int idSucursal) {
        super(id, nombre, nombreUsuario, rol, contrasena, correo, cuentaBancaria, idSucursal);
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

    @Override
    public String toString() {
        return "Administrador{" + "gestionarSucursal=" + gestionarSucursal + ", crearSucursal=" + crearSucursal + ", crearEmpleado=" + crearEmpleado + ", ascenderAdmin=" + ascenderAdmin + ", borrarCuentaEmpleado=" + borrarCuentaEmpleado + '}';
    }
    
    
}
