package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.interfaces.AlquilerRepository;
import cr.ac.una.alquilervehiculos.model.*;
import cr.ac.una.alquilervehiculos.repository.AlquilerRepositoryJDBC;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reglas de negocio de alquileres y devoluciones persistidas en MySQL. */
public class AlquilerService {
    private final AlquilerRepository alquileres;

    /** Inicializa el repositorio JDBC. */
    public AlquilerService() {
        alquileres = new AlquilerRepositoryJDBC();
    }

    /** Valida y registra el alquiler de manera transaccional. */
    public Alquiler registrarAlquiler(Cliente cliente, Vehiculo vehiculo,
                                    LocalDate fechaInicio, int dias, double depositoGarantia) {
        if (cliente == null) throw new IllegalArgumentException("Debe seleccionar un cliente.");
        if (vehiculo == null) throw new IllegalArgumentException("Debe seleccionar un vehículo.");
        if (fechaInicio == null) throw new IllegalArgumentException("Debe seleccionar una fecha de inicio.");
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE)
            throw new IllegalStateException("El vehículo no está disponible.");
        if (dias <= 0) throw new IllegalArgumentException("La cantidad de días debe ser mayor a cero.");
        if (!Double.isFinite(depositoGarantia) || depositoGarantia <= 0)
            throw new IllegalArgumentException("El depósito de garantía debe ser mayor a cero.");
        try {
            return alquileres.registrar(cliente, vehiculo, fechaInicio, dias, depositoGarantia);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar el alquiler en MySQL: " + e.getMessage(), e);
        }
    }

    /** Cobra una tarifa diaria por cada día de atraso. */
    public double calcularMulta(Alquiler alquiler, LocalDate fechaDevolucionReal) {
        validarAlquiler(alquiler);
        if (fechaDevolucionReal == null)
            throw new IllegalArgumentException("Debe indicar la fecha de devolución.");
        return alquiler.calcularDiasAtraso(fechaDevolucionReal)
                * alquiler.getVehiculo().getTarifaDiaria();
    }

    /** Devuelve el depósito íntegro; la multa se cobra por separado. */
    public double calcularDepositoADevolver(Alquiler alquiler, LocalDate fechaDevolucionReal) {
        validarAlquiler(alquiler);
        alquiler.calcularDiasAtraso(fechaDevolucionReal);
        return alquiler.getDepositoGarantia();
    }

    /** Devuelve el monto de multa pendiente. */
    public double calcularMontoPendiente(Alquiler alquiler, LocalDate fechaDevolucionReal) {
        return calcularMulta(alquiler, fechaDevolucionReal);
    }

    /** Finaliza en MySQL y libera el vehículo; después actualiza el objeto en pantalla. */
    public double devolverVehiculo(Alquiler alquiler, LocalDate fechaDevolucionReal) {
        validarAlquiler(alquiler);
        if (fechaDevolucionReal == null)
            throw new IllegalArgumentException("Debe indicar la fecha de devolución.");
        if (alquiler.isFinalizado())
            throw new IllegalStateException("Este alquiler ya fue finalizado.");
        double multa = calcularMulta(alquiler, fechaDevolucionReal);
        try {
            if (!alquileres.finalizar(alquiler.getId(), alquiler.getVehiculo().getPlaca(), fechaDevolucionReal))
                throw new IllegalStateException("El alquiler ya no está activo en MySQL.");
            alquiler.finalizarAlquiler(fechaDevolucionReal);
            alquiler.getVehiculo().setEstado(EstadoVehiculo.DISPONIBLE);
            return multa;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar la devolución en MySQL: " + e.getMessage(), e);
        }
    }

    /** Obtiene todos los alquileres de MySQL. */
    public List<Alquiler> listarAlquileres() {
        try {
            return alquileres.listar();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar los alquileres en MySQL: " + e.getMessage(), e);
        }
    }

    /** Obtiene únicamente los alquileres activos. */
    public List<Alquiler> listarAlquileresActivos() {
        List<Alquiler> activos = new ArrayList<>();
        for (Alquiler alquiler : listarAlquileres())
            if (!alquiler.isFinalizado()) activos.add(alquiler);
        return activos;
    }

    /** Obtiene únicamente los alquileres finalizados. */
    public List<Alquiler> listarAlquileresFinalizados() {
        List<Alquiler> finalizados = new ArrayList<>();
        for (Alquiler alquiler : listarAlquileres())
            if (alquiler.isFinalizado()) finalizados.add(alquiler);
        return finalizados;
    }

    /** Busca un alquiler por ID. */
    public Alquiler buscarPorId(int id) {
        try {
            return alquileres.buscarPorId(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el alquiler en MySQL: " + e.getMessage(), e);
        }
    }

    /** Valida que exista un alquiler seleccionado. */
    private void validarAlquiler(Alquiler alquiler) {
        if (alquiler == null) throw new IllegalArgumentException("Debe seleccionar un alquiler.");
    }
}
