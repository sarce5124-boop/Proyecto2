package cr.ac.una.alquilervehiculos.interfaces;

import cr.ac.una.alquilervehiculos.model.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface ClienteRepository {

    /**
     * Guarda un nuevo cliente.
     * @param cliente cliente que se desea registrar.
     * @throws SQLException si falla la operación.
     */
    void guardar(Cliente cliente) throws SQLException;

    /**
     * Busca un cliente por identificación.
     * @param identificacion identificación a buscar.
     * @return cliente encontrado o null.
     * @throws SQLException si falla la consulta.
     */
    Cliente buscarPorIdentificacion(String identificacion)
            throws SQLException;

    /**
     * Obtiene todos los clientes.
     * @return lista de clientes registrados.
     * @throws SQLException si falla la consulta.
     */
    List<Cliente> listar() throws SQLException;

    /**
     * Actualiza los datos de un cliente.
     * @param cliente cliente con los nuevos datos.
     * @return true si se actualizó algún registro.
     * @throws SQLException si falla la operación.
     */
    boolean actualizar(Cliente cliente) throws SQLException;

    /**
     * Elimina un cliente por identificación.
     * @param identificacion identificación del cliente.
     * @return true si se eliminó algún registro.
     * @throws SQLException si falla la operación.
     */
    boolean eliminar(String identificacion) throws SQLException;
}
