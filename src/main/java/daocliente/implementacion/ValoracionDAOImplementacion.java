package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ValoracionDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Valoracion;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Valoraciones.
 */
public class ValoracionDAOImplementacion extends DAOBase implements ValoracionDAO {

    private static final Logger LOGGER = Logger.getLogger(ValoracionDAOImplementacion.class.getName());

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultadas " + valoraciones.size() + " valoraciones del cliente " + idCliente);
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando valoraciones del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("idCliente", new ObjectId(valoracion.getIdCliente()));
            d.put("idTarifa", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
            LOGGER.info("Valoración publicada: id " + valoracion.getId() + ", puntuación " + valoracion.getPuntuacion());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error publicando una valoración del cliente " + valoracion.getIdCliente(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("puntuacion", valoracion.getPuntuacion());
            cambios.put("texto", valoracion.getTexto());
            if (!modificar(COL_VALORACIONES, valoracion.getId(), cambios)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración modificada: id " + valoracion.getId());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando la valoración " + valoracion.getId(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        try {
            if (!borrar(COL_VALORACIONES, idValoracion)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración borrada: id " + idValoracion);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la valoración " + idValoracion, e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        for (Document d : coleccion(COL_VALORACIONES).find(filtro)) {
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"));
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }
}
