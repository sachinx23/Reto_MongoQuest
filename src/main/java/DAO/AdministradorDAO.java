/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import org.bson.Document;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface AdministradorDAO {

    Document comprobarUsuarios(String usuario, String contrasena) throws OurException;

    boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria, String direccion, String idSucursal) throws OurException;

    boolean eliminarEmpleado(String nombreUsuario) throws OurException;

    boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) throws OurException;

    List<Document> obtenerPlantilla() throws OurException;
    
    boolean agregarEmpleadoASucursal(int idSucursal, String nombreUsuarioEmpleado) throws OurException;
}
