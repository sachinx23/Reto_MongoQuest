package daoempleado;

import exception.OurException;
import modelo.Empleado;

/**
 * Perfil del empleado.
 */
public interface EmpleadoDAO {

    // Sirve para empleados y administradores
    void actualizarEmpleado(Empleado empleado) throws OurException;
}
