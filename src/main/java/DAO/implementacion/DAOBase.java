package DAO.implementacion;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import DAO.MongoDBConexion;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;
import org.bson.Document;
import org.bson.types.ObjectId;

public abstract class DAOBase {

    private static final Logger LOGGER = Logger.getLogger(DAOBase.class.getName());
    protected static final String COL_CLIENTES = "Clientes";
    protected static final String COL_TARIFAS = "Tarifas";
    protected static final String COL_CONTRATOS = "Contratos";
    protected static final String COL_VALORACIONES = "Valoraciones";

    protected MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoDBConexion.getColeccion(nombre);
    }

    protected boolean nombreUsuarioOcupado(String nombreUsuario, String idPropio) throws OurException {
        String[] colecciones = {"Administradores", "Empleados", COL_CLIENTES};
        for (String nombreColeccion : colecciones) {
            Document d = coleccion(nombreColeccion).find(new Document("nombreUsuario", nombreUsuario)).first();
            if (d != null && !idTexto(d, "_id").equals(idPropio)) {
                return true;
            }
        }
        return false;
    }

    protected List<Contrato> buscarContratos(Document filtro) throws OurException {
        List<Contrato> contratos = new ArrayList<>();
        for (Document d : coleccion(COL_CONTRATOS).find(filtro)) {
            Contrato c = new Contrato(idTexto(d, "_id"), texto(d, "telefono"), texto(d, "estado"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"), idTexto(d, "idEmpleado"),
                    texto(d, "fechaComienzo"), texto(d, "fechaFinalizacion"));
            contratos.add(c);
        }
        return contratos;
    }

    protected void cambiarEstadoContrato(String idContrato, String estado, String idEmpleado, String mensajeError) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("estado", estado);
            if (idEmpleado != null) {
                cambios.put("idEmpleado", new ObjectId(idEmpleado));
            }
            if (!modificar(COL_CONTRATOS, idContrato, cambios)) {
                throw new OurException(mensajeError);
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en cambiarEstadoContrato", e);
            throw new OurException(mensajeError);
        }
    }

    protected Cliente documentoACliente(Document d) {
        return new Cliente(idTexto(d, "_id"), texto(d, "cuentaBancaria"), texto(d, "nombreUsuario"), texto(d, "nombre"),
                texto(d, "apellidos"), texto(d, "contrasena"), texto(d, "correo"), texto(d, "dni"), texto(d, "telefono"),
                texto(d, "direccion"));
    }

    protected Tarifa documentoATarifa(Document d) {
        return new Tarifa(idTexto(d, "_id"), texto(d, "nombreTarifa"), numero(d, "precio"), texto(d, "detalles"),
                texto(d, "tipoTarifa"), booleano(d, "activa", true));
    }

    protected Document buscarPorId(String nombreColeccion, Object id) throws OurException {
        return coleccion(nombreColeccion).find(new Document("_id", id)).first();
    }

    protected boolean modificar(String nombreColeccion, String id, Document cambios) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long encontrados = coleccion(nombreColeccion).updateOne(filtro, new Document("$set", cambios)).getMatchedCount();
        return encontrados > 0;
    }

    protected boolean borrar(String nombreColeccion, String id) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long borrados = coleccion(nombreColeccion).deleteOne(filtro).getDeletedCount();
        return borrados > 0;
    }

    protected ObjectId aObjectId(String id) {
        if (id == null) {
            return null;
        }
        return new ObjectId(id);
    }

    protected String texto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    protected String idTexto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return null;
        }
        return valor.toString();
    }

    protected boolean booleano(Document d, String campo, boolean porDefecto) {
        Object valor = d.get(campo);
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return porDefecto;
    }

    protected double numero(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
