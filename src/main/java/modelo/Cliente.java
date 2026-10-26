/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.util.List;
/**
 *
 * @author eneko
 */
public class Cliente {
    int id;
    String nombreUsuario;
    String nombre;
    String apellidos;
    String correo;
    String contrasena;
    String dni;
    int telefono;
    String cuentaBancaria;
    String direccion;
    List<String> ticketSoporte;
    List<Contrato> contrato;
    List<Valoracion> valoraciones;

    public Cliente(int id, String cuentaBancaria, String nombre, String nombreUsuario, String contrasena, String dni, String correo, int telefono, String direccion, List<String> ticketSoporte, List<Contrato> contrato, List<Valoracion> valoraciones) {
        this.id = id;
        this.cuentaBancaria = cuentaBancaria;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.dni = dni;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ticketSoporte = ticketSoporte;
        this.contrato = contrato;
        this.valoraciones = valoraciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(String cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getTicketSoporte() {
        return ticketSoporte;
    }

    public void setTicketSoporte(List<String> ticketSoporte) {
        this.ticketSoporte = ticketSoporte;
    }

    public List<Contrato> getContrato() {
        return contrato;
    }

    public void setContrato(List<Contrato> contrato) {
        this.contrato = contrato;
    }

    public List<Valoracion> getValoraciones() {
        return valoraciones;
    }

    public void setValoraciones(List<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
    }

    @Override
    public String toString() {
        return "Cliente{" + "id=" + id + ", cuentaBancaria=" + cuentaBancaria + ", nombre=" + nombre + ", nombreUsuario=" + nombreUsuario + ", contrasena=" + contrasena + ", dni=" + dni + ", correo=" + correo + ", telefono=" + telefono + ", direccion=" + direccion + ", ticketSoporte=" + ticketSoporte + ", contrato=" + contrato + ", valoraciones=" + valoraciones + '}';
    }
    
    
    
}
