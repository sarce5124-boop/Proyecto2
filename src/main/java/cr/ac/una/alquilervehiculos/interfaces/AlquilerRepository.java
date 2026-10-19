package cr.ac.una.alquilervehiculos.interfaces;

import cr.ac.una.alquilervehiculos.model.Alquiler;
import cr.ac.una.alquilervehiculos.model.Cliente;
import cr.ac.una.alquilervehiculos.model.Vehiculo;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Contrato de persistencia transaccional de alquileres. */
public interface AlquilerRepository {
    Alquiler registrar(Cliente cliente, Vehiculo vehiculo, LocalDate fechaInicio,
                      int dias, double depositoGarantia) throws SQLException;
    boolean finalizar(int id, String placa, LocalDate fechaDevolucionReal) throws SQLException;
    List<Alquiler> listar() throws SQLException;
    Alquiler buscarPorId(int id) throws SQLException;
}
