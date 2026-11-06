/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin;

import modeloadmin.Cliente;
import org.bson.Document;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface ClienteDAO {
    Document comprobarUsuarios(String usuario, String contrasena);
    boolean registrarCliente(String usuario, String nombre, String apellidos, String contrasena, String correo, String dni, String telefono, String direccion);
    boolean eliminarDatosSensiblesCliente(String nombreUsuario);
    List<Cliente> listarClientes();
}
