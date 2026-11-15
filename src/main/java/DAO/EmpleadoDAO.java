package DAO;

import exception.OurException;
import modelo.Empleado;
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
    Document comprobarUsuarios(String usuario, String contrasena) throws OurException;
    List<Empleado> listarEmpleados() throws OurException;
    boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria) throws OurException;
    boolean eliminarEmpleado(String nombreUsuario) throws OurException;
    boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) throws OurException;
}
