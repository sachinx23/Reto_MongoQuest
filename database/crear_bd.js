// Script para crear la base de datos Tel-ONDA con esquemas de validación.
// La usan las tres partes: cliente, empleado y administrador.
// Ejecutar con:  mongosh "mongodb://localhost:27017" database/crear_bd.js
// (o copiar y pegar en la consola mongosh de Compass)

db = db.getSiblingDB("Tel-ONDA");

// OJO: borra la base de datos entera para crearla de cero (también los datos que tenga)
db.dropDatabase();

// ---------------------------------------------------------------- Clientes
db.createCollection("Clientes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombreUsuario", "nombre", "contrasena", "dni", "correo"],
      properties: {
        nombreUsuario: { bsonType: "string", minLength: 1 },
        nombre:        { bsonType: "string", minLength: 1 },
        contrasena:    { bsonType: "string", minLength: 4 },
        dni:           { bsonType: "string", pattern: "^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$" },
        correo:        { bsonType: "string", pattern: "^.+@.+\\..+$" },
        telefono:      { bsonType: "string" },
        direccion:     { bsonType: "string" },
        cuentaBancaria:{ bsonType: "string" },
        dadoDeBaja:    { bsonType: "bool" },
        ticketSoporte: { bsonType: "array", items: { bsonType: "objectId" } }
      }
    }
  }
});
db.Clientes.createIndex({ nombreUsuario: 1 }, { unique: true });

// ---------------------------------------------------------------- Tarifas
db.createCollection("Tarifas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombreTarifa", "precio", "tipoTarifa"],
      properties: {
        nombreTarifa: { bsonType: "string" },
        precio:       { bsonType: ["int", "double"], minimum: 0 },
        detalles:     { bsonType: "string" },
        tipoTarifa:   { bsonType: "string" },
        activa:       { bsonType: "bool" }
      }
    }
  }
});

// ---------------------------------------------------------------- Contratos
db.createCollection("Contratos", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["telefono", "estado", "idCliente", "idTarifa", "fechaComienzo"],
      properties: {
        // Copia de la tarifa en el momento de contratar (si luego cambia el precio, el contrato no cambia)
        tarifa: {
          bsonType: "object",
          required: ["nombreTarifa", "precio"],
          properties: {
            nombreTarifa: { bsonType: "string" },
            precio:       { bsonType: ["int", "double"] }
          }
        },
        estado:    { enum: ["pendiente", "activo", "baja pendiente", "cancelado", "rechazado", "rescindido"] },
        idCliente: { bsonType: "objectId" },
        idTarifa:  { bsonType: "objectId" }
      }
    }
  }
});

// ---------------------------------------------------------------- Valoraciones
db.createCollection("Valoraciones", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["puntuacion", "texto", "idCliente"],
      properties: {
        puntuacion: { bsonType: "int", minimum: 1, maximum: 5 },
        texto:      { bsonType: "string", minLength: 1 },
        idCliente:  { bsonType: "objectId" }
      }
    }
  }
});

// ---------------------------------------------------------------- Resto
db.createCollection("Empleados");
db.createCollection("Administradores");
db.createCollection("TicketsSoporte");
db.createCollection("Sucursales");

// ---------------------------------------------------------------- Datos de ejemplo
// NumberInt: en mongosh los números se guardan como decimales; los que Java lee con getInteger van como enteros
const idSucursal = ObjectId();
const idPablo = ObjectId();
const idPedro = ObjectId();
const idFibra = ObjectId();
const idMovil = ObjectId();
const idContratoActivo = ObjectId();

db.Sucursales.insertOne({
  _id: idSucursal, id: NumberInt(1), horario: "Lunes a viernes: 09:00-20:00", telefono: 944123456,
  ubicacion: { direccion: "Gran Via 20", localidad: "Bilbao", codigoPostal: NumberInt(48001) },
  empleados: ["Pablodiaz0"]
});

db.Administradores.insertOne({
  nombreUsuario: "Aitorfernandez0", nombre: "Aitor fernandez", apellidos: "Fernandez", rol: "Admin",
  contrasena: "abcd", correo: "aitorfernandez@gmail.com", cuentaBancaria: "ES7921000813610123456789"
});

db.Empleados.insertOne({
  _id: idPablo, nombreUsuario: "Pablodiaz0", nombre: "Pablo diaz", apellidos: "Diaz", rol: "Empleado",
  contrasena: "abcd", correo: "pablodiaz@gmail.com", cuentaBancaria: "ES7921000813610123456789",
  direccion: "Calle Ercilla 5, Bilbao",
  // Número de la sucursal (su campo "id"), igual que guarda el formulario de nuevo empleado
  idSucursal: "1"
});

db.Clientes.insertOne({
  _id: idPedro, nombreUsuario: "PedroDiaz0", nombre: "Pedro", apellidos: "Diaz", rol: "Cliente",
  contrasena: "abcd", dni: "12345678Z", correo: "pedrodiaz@gmail.com", telefono: "666 666 666",
  direccion: "calle abando 2", cuentaBancaria: "ES7921000813610123456789", dadoDeBaja: false, ticketSoporte: []
});

db.Tarifas.insertMany([
  { _id: idFibra, nombreTarifa: "Onda Fibra 600", precio: 40, detalles: "600 Mb simétricos", tipoTarifa: "Fibra", activa: true },
  { _id: idMovil, nombreTarifa: "Onda Móvil 30", precio: 15, detalles: "30 GB + llamadas ilimitadas", tipoTarifa: "Móvil", activa: true },
  { nombreTarifa: "Onda Fibra 1G", precio: 35, detalles: "1 GB simétrico", tipoTarifa: "Fibra", activa: true }
]);

db.Contratos.insertMany([
  { _id: idContratoActivo, telefono: "666 666 666", estado: "activo", idCliente: idPedro, idTarifa: idFibra, idEmpleado: idPablo,
    tarifa: { nombreTarifa: "Onda Fibra 600", precio: 40 }, fechaComienzo: "2026-09-01", fechaFinalizacion: "2027-09-01" },
  { telefono: "666 666 666", estado: "pendiente", idCliente: idPedro, idTarifa: idMovil, idEmpleado: null,
    tarifa: { nombreTarifa: "Onda Móvil 30", precio: 15 }, fechaComienzo: "2026-11-04", fechaFinalizacion: "2027-11-04" }
]);

const ticket = db.TicketsSoporte.insertOne({
  asunto: "Sin conexión", descripcion: "No tengo internet desde ayer", tipo: "incidencia", estado: "pendiente",
  fechaCreacion: "2026-11-03", idCliente: idPedro, idContrato: idContratoActivo, idEmpleado: null
});
db.Clientes.updateOne({ _id: idPedro }, { $push: { ticketSoporte: ticket.insertedId } });

db.Valoraciones.insertMany([
  { puntuacion: NumberInt(5), texto: "Muy buena velocidad", idCliente: idPedro, idTarifa: idFibra },
  { puntuacion: NumberInt(3), texto: "El móvil pierde cobertura a veces", idCliente: idPedro, idTarifa: idMovil }
]);

print("Base de datos Tel-ONDA creada");
