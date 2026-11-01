package cr.ac.una.alquilervehiculos.service;

import java.sql.SQLException;

/** Convierte errores comunes de MySQL en mensajes claros para el usuario. */
public final class ErroresBD {
    private ErroresBD() { }

    public static IllegalStateException convertir(SQLException error, String accion) {
        String estado = error.getSQLState();
        int codigo = error.getErrorCode();
        String mensaje;
        if ("28000".equals(estado) || codigo == 1045) {
            mensaje = "No se pudo acceder a MySQL. Revise el usuario y la variable MYSQL_PASSWORD.";
        } else if ((estado != null && estado.startsWith("08")) || codigo == 2003) {
            mensaje = "No hay conexión con MySQL. Verifique que esté iniciado e intente nuevamente.";
        } else if (codigo == 1049 || codigo == 1146) {
            mensaje = "No se encuentra la base de datos o alguna tabla. Revise la instalación de MySQL.";
        } else if (codigo == 1062) {
            mensaje = "Ya existe un registro con esa identificación o placa.";
        } else if (codigo == 1451) {
            mensaje = "No se puede eliminar el registro porque tiene alquileres en el historial.";
        } else if (codigo == 1452) {
            mensaje = "El cliente o vehículo seleccionado ya no existe. Actualice los datos.";
        } else if (codigo == 3819) {
            mensaje = "La operación no cumple las restricciones de la base de datos.";
        } else if (error.getMessage() != null
                && error.getMessage().startsWith("El vehículo no está disponible")) {
            mensaje = "El vehículo ya no está disponible. Actualice los datos e intente de nuevo.";
        } else {
            mensaje = "No se pudo " + accion + " en MySQL. Intente nuevamente.";
        }
        return new IllegalStateException(mensaje, error);
    }
}
