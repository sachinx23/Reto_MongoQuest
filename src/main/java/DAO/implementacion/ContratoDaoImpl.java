package DAO.implementacion;

import DAO.ContratoDAO;
import DAO.MongoDBConexion;
import modelo.Contrato;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoDaoImpl implements ContratoDAO {

    private Document getFiltroId(String idStr) {
        if (idStr != null) {
            if (ObjectId.isValid(idStr)) {
                return new Document("_id", new ObjectId(idStr));
            }
        }
        return new Document("_id", idStr);
    }

    private Contrato mapearContrato(Document doc) {
        if (doc == null) {
            return null;
        }

        Contrato c = new Contrato();

        if (doc.get("_id") != null) {
            c.setId(doc.get("_id").toString());
        } else {
            c.setId("");
        }

        if (doc.get("telefono") != null) {
            c.setTelefono(doc.get("telefono").toString());
        } else {
            c.setTelefono("");
        }

        if (doc.get("estado") != null) {
            c.setEstado(doc.get("estado").toString());
        } else {
            c.setEstado("");
        }

        Object valCliente = doc.get("idCliente");
        if (valCliente != null) {
            if (valCliente instanceof List) {
                List<?> listaCli = (List<?>) valCliente;
                if (!listaCli.isEmpty() && listaCli.get(0) != null) {
                    c.setIdCliente(listaCli.get(0).toString());
                } else {
                    c.setIdCliente("");
                }
            } else {
                c.setIdCliente(valCliente.toString());
            }
        } else {
            c.setIdCliente("");
        }

        Object valTarifa = doc.get("idTarifa");
        if (valTarifa != null) {
            if (valTarifa instanceof List) {
                List<?> listaTarifa = (List<?>) valTarifa;
                if (!listaTarifa.isEmpty() && listaTarifa.get(0) != null) {
                    c.setIdTarifa(listaTarifa.get(0).toString());
                } else {
                    c.setIdTarifa("");
                }
            } else {
                c.setIdTarifa(valTarifa.toString());
            }
        } else {
            c.setIdTarifa("");
        }
        Object valEmpleado = doc.get("idEmpleado");
        if (valEmpleado != null) {
            if (valEmpleado instanceof List) {
                List<?> listaEmp = (List<?>) valEmpleado;
                if (!listaEmp.isEmpty() && listaEmp.get(0) != null) {
                    c.setIdEmpleado(listaEmp.get(0).toString());
                } else {
                    c.setIdEmpleado("");
                }
            } else {
                c.setIdEmpleado(valEmpleado.toString());
            }
        } else {
            c.setIdEmpleado("");
        }

        if (doc.get("fechaComienzo") != null) {
            c.setFechaComienzo(doc.get("fechaComienzo").toString());
        } else {
            c.setFechaComienzo("");
        }

        Object fFin = doc.get("fechaFinalizacion");
        if (fFin == null) {
            fFin = doc.get("fechaFinalización");
        }
        if (fFin != null) {
            c.setFechaFin(fFin.toString());
        } else {
            c.setFechaFin("");
        }

        return c;
    }

    @Override
    public List<Contrato> listarContratos() {
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");
            List<Document> documentos = col.find().into(new ArrayList<>());

            for (int i = 0; i < documentos.size(); i++) {
                Document doc = documentos.get(i);
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean aprobarContrato(Contrato c, String idGestor) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Object idEmp = idGestor;
            if (ObjectId.isValid(idGestor)) {
                idEmp = new ObjectId(idGestor);
            }

            // Al confirmar la tarifa, se marca como pendiente de firma para enviarlo al cliente
            Document camposNuevos = new Document("estado", "pendiente de firma")
                    .append("idEmpleado", idEmp);

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            boolean ok = col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean firmarContrato(Contrato c) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            // Al firmar el cliente, el contrato pasa a activo y se registra la fecha de comienzo
            Document camposNuevos = new Document("estado", "activo")
                    .append("fechaComienzo", LocalDate.now().toString());

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            boolean ok = col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;

            if (ok) {
                ObjectId idCli = null;
                if (ObjectId.isValid(c.getIdCliente())) {
                    idCli = new ObjectId(c.getIdCliente());
                }

                Object idEmp = c.getIdEmpleado();
                if (ObjectId.isValid(c.getIdEmpleado())) {
                    idEmp = new ObjectId(c.getIdEmpleado());
                }

                ObjectId idCon = null;
                if (ObjectId.isValid(c.getId())) {
                    idCon = new ObjectId(c.getId());
                }

                // Generar el ticket de soporte con los detalles de la compra al firmar
                Document ticket = new Document()
                        .append("tipo", "contrato")
                        .append("asunto", "Detalles de Compra y Firma de Contrato")
                        .append("descripcion", "Su contrato para la línea " + c.getTelefono() + " ha sido firmado correctamente.")
                        .append("fechaCreacion", LocalDate.now().toString())
                        .append("estado", "cerrado")
                        .append("idCliente", idCli)
                        .append("idEmpleado", idEmp)
                        .append("idContrato", idCon);

                db.getCollection("TicketsSoporte").insertOne(ticket);
            }
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rechazarContratos(Contrato c) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rechazado");
            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rescindirContrato(Contrato c) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rescindido")
                    .append("fechaFinalizacion", LocalDate.now().toString());

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Contrato> filtrarContratos(String telefono, String estado) {
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document filtro = new Document();

            if (telefono != null) {
                if (!telefono.trim().isEmpty()) {
                    filtro.append("telefono", java.util.regex.Pattern.compile(telefono.trim(), java.util.regex.Pattern.CASE_INSENSITIVE));
                }
            }

            if (estado != null) {
                if (!"Todos".equalsIgnoreCase(estado)) {
                    filtro.append("estado", estado);
                }
            }

            List<Document> documentos = col.find(filtro).into(new ArrayList<>());
            for (int i = 0; i < documentos.size(); i++) {
                Document doc = documentos.get(i);
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
