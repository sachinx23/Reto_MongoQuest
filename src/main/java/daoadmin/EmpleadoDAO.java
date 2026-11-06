package daoadmin;

import modeloadmin.Empleado;
import org.bson.Document;
import java.util.List;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Eneko.Plaza
 */
public interface EmpleadoDAO {
    Document comprobarUsuarios(String usuario, String contrasena);
    List<Empleado> listarEmpleados();
    boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria);
    boolean eliminarEmpleado(String nombreUsuario);
    boolean ascenderEmpleado(String nombreUsuario, String nuevoRol);
}
