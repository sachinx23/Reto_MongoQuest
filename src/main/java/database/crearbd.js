// Script para crear la base de datos Tel-ONDA con esquemas de validación.
// Autores: Iñigo, Sachin, Eneko Plaza
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
        nombreUsuario:  { bsonType: "string", minLength: 1 },
        nombre:         { bsonType: "string", minLength: 1 },
        contrasena:     { bsonType: "string", minLength: 4 },
        dni:            { bsonType: "string", pattern: "^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$" },
        correo:         { bsonType: "string", pattern: "^.+@.+\\..+$" },
        telefono:       { bsonType: "string" },
        cuentaBancaria: { bsonType: "string" },
        direccion: {
          bsonType: "object",
          required: ["calle", "localidad", "codigoPostal"],
          properties: {
            calle:        { bsonType: "string" },
            localidad:    { bsonType: "string" },
            codigoPostal: { bsonType: "int" }
          }
        }
      }
    }
  }
});
db.Clientes.createIndex({ nombreUsuario: 1 }, { unique: true });

// ---------------------------------------------------------------- Empleados
db.createCollection("Empleados", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "nombreUsuario", "rol", "contrasena", "correo"],
      properties: {
        nombre:         { bsonType: "string", minLength: 1 },
        nombreUsuario:  { bsonType: "string", minLength: 1 },
        rol:            { bsonType: "string" },
        direccion:      { bsonType: "string" },
        contrasena:     { bsonType: "string", minLength: 4 },
        correo:         { bsonType: "string", pattern: "^.+@.+\\..+$" },
        cuentaBancaria: { bsonType: "string" },
        sucursalId:     { bsonType: "objectId" }
      }
    }
  }
});
db.Empleados.createIndex({ nombreUsuario: 1 }, { unique: true });

// ---------------------------------------------------------------- Administradores
db.createCollection("Administradores", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "nombreUsuario", "rol", "contrasena", "correo"],
      properties: {
        nombre:         { bsonType: "string", minLength: 1 },
        nombreUsuario:  { bsonType: "string", minLength: 1 },
        rol:            { bsonType: "string" },
        contrasena:     { bsonType: "string", minLength: 4 },
        correo:         { bsonType: "string", pattern: "^.+@.+\\..+$" },
        cuentaBancaria: { bsonType: "string" }
      }
    }
  }
});
db.Administradores.createIndex({ nombreUsuario: 1 }, { unique: true });

// ---------------------------------------------------------------- Sucursales
db.createCollection("Sucursales", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "horario", "telefono", "Ubicacion"],
      properties: {
        nombre:   { bsonType: "string" },
        horario:  { bsonType: "string" },
        telefono: { bsonType: "string" },
        empleado: { bsonType: "array", items: { bsonType: "objectId" } },
        Ubicacion: {
          bsonType: "object",
          required: ["Direccion", "Localidad", "CodigoPostal"],
          properties: {
            Direccion:    { bsonType: "string" },
            Localidad:    { bsonType: "string" },
            CodigoPostal: { bsonType: "int" }
          }
        }
      }
    }
  }
});

// ---------------------------------------------------------------- Tarifas
db.createCollection("Tarifas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombreTarifa", "precio", "tipotarifa"],
      properties: {
        nombreTarifa: { bsonType: "string" },
        precio:       { bsonType: ["int", "double"], minimum: 0 },
        detalles:     { bsonType: "string" },
        tipotarifa:   { bsonType: "string" },
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
      required: ["telefono", "estado", "cliente_id", "tarifa_id", "fechaComienzo"],
      properties: {
        telefono:          { bsonType: "string" },
        estado:            { enum: ["pendiente", "activo", "baja pendiente", "cancelado", "rechazado", "rescindido"] },
        cliente_id:        { bsonType: "objectId" },
        tarifa_id:         { bsonType: "objectId" },
        empleado_id:       { bsonType: ["objectId", "null"] },
        fechaComienzo:     { bsonType: "string" },
        fechaFinalización: { bsonType: "string" }
      }
    }
  }
});

// ---------------------------------------------------------------- Valoraciones
db.createCollection("Valoraciones", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["puntuacion", "texto", "cliente_id"],
      properties: {
        puntuacion: { bsonType: "int", minimum: 1, maximum: 5 },
        texto:      { bsonType: "string", minLength: 1 },
        cliente_id: { bsonType: "objectId" },
        tarifa_id:  { bsonType: "objectId" }
      }
    }
  }
});

// ---------------------------------------------------------------- TicketSoportes
db.createCollection("TicketSoportes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["tipo", "asunto", "descripcion", "fechaCreacion", "estado", "cliente_id"],
      properties: {
        tipo:          { bsonType: "string" },
        asunto:        { bsonType: "string" },
        descripcion:   { bsonType: "string" },
        fechaCreacion: { bsonType: "string" },
        fechaFin:      { bsonType: ["string", "null"] },
        estado:        { enum: ["pendiente", "en proceso", "resuelto", "cerrado"] },
        cliente_id:    { bsonType: "objectId" }
      }
    }
  }
});

// ---------------------------------------------------------------- Datos de ejemplo
const idClientePedro   = ObjectId("653f101a2b8e4c1234567801");
const idEmpleadoPablo  = ObjectId("653f101a2b8e4c1234567802");
const idSucursalBilbao = ObjectId("653f101a2b8e4c1234567804");
const idTarifaCombo    = ObjectId("653f101a2b8e4c1234567805");
const idTarifaFibra    = ObjectId("653f101a2b8e4c1234567806");

db.Sucursales.insertOne({
  _id: idSucursalBilbao,
  nombre: "Tel-ONDA",
  horario: "Lunes a viernes: 09:00-20:00",
  telefono: "987 687 654",
  empleado: [idEmpleadoPablo],
  Ubicacion: {
    Direccion: "Lonbo Kalea",
    Localidad: "Bilbao",
    CodigoPostal: NumberInt(48480)
  }
});

db.Administradores.insertOne({
  nombre: "Aitor fernandez",
  nombreUsuario: "Aitorfernandez0",
  rol: "Admin",
  contrasena: "abcd",
  correo: "aitorfernandez@gmail.com",
  cuentaBancaria: "ES7921000813610123456789"
});

db.Empleados.insertOne({
  _id: idEmpleadoPablo,
  nombre: "Pablo diaz",
  nombreUsuario: "Pablodiaz0",
  rol: "Empleado",
  direccion: "lonbo kalea",
  contrasena: "abcd",
  correo: "pablodiaz@gmail.com",
  cuentaBancaria: "ES7921000813610123456789",
  sucursalId: idSucursalBilbao
});

db.Clientes.insertOne({
  _id: idClientePedro,
  nombreUsuario: "PedroDiaz0",
  nombre: "Pedro",
  contrasena: "abcd",
  dni: "12345678F",
  correo: "pedrodiaz@gmail.com",
  telefono: "666 666 666",
  cuentaBancaria: "ES7921000813610123456789",
  direccion: {
    calle: "Calle Abando 2",
    localidad: "Bilbao",
    codigoPostal: NumberInt(48001)
  }
});

db.Tarifas.insertMany([
  {
    _id: idTarifaCombo,
    nombreTarifa: "Onda Fibra+Móvil 600",
    precio: 40,
    detalles: "600 MB simétricos + 50 GB móvil",
    tipotarifa: "Fibra + Móvil",
    activa: true
  },
  {
    _id: idTarifaFibra,
    nombreTarifa: "Onda Fibra 1G",
    precio: 35,
    detalles: "1 GB simétrico",
    tipotarifa: "Fibra",
    activa: true
  }
]);

db.Contratos.insertMany([
  {
    telefono: "777 777 777",
    estado: "activo",
    cliente_id: idClientePedro,
    tarifa_id: idTarifaCombo,
    empleado_id: idEmpleadoPablo,
    fechaComienzo: "24/10/2030",
    fechaFinalización: "24/10/2030"
  },
  {
    telefono: "666 666 666",
    estado: "pendiente",
    cliente_id: idClientePedro,
    tarifa_id: idTarifaFibra,
    empleado_id: null,
    fechaComienzo: "04/11/2026",
    fechaFinalización: "04/11/2027"
  }
]);

db.Valoraciones.insertMany([
  {
    puntuacion: NumberInt(5),
    texto: "Muy buena velocidad y buen precio",
    cliente_id: idClientePedro,
    tarifa_id: idTarifaCombo
  }
]);

db.TicketSoportes.insertOne({
  tipo: "incidencia",
  asunto: "Sin conexion a internet",
  descripcion: "Desde ayer el router no se sincroniza con la fibra",
  fechaCreacion: "2026-10-05",
  fechaFin: null,
  estado: "pendiente",
  cliente_id: idClientePedro
});

print("Base de datos Tel-ONDA creada correctamente");