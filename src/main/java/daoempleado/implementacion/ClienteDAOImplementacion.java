package daoempleado.implementacion;

import com.mongodb.MongoException;
import daocliente.implementacion.DAOBase;
import daoempleado.ClienteDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Bajas de clientes (colecciones Clientes y Contratos).
 */
public class ClienteDAOImplementacion extends DAOBase implements ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAOImplementacion.class.getName());

    @Override
    public List<Cliente> obtenerClientesConBajaPendiente() throws OurException {
        List<Cliente> clientes = new ArrayList<>();
        List<String> idsVistos = new ArrayList<>();
        try {
            for (Document contrato : coleccion(COL_CONTRATOS).find(new Document("estado", EstadoContrato.BAJA_PENDIENTE.toString()))) {
                String idCliente = idTexto(contrato, "idCliente");
                // Un cliente puede tener varias bajas pendientes: solo se añade una vez
                if (!idsVistos.contains(idCliente)) {
                    idsVistos.add(idCliente);
                    Document d = buscarPorId(COL_CLIENTES, new ObjectId(idCliente));
                    if (d != null) {
                        clientes.add(documentoACliente(d));
                    }
                }
            }
            LOGGER.info("Consultados " + clientes.size() + " clientes con baja pendiente");
            return clientes;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando las bajas pendientes", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void aprobarBaja(String idCliente, String idEmpleado) throws OurException {
        try {
            // 1. Mirar si después de la baja le quedará algún contrato en vigor (activo o pendiente de validar)
            boolean quedanContratos = false;
            for (Contrato c : buscarContratos(new Document("idCliente", new ObjectId(idCliente)))) {
                if (c.getEstado() == EstadoContrato.ACTIVO || c.getEstado() == EstadoContrato.PENDIENTE) {
                    quedanContratos = true;
                }
            }

            // 2. Si no le queda ninguno, el cliente queda dado de baja (se mantiene su perfil)
            if (!quedanContratos) {
                modificar(COL_CLIENTES, idCliente, new Document("dadoDeBaja", true));
            }

            // 3. Cancelar todos los contratos del cliente que están en "baja pendiente"
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("estado", EstadoContrato.BAJA_PENDIENTE.toString());
            Document cambios = new Document();
            cambios.put("estado", EstadoContrato.CANCELADO.toString());
            cambios.put("idEmpleado", new ObjectId(idEmpleado));
            try {
                coleccion(COL_CONTRATOS).updateMany(filtro, new Document("$set", cambios));
            } catch (MongoException e) {
                // Si falla el paso 3, se deshace el 2: el cliente deja de estar dado de baja
                if (!quedanContratos) {
                    modificar(COL_CLIENTES, idCliente, new Document("dadoDeBaja", false));
                }
                throw e;
            }
            LOGGER.info("Baja aprobada por el empleado " + idEmpleado + " del cliente " + idCliente);
            if (!quedanContratos) {
                LOGGER.info("Cliente " + idCliente + " dado de baja");
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error aprobando la baja del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }
}
