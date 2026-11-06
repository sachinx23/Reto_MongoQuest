/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import org.bson.Document;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface AdministradorDAO {

    Document comprobarUsuarios(String usuario, String contrasena);

    boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria, String direccion, String idSucursal);

    boolean eliminarEmpleado(String nombreUsuario);

    boolean ascenderEmpleado(String nombreUsuario, String nuevoRol);

    List<Document> obtenerPlantilla();
    
    boolean agregarEmpleadoASucursal(int idSucursal, String nombreUsuarioEmpleado);
}
