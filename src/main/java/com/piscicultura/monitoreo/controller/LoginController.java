/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.controller;

/**
 *
 * @author Cristian
 */

import com.piscicultura.monitoreo.dao.UsuarioDAO;
import com.piscicultura.monitoreo.model.Usuario;
import com.piscicultura.monitoreo.util.ConexionDB;
import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML
    private TextField txtNombreUsuario; 

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;
    
    private Connection conexion;
    private UsuarioDAO usuarioDAO;

    public LoginController() {
        try {
            this.conexion = ConexionDB.getConnection();
            this.usuarioDAO = new UsuarioDAO(conexion);
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String nombreUsuario = txtNombreUsuario.getText();
        String password = txtPassword.getText();

        if (nombreUsuario.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Por favor ingrese su nombre de usuario y contraseña.");
            return;
        }

        if (usuarioDAO == null) {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("❌ Error de conexión a la base de datos.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.validarCredenciales(nombreUsuario, password);

            if (usuario != null) {
                lblMensaje.setStyle("-fx-text-fill: green;");
                lblMensaje.setText("✅ Bienvenido, " + usuario.getNombre() + " (" + usuario.getRol() + ")");
                     // 🔹 NUEVO CÓDIGO: abrir el dashboard del admin
                try {
                    javafx.stage.Stage stage = (javafx.stage.Stage) txtNombreUsuario.getScene().getWindow();

                    switch (usuario.getRol().name().toUpperCase()) {
                        case "ADMIN":
                            // 🔹 Cargar vista del administrador
                            javafx.fxml.FXMLLoader adminLoader = new javafx.fxml.FXMLLoader(
                                    getClass().getResource("/com/piscicultura/monitoreo/view/admin.fxml")
                            );
                            javafx.scene.Parent adminRoot = adminLoader.load();

                            com.piscicultura.monitoreo.controller.AdminController adminController = adminLoader.getController();
                            adminController.init(usuario, stage);

                            javafx.scene.Scene adminScene = new javafx.scene.Scene(adminRoot);
                            stage.setScene(adminScene);
                            stage.centerOnScreen();
                            break;

                        case "TECNICO":
                            // 🔹 Más adelante cargarás la vista del técnico
                            System.out.println("🔧 Vista del técnico (pendiente)");
                            lblMensaje.setText("🔧 Módulo de técnico en desarrollo.");
                            break;

                        case "INVESTIGADOR":
                            // 🔹 Más adelante cargarás la vista del investigador
                            System.out.println("🔬 Vista del investigador (pendiente)");
                            lblMensaje.setText("🔬 Módulo de investigador en desarrollo.");
                            break;

                        default:
                            lblMensaje.setText("⚠️ Rol no reconocido: " + usuario.getRol());
                            break;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblMensaje.setStyle("-fx-text-fill: red;");
                    lblMensaje.setText("❌ Error al abrir el panel correspondiente.");
                }

            } else {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("❌ Credenciales incorrectas.");
            }
        } catch (Exception e) {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("❌ Error al validar credenciales.");
            e.printStackTrace();
        }
    }
}