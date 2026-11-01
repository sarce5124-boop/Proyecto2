package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.interfaces.ClienteRepository;
import cr.ac.una.alquilervehiculos.repository.ClienteRepositoryJDBC;
import java.sql.SQLException;

/** Elimina clientes y respeta las llaves foráneas del historial. */
public class ClienteEliminacionService {
    private final ClienteRepository clientes = new ClienteRepositoryJDBC();

    /** @return true si se eliminó el cliente, false si no existía. */
    public boolean eliminarCliente(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria.");
        }
        try {
            // MySQL impide eliminar clientes con alquileres relacionados.
            return clientes.eliminar(identificacion.trim());
        } catch (SQLException e) {
            throw ErroresBD.convertir(e, "eliminar el cliente");
        }
    }
}
