
package cr.ac.una.alquilervehiculos.database;

import cr.ac.una.alquilervehiculos.model.Cliente;
import cr.ac.una.alquilervehiculos.repository.ClienteRepositoryJDBC;

import java.sql.SQLException;

public class PruebaClienteCRUD {

    /**
     * Prueba las operaciones CRUD de clientes.
     *
     * @param args argumentos de ejecución.
     */
    public static void main(String[] args) {

        ClienteRepositoryJDBC repo =
                new ClienteRepositoryJDBC();

        String identificacion = "PRUEBA001";

        try {
            // 1. CREAR
            System.out.println("1. CREAR CLIENTE");

            Cliente cliente = new Cliente(
                    identificacion,
                    "Cliente de Prueba",
                    "88888888",
                    "prueba@ejemplo.com"
            );

            repo.guardar(cliente);
            System.out.println("Cliente guardado correctamente.");

            // 2. LEER
            System.out.println("\n2. BUSCAR CLIENTE");

            Cliente encontrado =
                    repo.buscarPorIdentificacion(identificacion);

            if (encontrado == null) {
                throw new SQLException(
                        "No se encontró el cliente registrado."
                );
            }

            System.out.println("Encontrado: " + encontrado);

            // 3. ACTUALIZAR
            System.out.println("\n3. ACTUALIZAR CLIENTE");

            encontrado.setNombre("Cliente Actualizado");
            encontrado.setTelefono("87777777");

            boolean actualizado = repo.actualizar(encontrado);

            System.out.println(
                    "Actualización exitosa: " + actualizado
            );

            // 4. LISTAR
            System.out.println("\n4. LISTAR CLIENTES");

            for (Cliente c : repo.listar()) {
                System.out.println(c);
            }

            // 5. ELIMINAR
            System.out.println("\n5. ELIMINAR CLIENTE");

            boolean eliminado = repo.eliminar(identificacion);

            System.out.println(
                    "Eliminación exitosa: " + eliminado
            );

            System.out.println("\nPRUEBA CRUD FINALIZADA");

        } catch (SQLException e) {

            System.err.println(
                    "Error en CRUD: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
