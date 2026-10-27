package dao;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Implementación simulada (Mock) de la interfaz DAO para realizar pruebas
 * sin necesidad de conectar con una instancia activa de MongoDB.
 */
public class MockModelDao implements DAO {

    private boolean shouldThrowException = false;
    private Document mockUserResponse;

    public MockModelDao() {
        // Datos de usuario simulados por defecto (coinciden con tus pruebas de login)
        this.mockUserResponse = new Document("_id", new ObjectId())
                .append("nombreUsuario", "sachinx23")
                .append("nombre", "Sachin")
                .append("contrasena", "sachin")
                .append("correo", "sachin@gmail.com")
                .append("dni", "12345678F")
                .append("telefono", "123444555")
                .append("direccion", "calle miribilla 12 2 izq")
                .append("rol", "Cliente");
    }

    public void setShouldThrowException(boolean shouldThrow) {
        this.shouldThrowException = shouldThrow;
    }

    public void setMockUserResponse(Document mockUserResponse) {
        this.mockUserResponse = mockUserResponse;
    }

    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) {
        if (shouldThrowException) {
            throw new RuntimeException("Error simulado en la base de datos");
        }
        if (mockUserResponse != null && 
            mockUserResponse.getString("nombreUsuario").equals(usuario) && 
            mockUserResponse.getString("contrasena").equals(contrasena)) {
            return mockUserResponse;
        }
        return null;
    }

    @Override
    public boolean actualizarCliente(Document cliente) {
        if (shouldThrowException) {
            throw new RuntimeException("Error simulado en la base de datos");
        }
        return cliente != null && cliente.containsKey("_id");
    }

    @Override
    public boolean insertarCliente(String usuario, String nombre, String contrasena, 
                                 String correo, String dni, String telefono, String direccion) {
        if (shouldThrowException) {
            throw new RuntimeException("Error simulado en la base de datos");
        }
        return usuario != null && !usuario.trim().isEmpty();
    }

    @Override
    public boolean insertarContrato(String telefono, ObjectId idTarifa, ObjectId idCliente) {
        if (shouldThrowException) {
            throw new RuntimeException("Error simulado en la base de datos");
        }
        return telefono != null && idTarifa != null && idCliente != null;
    }

    @Override
    public boolean insertarTicket(String asunto, String descripcion, ObjectId idCliente) {
        if (shouldThrowException) {
            throw new RuntimeException("Error simulado en la base de datos");
        }
        return asunto != null && !asunto.trim().isEmpty() && idCliente != null;
    }
}