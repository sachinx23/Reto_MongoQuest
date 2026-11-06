/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.TicketSoporte;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface TicketDAO {
    boolean crearTickets(String idCliente, String asunto, String descripcion) throws OurException;
    List<TicketSoporte> listarTicket(String estado) throws OurException;
    List<TicketSoporte> listarTicketsCliente(String idCliente) throws OurException;
    boolean asignarTicket(TicketSoporte t, String idEmpleado) throws OurException;
    boolean cambiarEstadoTicket(TicketSoporte t, String estado) throws OurException;
    TicketSoporte mapearTicket(org.bson.Document d);
}
