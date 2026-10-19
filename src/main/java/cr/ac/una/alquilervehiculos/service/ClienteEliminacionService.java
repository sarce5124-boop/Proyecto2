package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Elimina clientes sin comprometer las referencias de los alquileres. */
public class ClienteEliminacionService {

    /** @return true si se eliminó el cliente, false si no existía. */
    public boolean eliminarCliente(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria.");
        }
        try (Connection conexion = ConexionBD.conectar()) {
            try (PreparedStatement consulta = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM alquileres WHERE cliente_id = ?")) {
                consulta.setString(1, identificacion);
                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next() && resultado.getInt(1) > 0) {
                        throw new IllegalStateException(
                                "No se puede eliminar: el cliente tiene alquileres registrados en el historial.");
                    }
                }
            }
            try (PreparedStatement eliminar = conexion.prepareStatement(
                    "DELETE FROM clientes WHERE identificacion = ?")) {
                eliminar.setString(1, identificacion);
                return eliminar.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el cliente en MySQL: " + e.getMessage(), e);
        }
    }
}
