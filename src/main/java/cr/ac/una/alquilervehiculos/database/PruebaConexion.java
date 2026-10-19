package cr.ac.una.alquilervehiculos.database;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    /**
     * Comprueba que Java puede conectarse a MySQL.
     *
     * @param args argumentos de ejecución.
     */
    public static void main(String[] args) {

        System.out.println("Probando conexión a MySQL...");

        try (Connection conexion = ConexionBD.conectar()) {

            if (conexion.isValid(5)) {

                System.out.println("¡CONEXIÓN EXITOSA!");

                System.out.println(
                        "Base de datos: " + conexion.getCatalog()
                );

            } else {

                System.out.println("La conexión no es válida.");
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error de conexión: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
