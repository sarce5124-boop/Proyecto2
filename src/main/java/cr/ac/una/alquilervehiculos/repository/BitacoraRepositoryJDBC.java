
package cr.ac.una.alquilervehiculos.repository;

import cr.ac.una.alquilervehiculos.database.ConexionBD;
import cr.ac.una.alquilervehiculos.model.BitacoraError;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BitacoraRepositoryJDBC {

    // Guarda un error de ingreso de datos en MySQL.
    public void registrar(String modulo, String operacion,
                          String tipoError, String descripcion) {

        String sql = """
                INSERT INTO bitacora_errores
                (modulo, operacion, tipo_error, descripcion)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, modulo);
            ps.setString(2, operacion);
            ps.setString(3, tipoError);
            ps.setString(4, descripcion);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                    "No se pudo guardar el error en la bitácora: "
                            + e.getMessage()
            );
        }
    }

    // Consulta los errores registrados, del más reciente al más antiguo.
    public List<BitacoraError> listar() {

        List<BitacoraError> errores = new ArrayList<>();

        String sql = """
                SELECT id_error, fecha_hora, modulo,
                       operacion, tipo_error, descripcion
                FROM bitacora_errores
                ORDER BY fecha_hora DESC, id_error DESC
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                errores.add(new BitacoraError(
                        rs.getInt("id_error"),
                        rs.getTimestamp("fecha_hora").toLocalDateTime(),
                        rs.getString("modulo"),
                        rs.getString("operacion"),
                        rs.getString("tipo_error"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "No se pudo consultar la bitácora: "
                            + e.getMessage()
            );
        }

        return errores;
    }
}
