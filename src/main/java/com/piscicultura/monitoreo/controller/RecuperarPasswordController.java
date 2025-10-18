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
import com.piscicultura.monitoreo.util.ConexionDB;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RecuperarPasswordController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtToken;
    @FXML private PasswordField txtNuevaPassword;
    @FXML private Label lblMensaje;
    
    private UsuarioDAO usuarioDAO;

    public RecuperarPasswordController() {
        try {
            this.usuarioDAO = new UsuarioDAO(ConexionDB.getConnection());
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        // Inicialización si es necesaria
    }

    @FXML
    private void handleGenerarToken() {
        String email = txtEmail.getText().trim();
        
        if (email.isEmpty()) {
            mostrarError("Por favor, ingrese su email");
            return;
        }
        
        if (usuarioDAO == null) {
            mostrarError("Error de conexión a la base de datos");
            return;
        }
        
        try {
            if (!usuarioDAO.existeEmail(email)) {
                mostrarError("El email no está registrado en el sistema o la cuenta está inactiva");
                return;
            }
            
            // Mostrar mensaje de carga
            lblMensaje.setStyle("-fx-text-fill: #f39c12;");
            lblMensaje.setText("⏳ Generando y enviando código...");
            
            // Ejecutar en hilo separado para no bloquear la UI
            new Thread(() -> {
                try {
                    String resultado = usuarioDAO.generarTokenRecuperacion(email);
                    
                    javafx.application.Platform.runLater(() -> {
                        if ("EMAIL_ENVIADO".equals(resultado)) {
                            mostrarExito("✅ Código enviado exitosamente\n📧 Revisa tu correo electrónico: " + email + 
                                       "\n🔐 Ingresa el código de 6 dígitos que recibiste");
                        } else {
                            mostrarError("❌ Error al enviar el código. Verifica la configuración del email.");
                        }
                    });
                    
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        mostrarError("❌ Error al procesar la solicitud");
                        e.printStackTrace();
                    });
                }
            }).start();
            
        } catch (Exception e) {
            mostrarError("❌ Error al procesar la solicitud");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCambiarPassword() {
        String token = txtToken.getText().trim();
        String nuevaPassword = txtNuevaPassword.getText().trim();
        
        if (token.isEmpty() || nuevaPassword.isEmpty()) {
            mostrarError("Por favor, complete todos los campos");
            return;
        }
        
        if (nuevaPassword.length() < 4) {
            mostrarError("La contraseña debe tener al menos 4 caracteres");
            return;
        }
        
        if (usuarioDAO == null) {
            mostrarError("Error de conexión a la base de datos");
            return;
        }
        
        try {
            if (usuarioDAO.cambiarPasswordConToken(token, nuevaPassword)) {
                mostrarExito("✅ Contraseña cambiada exitosamente\nPuede cerrar esta ventana y volver al login");
                
                // Limpiar campos
                txtToken.clear();
                txtNuevaPassword.clear();
                
            } else {
                mostrarError("❌ Token inválido, expirado o la cuenta está inactiva");
            }
        } catch (Exception e) {
            mostrarError("❌ Error al cambiar la contraseña");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        lblMensaje.setText(mensaje);
    }
    
    private void mostrarExito(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        lblMensaje.setText(mensaje);
    }
}