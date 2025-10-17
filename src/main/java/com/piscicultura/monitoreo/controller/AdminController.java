package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.model.Usuario;
import com.piscicultura.monitoreo.controller.CrudController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import javafx.scene.layout.VBox;

public class AdminController {

    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private Label lblEstado;
    @FXML private Label lblBarraEstado;

    // NUEVO: contenedor central donde incrustaremos vistas
    @FXML private StackPane contentPane;

    private Usuario usuario;        // usuario logueado
    private Stage stage;            // ventana actual (para cerrar sesión, etc.)

    /** Lo llamaremos desde el Login para pasar el usuario y el stage. */
    public void init(Usuario usuario, Stage stage) {
        this.usuario = usuario;
        this.stage = stage;

        lblNombre.setText(usuario.getNombre());
        lblRol.setText(usuario.getRol().name());
        setEstado("Sesión iniciada como " + usuario.getRol());
        // Asegura que, si hay placeholder en el centro, se muestre
        showPlaceholder(lblEstado.getText());
    }

    private void setEstado(String msg) {
        if (lblEstado != null) lblEstado.setText(msg);
        if (lblBarraEstado != null) lblBarraEstado.setText(msg);
    }

    // ===== utilidades internas para el área central =====
    /** Reemplaza el contenido del centro por el Node dado. */
    private void showInCenter(Node node) {
        if (contentPane != null) {
            contentPane.getChildren().setAll(node);
        }
    }

    /** Muestra el label de estado como placeholder en el centro. */
    private void showPlaceholder(String msg) {
        if (lblEstado != null) lblEstado.setText(msg);
        if (contentPane != null && lblEstado != null) {
            contentPane.getChildren().setAll(lblEstado);
        }
    }

    // ===== Acciones de menú/botones =====
    @FXML
    private void onCrearUsuario() {
        try {
            // Cargar el formulario y mostrarlo embebido (sin Stage modal)
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/piscicultura/monitoreo/view/crear_user.fxml")
            );
            Node root = loader.load();
            showInCenter(root);
            setEstado("Formulario de creación de usuario abierto.");
        } catch (IOException e) {
            error("Error", "No se pudo abrir el formulario:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onListarUsuarios() {
        try {
            // Reutilizamos el mismo FXML controlado por CrudController
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/piscicultura/monitoreo/view/crear_user.fxml")
            );
            Node root = loader.load();
            CrudController crud = loader.getController();

            // 1) Insertar algo controlado por CrudController en el centro
            showInCenter(root);
            setEstado("Cargando listado de usuarios...");

            // 2) Pedirle al CrudController que reemplace el centro por la tabla
            //    (tu onListarUsuarios() ya hace sp.getChildren().setAll(wrapper); sobre #contentPane)
            crud.onListarUsuarios();

            setEstado("Listado de usuarios cargado.");
        } catch (IOException e) {
            error("Error", "No se pudo cargar la vista de usuarios:\n" + e.getMessage());
            e.printStackTrace();
            showPlaceholder("Error al cargar usuarios.");
        }
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
    private void onInicio() {
        if (lblNombre!= null && lblNombre.getScene() != null) {
            Node content = lblNombre.getScene().lookup("#contentPane");
            if (content instanceof StackPane sp) {
                // Panel de inicio (más elegante y útil)
                Label title = new Label("Panel de Administración");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                Label subtitle = new Label("Aquí podrás ver las gráficas y el estado general de los estanques.");
                subtitle.setStyle("-fx-text-fill:#555; -fx-font-size:14px;");

                VBox home = new VBox(8, title, subtitle);
                home.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                home.setStyle("-fx-padding: 20;");

                sp.getChildren().setAll(home);
            }
        }
    }

    private void volverAlLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/piscicultura/monitoreo/view/Login.fxml")
            );
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
