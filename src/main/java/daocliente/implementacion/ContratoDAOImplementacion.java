package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ContratoDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.time.LocalDate;
import java.util.List;
import modelocliente.Contrato;
import modelocliente.Contrato.EstadoContrato;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Contratos (parte de cliente).
 */
public class ContratoDAOImplementacion extends DAOBase implements ContratoDAO {
    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("idCliente", new ObjectId(idCliente)));
            return contratos;
        } catch (MongoException e) {
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
        } catch (MongoException e) {
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
            return borrados > 0;
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstadoContrato(idContrato, EstadoContrato.BAJA_PENDIENTE, null, ErrorMessages.BAJA_CONTRATO);
    }

    @Override
    public void firmarContrato(Contrato contrato) throws OurException {
        try {
            // 1. El contrato pasa a activo: empieza hoy y dura un año
            Document cambios = new Document();
            cambios.put("estado", EstadoContrato.ACTIVO.toString());
            cambios.put("fechaComienzo", LocalDate.now().toString());
            cambios.put("fechaFinalizacion", LocalDate.now().plusYears(1).toString());
            if (!modificar(COL_CONTRATOS, contrato.getId(), cambios)) {
                throw new OurException(ErrorMessages.FIRMAR_CONTRATO);
            }
            try {
                // 2. Si el cliente estaba dado de baja, al tener otra vez un contrato activo deja de estarlo
                modificar(COL_CLIENTES, contrato.getIdCliente(), new Document("dadoDeBaja", false));

                // 3. Ticket ya cerrado con los detalles de la compra (el cliente lo ve en Soporte)
                Document ticket = new Document();
                ticket.put("tipo", "contrato");
                ticket.put("asunto", "Detalles de Compra y Firma de Contrato");
                ticket.put("descripcion", "Su contrato de la tarifa " + contrato.getNombreTarifa() + " (" + contrato.getPrecioTexto()
                        + " al mes) para la línea " + contrato.getTelefono() + " ha sido firmado correctamente.");
                ticket.put("fechaCreacion", LocalDate.now().toString());
                ticket.put("estado", "cerrado");
                ticket.put("idCliente", new ObjectId(contrato.getIdCliente()));
                ticket.put("idEmpleado", aObjectId(contrato.getIdEmpleado()));
                ticket.put("idContrato", new ObjectId(contrato.getId()));
                coleccion(COL_TICKETS).insertOne(ticket);
                Document nuevoTicket = new Document("ticketSoporte", ticket.get("_id"));
                coleccion(COL_CLIENTES).updateOne(new Document("_id", new ObjectId(contrato.getIdCliente())), new Document("$push", nuevoTicket));
            } catch (MongoException e) {
                // Si falla el paso 2 o 3, se deshace el 1: el contrato vuelve a estar pendiente de firma
                Document atras = new Document();
                atras.put("estado", EstadoContrato.PENDIENTE_FIRMA.toString());
                atras.put("fechaComienzo", contrato.getFechaComienzo());
                atras.put("fechaFinalizacion", contrato.getFechaFinalizacion());
                modificar(COL_CONTRATOS, contrato.getId(), atras);
                throw e;
            }
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.FIRMAR_CONTRATO);
        }
    }
}
