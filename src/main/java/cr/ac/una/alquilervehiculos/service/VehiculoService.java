package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.interfaces.VehiculoRepository;
import cr.ac.una.alquilervehiculos.model.EstadoVehiculo;
import cr.ac.una.alquilervehiculos.model.Vehiculo;
import cr.ac.una.alquilervehiculos.repository.VehiculoRepositoryJDBC;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reglas de negocio y acceso a datos de vehículos. */
public class VehiculoService {
    private final VehiculoRepository vehiculos;

    /** Crea el servicio conectado a MySQL. */
    public VehiculoService() {
        vehiculos = new VehiculoRepositoryJDBC();
    }

    /** Valida y registra un vehículo. */
    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) throw new IllegalArgumentException("Debe ingresar un vehículo.");
        if (vehiculo.getPlaca() == null || vehiculo.getPlaca().trim().isEmpty())
            throw new IllegalArgumentException("La placa es obligatoria.");
        if (vehiculo.getMarca() == null || vehiculo.getMarca().trim().isEmpty())
            throw new IllegalArgumentException("La marca es obligatoria.");
        if (!Double.isFinite(vehiculo.getTarifaDiaria()) || vehiculo.getTarifaDiaria() <= 0)
            throw new IllegalArgumentException("La tarifa diaria debe ser mayor a cero.");
        if (buscarPorPlaca(vehiculo.getPlaca()) != null)
            throw new IllegalArgumentException("Ya existe un vehículo con esa placa.");
        try {
            vehiculos.guardar(vehiculo);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al registrar el vehículo en MySQL.", e);
        }
    }

    /** Busca un vehículo por placa. */
    public Vehiculo buscarPorPlaca(String placa) {
        if (placa == null) return null;
        try {
            return vehiculos.buscarPorPlaca(placa);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el vehículo en MySQL.", e);
        }
    }

    /** Devuelve todos los vehículos. */
    public List<Vehiculo> listarVehiculos() {
        try {
            return vehiculos.listar();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar los vehículos en MySQL.", e);
        }
    }

    /** Devuelve los vehículos disponibles. */
    public List<Vehiculo> listarDisponibles() {
        List<Vehiculo> disponibles = new ArrayList<>();
        for (Vehiculo vehiculo : listarVehiculos()) {
            if (vehiculo.getEstado() == EstadoVehiculo.DISPONIBLE) disponibles.add(vehiculo);
        }
        return disponibles;
    }

    /** Actualiza marca y tarifa de un vehículo. */
    public boolean actualizarVehiculo(String placa, String marca, double tarifaDiaria) {
        Vehiculo vehiculo = buscarPorPlaca(placa);
        if (vehiculo == null) return false;
        if (marca == null || marca.trim().isEmpty())
            throw new IllegalArgumentException("La marca es obligatoria.");
        if (!Double.isFinite(tarifaDiaria) || tarifaDiaria <= 0)
            throw new IllegalArgumentException("La tarifa diaria debe ser mayor a cero.");
        vehiculo.setMarca(marca);
        vehiculo.setTarifaDiaria(tarifaDiaria);
        return actualizarEnBD(vehiculo);
    }

    /** Cambia y persiste el estado de un vehículo. */
    public boolean cambiarEstado(String placa, EstadoVehiculo estado) {
        if (estado == null) throw new IllegalArgumentException("Debe indicar un estado válido.");
        Vehiculo vehiculo = buscarPorPlaca(placa);
        if (vehiculo == null) return false;
        vehiculo.setEstado(estado);
        return actualizarEnBD(vehiculo);
    }

    /** Elimina un vehículo si no está alquilado. */
    public boolean eliminarVehiculo(String placa) {
        Vehiculo vehiculo = buscarPorPlaca(placa);
        if (vehiculo == null) return false;
        if (vehiculo.getEstado() == EstadoVehiculo.ALQUILADO)
            throw new IllegalStateException("No se puede eliminar un vehículo alquilado.");
        try {
            return vehiculos.eliminar(placa);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar el vehículo en MySQL.", e);
        }
    }

    /** Ejecuta una actualización de vehículo en MySQL. */
    private boolean actualizarEnBD(Vehiculo vehiculo) {
        try {
            return vehiculos.actualizar(vehiculo);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al actualizar el vehículo en MySQL.", e);
        }
    }
}
