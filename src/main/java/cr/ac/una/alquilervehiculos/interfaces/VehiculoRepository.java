package cr.ac.una.alquilervehiculos.interfaces;

import cr.ac.una.alquilervehiculos.model.Vehiculo;
import java.sql.SQLException;
import java.util.List;

/** Contrato de persistencia de vehículos. */
public interface VehiculoRepository {
    void guardar(Vehiculo vehiculo) throws SQLException;
    Vehiculo buscarPorPlaca(String placa) throws SQLException;
    List<Vehiculo> listar() throws SQLException;
    boolean actualizar(Vehiculo vehiculo) throws SQLException;
    boolean eliminar(String placa) throws SQLException;
}
