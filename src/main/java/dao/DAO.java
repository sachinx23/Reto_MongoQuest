package dao;

import org.bson.Document;
import org.bson.types.ObjectId;

public interface DAO {
    Document comprobarUsuarios(String usuario, String contrasena);
    boolean actualizarCliente(Document cliente);
    boolean insertarCliente(String usuario, String nombre, String contrasena, 
                            String correo, String dni, String telefono, String direccion);
    boolean insertarContrato(String telefono, ObjectId idTarifa, ObjectId idCliente);
    boolean insertarTicket(String asunto, String descripcion, ObjectId idCliente);
}