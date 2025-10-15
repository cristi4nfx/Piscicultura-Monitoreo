package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

public class AdminController {

    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private Label lblEstado;
    @FXML private Label lblBarraEstado;

    private Usuario usuario;        // usuario logueado
    private Stage stage;            // ventana actual (para cerrar sesión, etc.)

    /** Lo llamaremos desde el Login para pasar el usuario y el stage. */
    public void init(Usuario usuario, Stage stage) {
        this.usuario = usuario;
        this.stage = stage;

        lblNombre.setText(usuario.getNombre());
        lblRol.setText(usuario.getRol().name());
        setEstado("Sesión iniciada como " + usuario.getRol());
    }

    private void setEstado(String msg) {
        if (lblEstado != null) lblEstado.setText(msg);
        if (lblBarraEstado != null) lblBarraEstado.setText(msg);
    }

    // ===== Acciones de menú/botones =====
    @FXML
    private void onCrearUsuario() {
        setEstado("Abriendo formulario de creación de usuario...");
        // Aquí luego cargas otro FXML en el centro o abres un dialog
        info("Pendiente", "Aquí irá el formulario de creación de usuario.");
    }

    @FXML
    private void onListarUsuarios() {
        setEstado("Cargando listado de usuarios...");
        info("Pendiente", "Aquí irá la tabla de usuarios.");
    }

    @FXML
    private void onCerrarSesion() {
        var conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cerrar sesión y volver al login?", ButtonType.YES, ButtonType.NO);
        conf.setHeaderText("Cerrar sesión");
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                volverAlLogin();
            }
        });
    }
    
        @FXML
    private void onCerrarSesion() {
        var conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cerrar sesión y volver al login?", ButtonType.YES, ButtonType.NO);
        conf.setHeaderText("Cerrar sesión");
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                volverAlLogin();
            }
        });
    }

    private void volverAlLogin() {
        try {
            // Cargar el FXML del login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piscicultura/monitoreo/view/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.centerOnScreen();
            setEstado("Sesión cerrada.");
        } catch (IOException e) {
            error("Error", "No se pudo volver al login:\n" + e.getMessage());
        }
    }

    // Helpers de diálogo
    private void info(String title, String content) {
        var a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void error(String title, String content) {
        var a = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
