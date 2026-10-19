
package cr.ac.una.alquilervehiculos.repository;

import cr.ac.una.alquilervehiculos.database.ConexionBD;
import cr.ac.una.alquilervehiculos.interfaces.ClienteRepository;
import cr.ac.una.alquilervehiculos.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepositoryJDBC implements ClienteRepository {

    /**
     * Inserta un cliente en MySQL.
     * @param cliente datos del nuevo cliente.
     * @throws SQLException si falla la inserción.
     */
    @Override
    public void guardar(Cliente cliente) throws SQLException {

        String sql = """
                INSERT INTO clientes
                (identificacion, nombre, telefono, correo)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, cliente.getIdentificacion());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());

            ps.executeUpdate();
        }
    }

    /**
     * Busca un cliente por identificación.
     * @param identificacion identificación del cliente.
     * @return cliente encontrado o null.
     * @throws SQLException si falla la consulta.
     */
    @Override
    public Cliente buscarPorIdentificacion(String identificacion)
            throws SQLException {

        String sql = """
                SELECT identificacion, nombre, telefono, correo
                FROM clientes
                WHERE identificacion = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirCliente(rs);
                }
            }
        }

        return null;
    }

    /**
     * Consulta todos los clientes registrados.
     * @return lista de clientes.
     * @throws SQLException si falla la consulta.
     */
    @Override
    public List<Cliente> listar() throws SQLException {

        List<Cliente> clientes = new ArrayList<>();

        String sql = """
                SELECT identificacion, nombre, telefono, correo
                FROM clientes
                ORDER BY nombre
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(convertirCliente(rs));
            }
        }

        return clientes;
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param cliente datos actualizados.
     * @return true si se actualizó el cliente.
     * @throws SQLException si falla la operación.
     */
    @Override
    public boolean actualizar(Cliente cliente)
            throws SQLException {

        String sql = """
                UPDATE clientes
                SET nombre = ?, telefono = ?, correo = ?
                WHERE identificacion = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getIdentificacion());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un cliente por identificación.
     * @param identificacion identificación del cliente.
     * @return true si se eliminó el cliente.
     * @throws SQLException si falla la operación.
     */
    @Override
    public boolean eliminar(String identificacion)
            throws SQLException {

        String sql =
                "DELETE FROM clientes WHERE identificacion = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, identificacion);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Convierte una fila SQL en un objeto Cliente.
     * @param rs resultado de la consulta.
     * @return objeto Cliente.
     * @throws SQLException si falla la lectura.
     */
    private Cliente convertirCliente(ResultSet rs)
            throws SQLException {

        return new Cliente(
                rs.getString("identificacion"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("correo")
        );
    }
}
