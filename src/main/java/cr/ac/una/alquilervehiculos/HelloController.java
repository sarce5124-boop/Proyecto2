package cr.ac.una.alquilervehiculos;

import cr.ac.una.alquilervehiculos.network.SyncManager;
import cr.ac.una.alquilervehiculos.model.Alquiler;
import cr.ac.una.alquilervehiculos.model.Bicimoto;
import cr.ac.una.alquilervehiculos.model.Cliente;
import cr.ac.una.alquilervehiculos.model.Cuadraciclo;
import cr.ac.una.alquilervehiculos.model.Moto;
import cr.ac.una.alquilervehiculos.model.Vehiculo;
import cr.ac.una.alquilervehiculos.service.AlquilerService;
import cr.ac.una.alquilervehiculos.service.ClienteService;
import cr.ac.una.alquilervehiculos.service.ClienteEliminacionService;
import cr.ac.una.alquilervehiculos.service.VehiculoService;
import cr.ac.una.alquilervehiculos.service.Validaciones;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class HelloController {

    // Servicios de aplicación: conservan los datos y aplican las reglas del negocio.
    private final VehiculoService vehiculoService = new VehiculoService();
    private final ClienteService clienteService = new ClienteService();
    private final ClienteEliminacionService clienteEliminacionService = new ClienteEliminacionService();
    private final AlquilerService alquilerService = new AlquilerService();

    // Recibe avisos de otras ventanas y actualiza los datos desde MySQL.
    private final SyncManager syncManager = new SyncManager(
            evento -> Platform.runLater(this::actualizarListas)
    );

    // Controles de la sección de inventario.
    @FXML
    private ComboBox<String> tipoVehiculoCombo;
    @FXML
    private Label caracteristicaLabel;
    @FXML
    private TextField placaField;
    @FXML
    private TextField marcaField;
    @FXML
    private TextField tarifaField;
    @FXML
    private TextField caracteristicaField;
    @FXML
    private ListView<Vehiculo> vehiculosList;

    // Controles de la sección de clientes.
    @FXML
    private TextField identificacionField;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField correoField;
    @FXML
    private ListView<Cliente> clientesList;

    // Filtros y consulta del historial de alquileres.
    @FXML private TextField buscarClienteField;
    @FXML private TextField buscarVehiculoField;
    @FXML private ListView<Alquiler> historialAlquileresList;
    private List<Cliente> todosLosClientes = new ArrayList<>();
    private List<Vehiculo> todosLosVehiculos = new ArrayList<>();
    @FXML private Label estadoConexionLabel;
    private boolean datosCargados = false;
    private int numeroCarga = 0;

    /** Conjunto de datos que el hilo de MySQL devuelve a JavaFX de una sola vez. */
    private static class DatosSistema {
        private final List<Cliente> clientes;
        private final List<Vehiculo> vehiculos;
        private final List<Alquiler> alquileres;

        private DatosSistema(List<Cliente> clientes, List<Vehiculo> vehiculos,
                             List<Alquiler> alquileres) {
            this.clientes = clientes;
            this.vehiculos = vehiculos;
            this.alquileres = alquileres;
        }
    }

    // Controles de la sección de alquileres.
    @FXML
    private ComboBox<Cliente> clienteAlquilerCombo;
    @FXML
    private ComboBox<Vehiculo> vehiculoAlquilerCombo;
    @FXML
    private DatePicker fechaInicioPicker;
    @FXML
    private Spinner<Integer> diasSpinner;
    @FXML
    private TextField depositoField;
    @FXML
    private Label costoAlquilerLabel;
    @FXML
    private Label totalAlquilerLabel;
    @FXML
    private ListView<Alquiler> alquileresActivosList;

    // Controles de la sección de devoluciones.
    @FXML
    private ComboBox<Alquiler> devolucionCombo;
    @FXML
    private DatePicker fechaDevolucionPicker;
    @FXML
    private Label multaLabel;
    @FXML
    private Label depositoDevolverLabel;
    @FXML
    private Label totalDevolucionLabel;

    /**
     * Configura los valores iniciales de los controles y conecta los cambios
     * de la interfaz con los cálculos que deben actualizarse automáticamente
     * durante el uso del sistema.
     */
    @FXML
    private void initialize() {
        tipoVehiculoCombo.getItems().addAll(
                "Moto", "Bicimoto", "Cuadraciclo"
        );
        tipoVehiculoCombo.setEditable(false);
        clienteAlquilerCombo.setEditable(false);
        vehiculoAlquilerCombo.setEditable(false);
        devolucionCombo.setEditable(false);
        fechaInicioPicker.setEditable(false);
        fechaDevolucionPicker.setEditable(false);
        diasSpinner.setEditable(false);
        tipoVehiculoCombo.getSelectionModel().selectFirst();
        actualizarCaracteristica();

        fechaInicioPicker.setValue(LocalDate.now());
        fechaDevolucionPicker.setValue(LocalDate.now());
        diasSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1)
        );

        tipoVehiculoCombo.valueProperty().addListener(
                (observable, anterior, actual) -> actualizarCaracteristica()
        );
        vehiculoAlquilerCombo.valueProperty().addListener(
                (observable, anterior, actual) -> actualizarTotalAlquiler()
        );
        diasSpinner.valueProperty().addListener(
                (observable, anterior, actual) -> actualizarTotalAlquiler()
        );
        depositoField.textProperty().addListener(
                (observable, anterior, actual) -> actualizarTotalAlquiler()
        );
        devolucionCombo.valueProperty().addListener(
                (observable, anterior, actual) -> calcularDevolucion()
        );
        fechaDevolucionPicker.valueProperty().addListener(
                (observable, anterior, actual) -> calcularDevolucion()
        );

        buscarClienteField.textProperty().addListener((obs, anterior, actual) -> filtrarClientes());
        buscarVehiculoField.textProperty().addListener((obs, anterior, actual) -> filtrarVehiculos());
        actualizarListas();
        syncManager.iniciar();
    }

    /**
     * Valida los datos ingresados y registra un cliente nuevo.
     *
     * <p>La identificación y el nombre son obligatorios. El teléfono y el
     * correo se almacenan como datos complementarios del cliente. Después de
     * registrarlo, se limpian los campos y se actualizan las listas y combos
     * que utilizan clientes.</p>
     */
    @FXML
    private void registrarCliente() {
        try {
            comprobarDatosCargados();
            validarTexto(identificacionField.getText(), "la identificación");
            validarTexto(nombreField.getText(), "el nombre");

            Cliente cliente = new Cliente(
                    identificacionField.getText().trim(),
                    nombreField.getText().trim(),
                    telefonoField.getText().trim(),
                    correoField.getText().trim()
            );
            clienteService.registrarCliente(cliente);
            limpiarCamposCliente();
            syncManager.notificarCambio("CLIENTE", "CREAR");
            actualizarListas();
            mostrarInformacion("Cliente registrado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    /**
     * Construye y registra un vehículo según el tipo seleccionado.
     *
     * <p>Todos los vehículos requieren placa, marca y tarifa diaria. Además,
     * cada subtipo solicita su característica particular: cilindrada para
     * motos, tipo de motor para bicimotos y tipo de tracción para
     * cuadraciclos.</p>
     */
    @FXML
    private void registrarVehiculo() {
        try {
            comprobarDatosCargados();
            String tipo = tipoVehiculoCombo.getValue();
            if (tipo == null || !tipoVehiculoCombo.getItems().contains(tipo)) {
                throw new IllegalArgumentException("Seleccione un tipo de vehículo válido.");
            }
            validarTexto(placaField.getText(), "la placa");
            validarTexto(marcaField.getText(), "la marca");
            validarTexto(caracteristicaField.getText(), "la característica");

            String placa = placaField.getText().trim();
            String marca = marcaField.getText().trim();
            double tarifa = convertirDouble(tarifaField.getText(), "la tarifa diaria");
            String caracteristica = caracteristicaField.getText().trim();
            Vehiculo vehiculo;

            if ("Moto".equals(tipo)) {
                vehiculo = new Moto(
                        placa, marca, tarifa,
                        convertirEntero(caracteristica, "la cilindrada")
                );
            } else if ("Bicimoto".equals(tipo)) {
                vehiculo = new Bicimoto(placa, marca, tarifa, caracteristica);
            } else if ("Cuadraciclo".equals(tipo)) {
                vehiculo = new Cuadraciclo(placa, marca, tarifa, caracteristica);
            } else {
                throw new IllegalArgumentException("El tipo de vehículo no es válido.");
            }

            vehiculoService.registrarVehiculo(vehiculo);
            limpiarCamposVehiculo();
            syncManager.notificarCambio("VEHICULO", "CREAR");
            actualizarListas();
            mostrarInformacion("Vehículo registrado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    /**
     * Registra un alquiler para el cliente y el vehículo seleccionados.
     *
     * <p>El servicio valida que el vehículo esté disponible, calcula el costo
     * del alquiler más el depósito y cambia el estado del vehículo a
     * {@code ALQUILADO}. Al finalizar, las listas de la interfaz se
     * sincronizan con el nuevo estado.</p>
     */
    @FXML
    private void registrarAlquiler() {
        try {
            comprobarDatosCargados();
            if (clienteAlquilerCombo.getValue() == null
                    || !clienteAlquilerCombo.getItems().contains(clienteAlquilerCombo.getValue())) {
                throw new IllegalArgumentException("Debe seleccionar un cliente válido.");
            }
            if (vehiculoAlquilerCombo.getValue() == null
                    || !vehiculoAlquilerCombo.getItems().contains(vehiculoAlquilerCombo.getValue())) {
                throw new IllegalArgumentException("Debe seleccionar un vehículo disponible.");
            }
            if (diasSpinner.getValue() == null || diasSpinner.getValue() < 1
                    || diasSpinner.getValue() > 365) {
                throw new IllegalArgumentException("Seleccione una cantidad de días entre 1 y 365.");
            }
            if (fechaInicioPicker.getValue() == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar la fecha de inicio."
                );
            }

            double deposito = convertirDouble(
                    depositoField.getText(), "el depósito de garantía"
            );
            Alquiler alquiler = alquilerService.registrarAlquiler(
                    clienteAlquilerCombo.getValue(),
                    vehiculoAlquilerCombo.getValue(),
                    fechaInicioPicker.getValue(),
                    diasSpinner.getValue(),
                    deposito
            );
            syncManager.notificarCambio("ALQUILER", "CREAR");
            actualizarListas();
            mostrarInformacion(
                    "Alquiler #" + alquiler.getId()
                            + " registrado. Total inicial: "
                            + dinero(alquiler.calcularTotal())
            );
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    /**
     * Calcula una vista previa de la devolución seleccionada.
     *
     * <p>El cálculo se ejecuta automáticamente cuando cambia el alquiler o la
     * fecha. La multa se cobra por separado y se compara con el depósito para
     * indicar claramente si se devuelve dinero o si el cliente debe pagar un
     * saldo adicional.</p>
     */
    @FXML
    private void calcularDevolucion() {
        Alquiler alquiler = devolucionCombo.getValue();
        LocalDate fecha = fechaDevolucionPicker.getValue();

        if (alquiler == null || fecha == null) {
            multaLabel.setText("₡0.00");
            depositoDevolverLabel.setText("₡0.00");
            totalDevolucionLabel.setText("₡0.00");
            return;
        }

        try {
            double multa = alquilerService.calcularMulta(alquiler, fecha);
            double deposito = alquilerService.calcularDepositoADevolver(
                    alquiler, fecha
            );
            multaLabel.setText(dinero(multa));
            depositoDevolverLabel.setText(dinero(deposito));
            double saldo = deposito - multa;
            if (saldo >= 0) {
                totalDevolucionLabel.setText(
                        "Se devuelve: " + dinero(saldo)
                );
            } else {
                totalDevolucionLabel.setText(
                        "Cliente debe pagar: " + dinero(Math.abs(saldo))
                );
            }
        } catch (RuntimeException exception) {
            multaLabel.setText("Fecha inválida");
            depositoDevolverLabel.setText("Fecha inválida");
            totalDevolucionLabel.setText("Fecha inválida");
        }
    }

    /**
     * Finaliza el alquiler y registra la devolución del vehículo.
     *
     * <p>El servicio calcula la multa, marca el alquiler como finalizado y
     * devuelve el vehículo al estado {@code DISPONIBLE}. Luego se actualizan
     * las listas para que el vehículo vuelva a aparecer como disponible.</p>
     */
    @FXML
    private void devolverVehiculo() {
        try {
            comprobarDatosCargados();
            if (devolucionCombo.getValue() == null
                    || !devolucionCombo.getItems().contains(devolucionCombo.getValue())) {
                throw new IllegalArgumentException("Debe seleccionar un alquiler activo válido.");
            }
            if (fechaDevolucionPicker.getValue() == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar la fecha de devolución."
                );
            }

            Alquiler alquiler = devolucionCombo.getValue();
            double multa = alquilerService.devolverVehiculo(
                    alquiler, fechaDevolucionPicker.getValue()
            );
            syncManager.notificarCambio("ALQUILER", "DEVOLVER");
            actualizarListas();
            mostrarInformacion(
                    "Devolución registrada. Multa cobrada: "
                            + dinero(multa)
            );
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    /**
     * Cambia el texto de la característica según el subtipo de vehículo.
     */
    private void actualizarCaracteristica() {
        String tipo = tipoVehiculoCombo.getValue();
        if ("Moto".equals(tipo)) {
            caracteristicaLabel.setText("Cilindrada (cc)");
        } else if ("Bicimoto".equals(tipo)) {
            caracteristicaLabel.setText("Tipo de motor");
        } else if ("Cuadraciclo".equals(tipo)) {
            caracteristicaLabel.setText("Tipo de tracción");
        } else {
            caracteristicaLabel.setText("Característica");
        }
    }

    /**
     * Actualiza el costo base y el total inicial del alquiler.
     *
     * <p>El total inicial incluye la tarifa diaria multiplicada por los días
     * seleccionados y el depósito ingresado. El depósito se valida nuevamente
     * al confirmar el alquiler.</p>
     */
    private void actualizarTotalAlquiler() {
        Vehiculo vehiculo = vehiculoAlquilerCombo.getValue();
        if (vehiculo == null || diasSpinner.getValue() == null) {
            costoAlquilerLabel.setText("₡0.00");
            totalAlquilerLabel.setText("₡0.00");
            return;
        }

        double costo = vehiculo.calcularCostoAlquiler(diasSpinner.getValue());
        costoAlquilerLabel.setText(dinero(costo));
        try {
            double deposito = convertirDouble(depositoField.getText(), "el depósito");
            totalAlquilerLabel.setText(dinero(costo + deposito));
        } catch (IllegalArgumentException e) {
            totalAlquilerLabel.setText("Ingrese un depósito válido");
        }
    }

    /**
     * Recarga las listas y combos usando la información actual de los
     * servicios, incluyendo únicamente vehículos disponibles y alquileres
     * activos en las secciones correspondientes.
     */
    @FXML
    private void actualizarListas() {
        final int solicitud = ++numeroCarga;
        datosCargados = false;
        estadoConexionLabel.setText("Cargando datos de MySQL...");

        // La consulta de MySQL nunca se hace dentro del hilo gráfico de JavaFX.
        Task<DatosSistema> tarea = new Task<>() {
            @Override
            protected DatosSistema call() {
                List<Cliente> clientes = clienteService.listarClientes();
                List<Vehiculo> vehiculos = vehiculoService.listarVehiculos();
                List<Alquiler> alquileres = alquilerService.listarAlquileres();
                return new DatosSistema(clientes, vehiculos, alquileres);
            }
        };

        tarea.setOnSucceeded(event -> {
            if (solicitud != numeroCarga) return; // Descarta resultados anteriores.
            DatosSistema datos = tarea.getValue();
            todosLosClientes = new ArrayList<>(datos.clientes);
            todosLosVehiculos = new ArrayList<>(datos.vehiculos);
            filtrarClientes();
            filtrarVehiculos();
            clienteAlquilerCombo.getItems().setAll(datos.clientes);
            vehiculoAlquilerCombo.getItems().setAll(datos.vehiculos.stream()
                    .filter(v -> v.getEstado() == cr.ac.una.alquilervehiculos.model.EstadoVehiculo.DISPONIBLE)
                    .toList());
            List<Alquiler> activos = datos.alquileres.stream()
                    .filter(a -> !a.isFinalizado()).toList();
            alquileresActivosList.getItems().setAll(activos);
            devolucionCombo.getItems().setAll(activos);
            historialAlquileresList.getItems().setAll(datos.alquileres);
            datosCargados = true;
            estadoConexionLabel.setText("Datos actualizados correctamente.");
            actualizarTotalAlquiler();
            calcularDevolucion();
        });

        tarea.setOnFailed(event -> {
            if (solicitud != numeroCarga) return;
            datosCargados = false;
            todosLosClientes.clear();
            todosLosVehiculos.clear();
            clientesList.getItems().clear();
            vehiculosList.getItems().clear();
            clienteAlquilerCombo.getItems().clear();
            vehiculoAlquilerCombo.getItems().clear();
            alquileresActivosList.getItems().clear();
            devolucionCombo.getItems().clear();
            historialAlquileresList.getItems().clear();
            estadoConexionLabel.setText("Sin datos. Verifique MySQL y pulse Actualizar datos.");
            Throwable error = tarea.getException();
            String mensaje = error == null || error.getMessage() == null
                    ? "No se pudieron consultar los datos en MySQL."
                    : error.getMessage();
            mostrarError(mensaje + "\nVerifique MySQL y pulse Actualizar datos.");
        });

        Thread hilo = new Thread(tarea, "cargar-datos-mysql");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** No permite operar con registros desconocidos o durante una actualización. */
    private void comprobarDatosCargados() {
        if (!datosCargados) {
            throw new IllegalStateException(
                    "Los datos todavía no están disponibles. Pulse Actualizar datos.");
        }
    }



    /** Permite modificar nombre, teléfono y correo sin cambiar la identificación. */
    @FXML
    private void editarCliente() {
        if (!datosCargados) {
            mostrarError("Los datos todavía no están disponibles. Pulse Actualizar datos.");
            return;
        }
        Cliente seleccionado = clientesList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione primero un cliente de la lista.");
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Editar cliente");
        dialogo.setHeaderText("Cliente: " + seleccionado.getIdentificacion()
                + " (la identificación no se puede modificar)");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre = new TextField(seleccionado.getNombre());
        TextField telefono = new TextField(seleccionado.getTelefono() == null ? "" : seleccionado.getTelefono());
        TextField correo = new TextField(seleccionado.getCorreo() == null ? "" : seleccionado.getCorreo());
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.setPadding(new Insets(12));
        formulario.addRow(0, new Label("Nombre:"), nombre);
        formulario.addRow(1, new Label("Teléfono:"), telefono);
        formulario.addRow(2, new Label("Correo:"), correo);
        dialogo.getDialogPane().setContent(formulario);

        Button aceptar = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        aceptar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                boolean actualizado = clienteService.actualizarCliente(
                        seleccionado.getIdentificacion(), nombre.getText(),
                        telefono.getText(), correo.getText());
                if (!actualizado) {
                    mostrarError("El cliente ya no existe en MySQL.");
                    evento.consume();
                }
            } catch (RuntimeException e) {
                mostrarError(e.getMessage());
                evento.consume(); // Conserva el formulario para que se corrijan los datos.
            }
        });
        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                syncManager.notificarCambio("CLIENTE", "EDITAR");
                actualizarListas();
                mostrarInformacion("Cliente actualizado correctamente en MySQL.");
            } catch (RuntimeException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    /** Modifica marca y tarifa diaria sin alterar placa, subtipo o disponibilidad. */
    @FXML
    private void editarVehiculo() {
        if (!datosCargados) {
            mostrarError("Los datos todavía no están disponibles. Pulse Actualizar datos.");
            return;
        }
        Vehiculo seleccionado = vehiculosList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione primero un vehículo de la lista.");
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Editar vehículo");
        dialogo.setHeaderText("Placa: " + seleccionado.getPlaca()
                + " (la placa no se puede modificar)");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField marca = new TextField(seleccionado.getMarca());
        TextField tarifa = new TextField(Double.toString(seleccionado.getTarifaDiaria()));
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.setPadding(new Insets(12));
        formulario.addRow(0, new Label("Marca:"), marca);
        formulario.addRow(1, new Label("Tarifa diaria:"), tarifa);
        dialogo.getDialogPane().setContent(formulario);

        Button aceptar = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        aceptar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                double tarifaNueva = convertirDouble(tarifa.getText(), "la tarifa diaria");
                boolean actualizado = vehiculoService.actualizarVehiculo(
                        seleccionado.getPlaca(), marca.getText(), tarifaNueva);
                if (!actualizado) {
                    mostrarError("El vehículo ya no existe en MySQL.");
                    evento.consume();
                }
            } catch (RuntimeException e) {
                mostrarError(e.getMessage());
                evento.consume(); // Permite corregir la tarifa o la marca.
            }
        });
        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                syncManager.notificarCambio("VEHICULO", "EDITAR");
                actualizarListas();
                mostrarInformacion("Vehículo actualizado correctamente en MySQL.");
            } catch (RuntimeException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    /** Elimina de MySQL el cliente seleccionado, previa confirmación. */
    @FXML
    private void eliminarCliente() {
        if (!datosCargados) {
            mostrarError("Los datos todavía no están disponibles. Pulse Actualizar datos.");
            return;
        }
        Cliente seleccionado = clientesList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione primero un cliente de la lista.");
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("¿Eliminar al cliente " + seleccionado.getNombre() + "?");
        confirmar.setContentText("Esta operación es permanente. No se permite eliminar clientes con historial de alquileres.");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            if (!clienteEliminacionService.eliminarCliente(seleccionado.getIdentificacion())) {
                mostrarError("El cliente ya no existe en MySQL.");
                return;
            }
            syncManager.notificarCambio("CLIENTE", "ELIMINAR");
            actualizarListas();
            mostrarInformacion("Cliente eliminado correctamente.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    /** Elimina un vehículo disponible sin historial, previa confirmación. */
    @FXML
    private void eliminarVehiculo() {
        if (!datosCargados) {
            mostrarError("Los datos todavía no están disponibles. Pulse Actualizar datos.");
            return;
        }
        Vehiculo seleccionado = vehiculosList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione primero un vehículo de la lista.");
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("¿Eliminar el vehículo " + seleccionado.getPlaca() + "?");
        confirmar.setContentText("Esta operación es permanente. No se permite eliminar vehículos alquilados ni con historial de alquileres.");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            if (!vehiculoService.eliminarVehiculo(seleccionado.getPlaca())) {
                mostrarError("El vehículo ya no existe en MySQL.");
                return;
            }
            syncManager.notificarCambio("VEHICULO", "ELIMINAR");
            actualizarListas();
            mostrarInformacion("Vehículo eliminado correctamente.");
        } catch (RuntimeException e) {
            mostrarError("No se pudo eliminar el vehículo. Si tiene alquileres registrados, debe conservarse para mantener el historial. Detalle: " + e.getMessage());
        }
    }

    /** Filtra clientes sin modificar los registros guardados en MySQL. */
    private void filtrarClientes() {
        String texto = buscarClienteField.getText() == null ? "" :
                buscarClienteField.getText().trim().toLowerCase(Locale.ROOT);
        clientesList.getItems().setAll(todosLosClientes.stream()
                .filter(c -> texto.isEmpty()
                        || String.valueOf(c).toLowerCase(Locale.ROOT).contains(texto))
                .toList());
    }

    /** Filtra vehículos por el texto visible de cada registro. */
    private void filtrarVehiculos() {
        String texto = buscarVehiculoField.getText() == null ? "" :
                buscarVehiculoField.getText().trim().toLowerCase(Locale.ROOT);
        vehiculosList.getItems().setAll(todosLosVehiculos.stream()
                .filter(v -> texto.isEmpty()
                        || String.valueOf(v).toLowerCase(Locale.ROOT).contains(texto))
                .toList());
    }

    /**
     * Limpia los campos del formulario de clientes después de un registro
     * exitoso.
     */
    private void limpiarCamposCliente() {
        identificacionField.clear();
        nombreField.clear();
        telefonoField.clear();
        correoField.clear();
    }

    /**
     * Limpia los campos del formulario de inventario después de registrar un
     * vehículo correctamente.
     */
    private void limpiarCamposVehiculo() {
        placaField.clear();
        marcaField.clear();
        tarifaField.clear();
        caracteristicaField.clear();
    }

    /**
     * Verifica que un campo de texto contenga información útil.
     *
     * @param valor contenido ingresado en el campo.
     * @param campo nombre del dato que se mostrará en el mensaje de error.
     * @throws IllegalArgumentException si el valor es nulo o está vacío.
     */
    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar " + campo + "."
            );
        }
    }

    /**
     * Convierte un texto a un número entero positivo.
     *
     * @param valor texto que contiene el número.
     * @param campo nombre del campo que se está convirtiendo.
     * @return número entero ingresado.
     * @throws IllegalArgumentException si el texto no es numérico o no es
     *                                  mayor que cero.
     */
    private int convertirEntero(String valor, String campo) {
        try {
            int resultado = Integer.parseInt(valor.trim());
            if (resultado <= 0) {
                throw new IllegalArgumentException(
                        campo + " debe ser mayor que cero."
                );
            }
            return resultado;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Ingrese un valor numérico válido para " + campo + "."
            );
        }
    }

    /**
     * Convierte un texto a un número decimal positivo y finito.
     *
     * @param valor texto que contiene el número.
     * @param campo nombre del campo que se está convirtiendo.
     * @return número decimal ingresado.
     * @throws IllegalArgumentException si el texto no es numérico, no es
     *                                  finito o no es mayor que cero.
     */
    private double convertirDouble(String valor, String campo) {
        return Validaciones.convertirMonto(valor, campo);
    }

    /**
     * Formatea un monto para mostrarlo como dinero en la interfaz.
     *
     * @param valor monto que se desea formatear.
     * @return monto con símbolo de moneda y dos decimales.
     */
    private String dinero(double valor) {
        return String.format("₡%,.2f", valor);
    }

    /**
     * Muestra un mensaje informativo después de completar una operación.
     *
     * @param mensaje texto que se presentará al usuario.
     */
    private void mostrarInformacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alquiler de vehículos");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error producido por una validación o regla del
     * negocio.
     *
     * @param mensaje descripción del problema encontrado.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje == null ? "Ocurrió un error." : mensaje);
        alert.showAndWait();
    }
}
