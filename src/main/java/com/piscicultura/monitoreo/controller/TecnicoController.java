package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class TecnicoController {

    @FXML private Label lblBienvenida;
    @FXML private Button btnVerEstanques;
    @FXML private Button btnGenerarAlarma;
    @FXML private Button btnAccionCorrectiva;
    @FXML private Button btnCerrarSesion;

    private Usuario tecnico;     // Usuario autenticado
    private Stage stage;         // Ventana principal

    // Inicializa el controlador desde el Login
    public void init(Usuario tecnico, Stage stage) {
        this.tecnico = tecnico;
        this.stage = stage;
    }
    
    @FXML
    private void onAccionCorrectiva() {
        AccionCorrectiva accion = new AccionCorrectiva(
                101,
                "Verificar bomba de oxigenación",
                tecnico,
                "1) Revisar encendido\n2) Limpiar válvulas\n3) Registrar presión",
                0.0f
        );
        accion.ejecutar();
        mostrarAlerta("Acción Correctiva", "Acción ejecutada: " + accion.getNombre(), Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void onCerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piscicultura/monitoreo/view/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cerrar sesión: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
