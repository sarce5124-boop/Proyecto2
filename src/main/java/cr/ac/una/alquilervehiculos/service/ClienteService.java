
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

        cliente.setIdentificacion(Validaciones.identificacion(cliente.getIdentificacion()));
        cliente.setNombre(Validaciones.nombre(cliente.getNombre()));
        cliente.setTelefono(Validaciones.telefono(cliente.getTelefono()));
        cliente.setCorreo(Validaciones.correo(cliente.getCorreo()));

        if (buscarPorIdentificacion(cliente.getIdentificacion()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con esa identificación."
            );
        }

        try {
            clientes.guardar(cliente);

        } catch (SQLException e) {
            throw ErroresBD.convertir(e, "registrar el cliente");
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
            throw ErroresBD.convertir(e, "buscar el cliente");
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
            throw ErroresBD.convertir(e, "consultar los clientes");
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

        String nombreLimpio = Validaciones.nombre(nombre);
        String telefonoLimpio = Validaciones.telefono(telefono);
        String correoLimpio = Validaciones.correo(correo);

        Cliente cliente = buscarPorIdentificacion(identificacion);

        if (cliente == null) {
            return false;
        }

        cliente.setNombre(nombreLimpio);
        cliente.setTelefono(telefonoLimpio);
        cliente.setCorreo(correoLimpio);

        try {
            return clientes.actualizar(cliente);

        } catch (SQLException e) {
            throw ErroresBD.convertir(e, "actualizar el cliente");
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
            throw ErroresBD.convertir(e, "eliminar el cliente");
        }
    }
}
