module cr.ac.una.alquilervehiculos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens cr.ac.una.alquilervehiculos to javafx.fxml;
    exports cr.ac.una.alquilervehiculos;
}
