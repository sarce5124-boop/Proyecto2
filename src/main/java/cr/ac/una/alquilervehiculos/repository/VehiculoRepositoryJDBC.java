package cr.ac.una.alquilervehiculos.repository;

import cr.ac.una.alquilervehiculos.database.ConexionBD;
import cr.ac.una.alquilervehiculos.interfaces.VehiculoRepository;
import cr.ac.una.alquilervehiculos.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC del repositorio de vehículos. */
public class VehiculoRepositoryJDBC implements VehiculoRepository {

    /** Guarda un vehículo nuevo con su subtipo y características. */
    @Override
    public void guardar(Vehiculo vehiculo) throws SQLException {
        String sql = "INSERT INTO vehiculos (placa, marca, tarifa_diaria, estado, tipo, "
                + "cilindrada, tipo_motor, tipo_traccion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setDouble(3, vehiculo.getTarifaDiaria());
            ps.setString(4, vehiculo.getEstado().name());
            if (vehiculo instanceof Moto moto) {
                ps.setString(5, "MOTO");
                ps.setInt(6, moto.getCilindrada());
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            } else if (vehiculo instanceof Bicimoto bicimoto) {
                ps.setString(5, "BICIMOTO");
                ps.setNull(6, Types.INTEGER);
                ps.setString(7, bicimoto.getTipoMotor());
                ps.setNull(8, Types.VARCHAR);
            } else if (vehiculo instanceof Cuadraciclo cuadraciclo) {
                ps.setString(5, "CUADRACICLO");
                ps.setNull(6, Types.INTEGER);
                ps.setNull(7, Types.VARCHAR);
                ps.setString(8, cuadraciclo.getTipoTraccion());
            } else {
                throw new IllegalArgumentException("Tipo de vehículo no soportado.");
            }
            ps.executeUpdate();
        }
    }

    /** Busca un vehículo por su placa. */
    @Override
    public Vehiculo buscarPorPlaca(String placa) throws SQLException {
        String sql = "SELECT * FROM vehiculos WHERE placa = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertirVehiculo(rs) : null;
            }
        }
    }

    /** Lista los vehículos registrados. */
    @Override
    public List<Vehiculo> listar() throws SQLException {
        List<Vehiculo> resultado = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos ORDER BY placa";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(convertirVehiculo(rs));
            }
        }
        return resultado;
    }

    /** Actualiza marca, tarifa y estado de un vehículo. */
    @Override
    public boolean actualizar(Vehiculo vehiculo) throws SQLException {
        String sql = "UPDATE vehiculos SET marca = ?, tarifa_diaria = ?, estado = ? WHERE placa = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMarca());
            ps.setDouble(2, vehiculo.getTarifaDiaria());
            ps.setString(3, vehiculo.getEstado().name());
            ps.setString(4, vehiculo.getPlaca());
            return ps.executeUpdate() > 0;
        }
    }

    /** Elimina un vehículo por su placa. */
    @Override
    public boolean eliminar(String placa) throws SQLException {
        String sql = "DELETE FROM vehiculos WHERE placa = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, placa);
            return ps.executeUpdate() > 0;
        }
    }

    /** Reconstruye el subtipo de vehículo a partir de una fila SQL. */
    private Vehiculo convertirVehiculo(ResultSet rs) throws SQLException {
        String placa = rs.getString("placa");
        String marca = rs.getString("marca");
        double tarifa = rs.getDouble("tarifa_diaria");
        String tipo = rs.getString("tipo");
        Vehiculo vehiculo;
        switch (tipo) {
            case "MOTO" -> vehiculo = new Moto(placa, marca, tarifa, rs.getInt("cilindrada"));
            case "BICIMOTO" -> vehiculo = new Bicimoto(placa, marca, tarifa, rs.getString("tipo_motor"));
            case "CUADRACICLO" -> vehiculo = new Cuadraciclo(placa, marca, tarifa, rs.getString("tipo_traccion"));
            default -> throw new SQLException("Tipo de vehículo desconocido: " + tipo);
        }
        try {
            vehiculo.setEstado(EstadoVehiculo.valueOf(rs.getString("estado")));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Estado de vehículo inválido para " + placa, e);
        }
        return vehiculo;
    }
}
