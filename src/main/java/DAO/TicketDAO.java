/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import modelo.TicketSoporte;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface TicketDAO {
    boolean crearTickets(String idCliente, String asunto, String descripcion);
    List<TicketSoporte> listarTicket(String estado);
    List<TicketSoporte> listarTicketsCliente(String idCliente);
    boolean asignarTicket(TicketSoporte t, String idEmpleado);
    boolean cambiarEstadoTicket(TicketSoporte t, String estado);
    TicketSoporte mapearTicket(org.bson.Document d);
}
