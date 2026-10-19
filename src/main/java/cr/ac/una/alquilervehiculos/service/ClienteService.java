
package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.interfaces.ClienteRepository;
import cr.ac.una.alquilervehiculos.model.Cliente;
import cr.ac.una.alquilervehiculos.repository.ClienteRepositoryJDBC;

import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteRepository clientes;

    /**
     * Crea el servicio utilizando el repositorio JDBC.
     */
    public ClienteService() {
        clientes = new ClienteRepositoryJDBC();
    }

    /**
     * Registra un cliente nuevo en MySQL.
     *
     * @param cliente cliente que se desea registrar.
     */
    public void registrarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Debe ingresar un cliente."
            );
        }

        if (cliente.getIdentificacion() == null
                || cliente.getIdentificacion().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La identificación es obligatoria."
            );
        }

        if (cliente.getNombre() == null
                || cliente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (buscarPorIdentificacion(
                cliente.getIdentificacion()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con esa identificación."
            );
        }

        try {
            clientes.guardar(cliente);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al registrar el cliente en MySQL.", e
            );
        }
    }

    /**
     * Busca un cliente por su identificación.
     *
     * @param identificacion identificación del cliente.
     * @return cliente encontrado o null si no existe.
     */
    public Cliente buscarPorIdentificacion(String identificacion) {

        if (identificacion == null) {
            return null;
        }

        try {
            return clientes.buscarPorIdentificacion(identificacion);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al buscar el cliente en MySQL.", e
            );
        }
    }

    /**
     * Obtiene todos los clientes registrados en MySQL.
     *
     * @return lista de clientes.
     */
    public List<Cliente> listarClientes() {

        try {
            return clientes.listar();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al consultar los clientes en MySQL.", e
            );
        }
    }

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param identificacion identificación del cliente.
     * @param nombre nuevo nombre.
     * @param telefono nuevo teléfono.
     * @param correo nuevo correo.
     * @return true si se actualizó, false si no existe.
     */
    public boolean actualizarCliente(
            String identificacion,
            String nombre,
            String telefono,
            String correo) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        Cliente cliente = buscarPorIdentificacion(identificacion);

        if (cliente == null) {
            return false;
        }

        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setCorreo(correo);

        try {
            return clientes.actualizar(cliente);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al actualizar el cliente en MySQL.", e
            );
        }
    }

    /**
     * Elimina un cliente según su identificación.
     *
     * @param identificacion identificación del cliente.
     * @return true si se eliminó, false si no existe.
     */
    public boolean eliminarCliente(String identificacion) {

        if (identificacion == null
                || identificacion.trim().isEmpty()) {
            return false;
        }

        try {
            return clientes.eliminar(identificacion);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al eliminar el cliente en MySQL.", e
            );
        }
    }
}
