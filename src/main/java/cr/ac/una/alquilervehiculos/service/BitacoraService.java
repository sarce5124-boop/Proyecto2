
package cr.ac.una.alquilervehiculos.service;

import cr.ac.una.alquilervehiculos.model.BitacoraError;
import cr.ac.una.alquilervehiculos.repository.BitacoraRepositoryJDBC;

import java.util.List;
import java.util.Locale;

public class BitacoraService {

    private final BitacoraRepositoryJDBC repository =
            new BitacoraRepositoryJDBC();

    // Registra errores de clientes e inventario.
    public void registrarError(String modulo, String operacion,
                               String descripcion) {

        if (!"Clientes".equals(modulo)
                && !"Inventario".equals(modulo)) {
            return;
        }

        String mensaje = descripcion == null
                ? ""
                : descripcion.toLowerCase(Locale.ROOT);

        String tipoError;
        String descripcionSegura;

        // Identifica los campos obligatorios.
        if (mensaje.contains("identificación")
                && mensaje.contains("ingresar")) {

            tipoError = "Identificación obligatoria";
            descripcionSegura = "Debe ingresar la identificación del cliente.";

        } else if (mensaje.contains("nombre")
                && mensaje.contains("ingresar")) {

            tipoError = "Nombre obligatorio";
            descripcionSegura = "Debe ingresar el nombre del cliente.";

        } else if (mensaje.contains("placa")
                && mensaje.contains("ingresar")) {

            tipoError = "Placa obligatoria";
            descripcionSegura = "Debe ingresar la placa del vehículo.";

        } else if (mensaje.contains("marca")
                && mensaje.contains("ingresar")) {

            tipoError = "Marca obligatoria";
            descripcionSegura = "Debe ingresar la marca del vehículo.";

        } else if (mensaje.contains("característica")
                && mensaje.contains("ingresar")) {

            tipoError = "Característica obligatoria";
            descripcionSegura = "Debe ingresar la característica del vehículo.";

            // Identifica valores numéricos incorrectos.
        } else if (mensaje.contains("tarifa")) {

            tipoError = "Tarifa inválida";
            descripcionSegura =
                    "La tarifa diaria debe ser un número mayor que cero.";

        } else if (mensaje.contains("cilindrada")) {

            tipoError = "Cilindrada inválida";
            descripcionSegura =
                    "La cilindrada debe ser un número entero mayor que cero.";

            // Identifica registros duplicados.
        } else if (mensaje.contains("existe")
                || mensaje.contains("duplicad")) {

            tipoError = "Registro duplicado";
            descripcionSegura =
                    "Se intentó ingresar un registro que ya existe.";

            // Identifica otros errores de validación.
        } else if (mensaje.contains("inválid")
                || mensaje.contains("incorrect")
                || mensaje.contains("obligatorio")) {

            tipoError = "Dato inválido";
            descripcionSegura =
                    "Se ingresó información con un formato incorrecto.";

        } else {

            tipoError = "Validación";
            descripcionSegura =
                    "No se pudo completar la operación por datos inválidos.";
        }

        repository.registrar(
                modulo,
                operacion,
                tipoError,
                descripcionSegura
        );
    }

    // Consulta los errores registrados en MySQL.
    public List<BitacoraError> listarErrores() {
        return repository.listar();
    }
}
