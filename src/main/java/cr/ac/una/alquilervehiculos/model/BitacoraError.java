
package cr.ac.una.alquilervehiculos.model;

import java.time.LocalDateTime;

public class BitacoraError {

    private int idError;
    private LocalDateTime fechaHora;
    private String modulo;
    private String operacion;
    private String tipoError;
    private String descripcion;

    // Crea un registro de error.
    public BitacoraError(int idError, LocalDateTime fechaHora,
                         String modulo, String operacion,
                         String tipoError, String descripcion) {
        this.idError = idError;
        this.fechaHora = fechaHora;
        this.modulo = modulo;
        this.operacion = operacion;
        this.tipoError = tipoError;
        this.descripcion = descripcion;
    }

    public int getIdError() {
        return idError;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getModulo() {
        return modulo;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getTipoError() {
        return tipoError;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Muestra el error de forma sencilla en la bitácora.
    @Override
    public String toString() {
        return String.format(
                "%s | %s | %s | %s | %s",
                fechaHora, modulo, operacion, tipoError, descripcion
        );
    }
}
