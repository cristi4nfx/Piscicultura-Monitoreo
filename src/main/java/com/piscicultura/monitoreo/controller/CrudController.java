/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.dao.UsuarioDAO;
import com.piscicultura.monitoreo.model.Usuario;
import com.piscicultura.monitoreo.util.ConexionDB;
import java.sql.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class CrudController {

    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmar;
    @FXML private TextField txtTelefono;
    @FXML private ChoiceBox<String> cbRol;
    @FXML private CheckBox chkActivo;

    private Stage stage;
    private UsuarioFormData result; // null si se canceló

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Cargamos los roles por código (evita problemas de FXML con fx:String)
        cbRol.getItems().setAll("ADMIN", "TECNICO", "INVESTIGADOR");
        cbRol.setValue("TECNICO");
        chkActivo.setSelected(true);
    }

    @FXML
    private void onCrearUsuario() {
        // Validaciones básicas
        if (txtNombreUsuario.getText().isBlank()) {
            alert(Alert.AlertType.WARNING, "Valida", "El nombre de usuario es obligatorio");
            return;
        }
        if (txtContrasena.getText().isBlank()) {
            alert(Alert.AlertType.WARNING, "Valida", "La contraseña es obligatoria");
            return;
        }
        if (!txtContrasena.getText().equals(txtConfirmar.getText())) {
            alert(Alert.AlertType.WARNING, "Valida", "Las contraseñas no coinciden");
            return;
        }
        if (cbRol.getValue() == null || cbRol.getValue().isBlank()) {
            alert(Alert.AlertType.WARNING, "Valida", "Selecciona un rol.");
            return;
        }

        final int telefono;
        try {
            String t = txtTelefono.getText().trim();
            telefono = t.isEmpty() ? 0 : Integer.parseInt(t);
        } catch (NumberFormatException nfe) {
            alert(Alert.AlertType.WARNING, "Valida", "El teléfono debe ser numérico.");
            return;
        }

        try (Connection conexion = ConexionDB.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conexion);

            boolean ok = usuarioDAO.registrarUsuario(
                    txtNombreUsuario.getText().trim(),
                    txtEmail.getText().trim(),
                    txtContrasena.getText(),    
                    telefono,
                    cbRol.getValue().toUpperCase().trim(),
                    chkActivo.isSelected()
            );

            if (ok) {
                alert(Alert.AlertType.INFORMATION, "Éxito", "Usuario creado correctamente.");
                close(); // solo cerrar si todo salió bien
            } else {
                alert(Alert.AlertType.ERROR, "Error", "No se pudo crear el usuario (ver logs).");
            }

        } catch (java.sql.SQLException sqle) {
            if ("23505".equals(sqle.getSQLState())) {
                alert(Alert.AlertType.ERROR, "Duplicado", "El nombre de usuario o el email ya existen.");
            } else {
                alert(Alert.AlertType.ERROR, "SQL Error", "Error al guardar: " + sqle.getMessage());
            }
            sqle.printStackTrace();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Error", "Ocurrió un error: " + e.getMessage());
            e.printStackTrace();
        }
    }


        @FXML
        private void onCancelar() {
            result = null; // explícito
            close();
        }

        public Optional<UsuarioFormData> getResult() {
            return Optional.ofNullable(result);
        }

        private void close() {
            Stage s = (stage != null) ? stage : (Stage) txtNombreUsuario.getScene().getWindow();
            s.close();
        }

        private void alert(Alert.AlertType type, String header, String content) {
            Alert a = new Alert(type);
            a.setHeaderText(header);
            a.setContentText(content);
            a.showAndWait();
        }

    // DTO simple para devolver los datos del formulario
    public static class UsuarioFormData {
        public final String nombreUsuario;
        public final String email;
        public final String contrasena;  // en texto por ahora
        public final String telefono;
        public final String rol;         // "ADMIN" | "TECNICO" | "INVESTIGADOR"
        public final boolean activo;

        public UsuarioFormData(String nombreUsuario, String email, String contrasena,
                               String telefono, String rol, boolean activo) {
            this.nombreUsuario = nombreUsuario;
            this.email = email;
            this.contrasena = contrasena;
            this.telefono = telefono;
            this.rol = rol;
            this.activo = activo;
        }
    }
}


