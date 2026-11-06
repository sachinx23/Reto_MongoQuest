package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ContratoDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Contratos (parte de cliente).
 */
public class ContratoDAOImplementacion extends DAOBase implements ContratoDAO {

    private static final Logger LOGGER = Logger.getLogger(ContratoDAOImplementacion.class.getName());

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultados " + contratos.size() + " contratos del cliente " + idCliente);
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando contratos del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        try {
            Document d = new Document();
            d.put("telefono", contrato.getTelefono());
            d.put("estado", contrato.getEstado().toString());
            d.put("idCliente", new ObjectId(contrato.getIdCliente()));
            d.put("idTarifa", new ObjectId(contrato.getIdTarifa()));
            d.put("idEmpleado", aObjectId(contrato.getIdEmpleado()));

            // Información embebida: los datos de la tarifa se guardan dentro del contrato
            Document tarifa = new Document();
            tarifa.put("nombreTarifa", contrato.getNombreTarifa());
            tarifa.put("precio", contrato.getPrecio());
            d.put("tarifa", tarifa);

            d.put("fechaComienzo", contrato.getFechaComienzo());
            d.put("fechaFinalizacion", contrato.getFechaFinalizacion());
            coleccion(COL_CONTRATOS).insertOne(d);

            contrato.setId(idTexto(d, "_id"));
            LOGGER.info("Contrato creado: id " + contrato.getId() + ", cliente " + contrato.getIdCliente()
                    + ", tarifa " + contrato.getIdTarifa() + ", estado " + contrato.getEstado());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando un contrato del cliente " + contrato.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_CONTRATO);
        }
    }

    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        try {
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("idTarifa", new ObjectId(idTarifa));
            filtro.put("estado", EstadoContrato.PENDIENTE.toString());
            long borrados = coleccion(COL_CONTRATOS).deleteOne(filtro).getDeletedCount();
            LOGGER.info("Petición de tarifa borrada: cliente " + idCliente + ", tarifa " + idTarifa + ", borrados " + borrados);
            return borrados > 0;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la petición del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstadoContrato(idContrato, EstadoContrato.BAJA_PENDIENTE, null, ErrorMessages.BAJA_CONTRATO);
        LOGGER.info("Baja solicitada del contrato " + idContrato);
    }
}
