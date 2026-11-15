/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.Cliente;
import org.bson.Document;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface ClienteDAO {

    Document comprobarUsuarios(String usuario, String contrasena) throws OurException;

    boolean actualizarCliente(Cliente c) throws OurException;

    boolean registrarCliente(String usuario, String nombre, String apellidos, String contrasena, String correo, String dni, String telefono, String direccion) throws OurException;

    boolean eliminarDatosSensiblesCliente(String nombreUsuario) throws OurException;

    List<Cliente> listarClientes() throws OurException;

    Cliente obtenerCliente(String id) throws OurException;
}
