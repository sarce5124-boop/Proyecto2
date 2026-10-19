
package cr.ac.una.alquilervehiculos.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/alquiler_vehiculos";

    private static final String USUARIO = "alquiler_app";

    private static final String PASSWORD =
            System.getenv("MYSQL_PASSWORD");

    /**
     * Establece una conexión con MySQL.
     *
     * @return conexión activa a la base de datos.
     * @throws SQLException si falla la conexión.
     */
    public static Connection conectar() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }
}
