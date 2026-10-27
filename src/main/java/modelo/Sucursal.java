package modelo;

import java.util.List;

/**
 * Representa una tienda física de la colección <b>Sucursales</b>.
 * Los datos de la ubicación (dirección, localidad y código postal) se guardan
 * en MongoDB dentro del subdocumento "Ubicacion".
 *
 * @author eneko
 */
public class Sucursal {

    /** Identificador (ObjectId en hexadecimal). */
    private String id;
    /** Nombre o código de la sucursal. */
    private String nombre;
    /** Horario de atención, p. ej. "Lunes a viernes: 09:00-20:00". */
    private String horario;
    /** Teléfono de la tienda. */
    private String telefono;
    /** Calle y número. */
    private String direccion;
    /** Localidad. */
    private String localidad;
    /** Código postal. */
    private int codigoPostal;
    /** Ids de los empleados asignados a la sucursal. */
    private List<String> empleados;

    public Sucursal(String id, String nombre, String horario, String telefono, String direccion, String localidad, int codigoPostal, List<String> empleados) {
        this.id = id;
        this.nombre = nombre;
        this.horario = horario;
        this.telefono = telefono;
        this.direccion = direccion;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
        this.empleados = empleados;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public int getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(int codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public List<String> getEmpleados() {
        return empleados;
    }

    public void setEmpleados(List<String> empleados) {
        this.empleados = empleados;
    }

    /**
     * Devuelve solo el nombre, que es lo que se ve al elegir una sucursal en un ComboBox.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
