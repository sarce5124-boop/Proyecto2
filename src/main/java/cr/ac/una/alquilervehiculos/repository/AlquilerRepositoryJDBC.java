package cr.ac.una.alquilervehiculos.repository;

import cr.ac.una.alquilervehiculos.database.ConexionBD;
import cr.ac.una.alquilervehiculos.interfaces.AlquilerRepository;
import cr.ac.una.alquilervehiculos.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Repositorio JDBC; cada alquiler/devolución actualiza el vehículo atómicamente. */
public class AlquilerRepositoryJDBC implements AlquilerRepository {

    /** Inserta un alquiler solo si el vehículo puede reservarse. */
    @Override
    public Alquiler registrar(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio,
                             int dias, double depositoGarantia) throws SQLException {
        String reservar = "UPDATE vehiculos SET estado = 'ALQUILADO' "
                + "WHERE placa = ? AND estado = 'DISPONIBLE'";
        String insertar = "INSERT INTO alquileres (cliente_id, vehiculo_placa, fecha_inicio, "
                + "dias, deposito_garantia, costo_alquiler, finalizado, fecha_devolucion_real) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0, NULL)";
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(reservar)) {
                    ps.setString(1, vehiculo.getPlaca());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("El vehículo no está disponible en MySQL.");
                    }
                }
                int id;
                try (PreparedStatement ps = conexion.prepareStatement(insertar,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cliente.getIdentificacion());
                    ps.setString(2, vehiculo.getPlaca());
                    ps.setDate(3, Date.valueOf(fechaInicio));
                    ps.setInt(4, dias);
                    ps.setDouble(5, depositoGarantia);
                    ps.setDouble(6, vehiculo.calcularCostoAlquiler(dias));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL no devolvió el ID del alquiler.");
                        id = keys.getInt(1);
                    }
                }
                conexion.commit();
                vehiculo.setEstado(EstadoVehiculo.ALQUILADO);
                return new Alquiler(id, cliente, vehiculo, fechaInicio, dias, depositoGarantia);
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /** Finaliza un alquiler activo y libera su vehículo en la misma transacción. */
    @Override
    public boolean finalizar(int id, String placa, LocalDate fechaDevolucionReal)
            throws SQLException {
        String finalizar = "UPDATE alquileres SET finalizado = 1, fecha_devolucion_real = ? "
                + "WHERE id = ? AND vehiculo_placa = ? AND finalizado = 0";
        String liberar = "UPDATE vehiculos SET estado = 'DISPONIBLE' "
                + "WHERE placa = ? AND estado = 'ALQUILADO'";
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int modificados;
                try (PreparedStatement ps = conexion.prepareStatement(finalizar)) {
                    ps.setDate(1, Date.valueOf(fechaDevolucionReal));
                    ps.setInt(2, id);
                    ps.setString(3, placa);
                    modificados = ps.executeUpdate();
                }
                if (modificados == 0) {
                    conexion.rollback();
                    return false;
                }
                try (PreparedStatement ps = conexion.prepareStatement(liberar)) {
                    ps.setString(1, placa);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No fue posible liberar el vehículo en MySQL.");
                    }
                }
                conexion.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /** Obtiene todos los alquileres con su cliente y vehículo asociados. */
    @Override
    public List<Alquiler> listar() throws SQLException {
        List<Alquiler> resultado = new ArrayList<>();
        String sql = "SELECT * FROM alquileres ORDER BY id";
        ClienteRepositoryJDBC clientes = new ClienteRepositoryJDBC();
        VehiculoRepositoryJDBC vehiculos = new VehiculoRepositoryJDBC();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(convertir(rs, clientes, vehiculos));
            }
        }
        return resultado;
    }

    /** Busca un alquiler por su identificador. */
    @Override
    public Alquiler buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM alquileres WHERE id = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return convertir(rs, new ClienteRepositoryJDBC(), new VehiculoRepositoryJDBC());
            }
        }
    }

    /** Reconstruye el alquiler y preserva su costo de contratación. */
    private Alquiler convertir(ResultSet rs, ClienteRepositoryJDBC clientes,
                              VehiculoRepositoryJDBC vehiculos) throws SQLException {
        Cliente cliente = clientes.buscarPorIdentificacion(rs.getString("cliente_id"));
        Vehiculo vehiculo = vehiculos.buscarPorPlaca(rs.getString("vehiculo_placa"));
        if (cliente == null || vehiculo == null) {
            throw new SQLException("Alquiler " + rs.getInt("id") + " con referencias inválidas.");
        }
        Date fechaReal = rs.getDate("fecha_devolucion_real");
        boolean finalizado = rs.getBoolean("finalizado");
        if (finalizado != (fechaReal != null)) {
            throw new SQLException("Estado de finalización inconsistente en alquiler " + rs.getInt("id"));
        }
        return new Alquiler(rs.getInt("id"), cliente, vehiculo,
                rs.getDate("fecha_inicio").toLocalDate(), rs.getInt("dias"),
                rs.getDouble("deposito_garantia"), rs.getDouble("costo_alquiler"),
                fechaReal == null ? null : fechaReal.toLocalDate());
    }
}
