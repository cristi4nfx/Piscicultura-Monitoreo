package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.dao.UsuarioDAO;
import com.piscicultura.monitoreo.model.Administrador;
import com.piscicultura.monitoreo.model.Investigador;
import com.piscicultura.monitoreo.model.Rol;
import com.piscicultura.monitoreo.model.Tecnico;
import com.piscicultura.monitoreo.model.Usuario;
import com.piscicultura.monitoreo.util.ConexionDB;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

import java.sql.Connection;
import java.util.List;
import javafx.scene.layout.VBox;

public class CrudController {

    // ---- Campos compartidos por crear/editar ----
    @FXML private TextField txtId;              // usado en editar (si tu FXML lo incluye)
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmar;
    @FXML private TextField txtTelefono;
    @FXML private ChoiceBox<String> cbRol;
    @FXML private CheckBox chkActivo;

    private Stage stage;

    // Estado de lista/edición
    private TableView<Usuario> tablaUsuarios;   // para leer selección y atajos
    private Usuario usuarioEditando;            // usuario actualmente en edición

    public void setStage(Stage stage) { this.stage = stage; }

    @FXML
    private void initialize() {
        // Poblamos roles por código (evita problemas con <FXCollections> en FXML)
        if (cbRol != null && cbRol.getItems().isEmpty()) {
            cbRol.getItems().setAll("ADMIN", "TECNICO", "INVESTIGADOR");
            cbRol.setValue("TECNICO");
        }
        if (chkActivo != null) chkActivo.setSelected(true);
    }

    // ===================== CREAR =====================
    @FXML
    private void onCrearUsuario() {
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

        try (Connection cn = ConexionDB.getConnection()) {
            UsuarioDAO dao = new UsuarioDAO(cn);
            boolean ok = dao.registrarUsuario(
                    txtNombreUsuario.getText().trim(),
                    txtEmail.getText().trim(),
                    txtContrasena.getText(),
                    telefono,
                    cbRol.getValue().toUpperCase().trim(),
                    chkActivo.isSelected()
            );
            if (ok) {
                alert(Alert.AlertType.INFORMATION, "Éxito", "Usuario creado correctamente.");
                close();
            } else {
                alert(Alert.AlertType.ERROR, "Error", "No se pudo crear el usuario (ver logs).");
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Error", "Ocurrió un error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===================== LISTAR =====================
    @FXML
    void onListarUsuarios() {
        TableView<Usuario> table = new TableView<>();
        this.tablaUsuarios = table;
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Columnas
        TableColumn<Usuario, Integer> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Usuario, String> cUser = new TableColumn<>("Usuario");
        cUser.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));

        TableColumn<Usuario, String> cEmail = new TableColumn<>("Email");
        cEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Usuario, String> cTel = new TableColumn<>("Teléfono");
        cTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<Usuario, String> cRol = new TableColumn<>("Rol");
        cRol.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getRol() == null ? "" : cd.getValue().getRol().name()
        ));

        TableColumn<Usuario, Boolean> cActivo = new TableColumn<>("Activo");
        cActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        cActivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Sí" : "No"));
            }
        });

        table.getColumns().setAll(cId, cUser, cEmail, cTel, cRol, cActivo);

        // Cargar datos via DAO
        ObservableList<Usuario> items = FXCollections.observableArrayList();
        try (Connection cn = ConexionDB.getConnection()) {
            UsuarioDAO dao = new UsuarioDAO(cn);
            List<Usuario> lista = dao.listarTodos();
            items.setAll(lista);
            table.setItems(items);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Error", "No se pudo cargar usuarios:\n" + e.getMessage());
            e.printStackTrace();
        }

        // Menú contextual: Editar / Eliminar
        table.setRowFactory(tv -> {
            TableRow<Usuario> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem miEditar = new MenuItem("Editar");
            miEditar.setOnAction(a -> {
                tablaUsuarios.getSelectionModel().select(row.getIndex());
                onEditarUsuario();
            });

            MenuItem miEliminar = new MenuItem("Eliminar");
            miEliminar.setOnAction(a -> {
                Usuario u = row.getItem();
                if (u != null) eliminarUsuario(u, items, table);
            });

            menu.getItems().addAll(miEditar, miEliminar);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            // Doble clic = Editar
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    tablaUsuarios.getSelectionModel().select(row.getIndex());
                    onEditarUsuario();
                }
            });

            return row;
        });

        // Supr/Del = Eliminar seleccionado
        table.setOnKeyPressed(ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.DELETE) {
                Usuario u = table.getSelectionModel().getSelectedItem();
                if (u != null) eliminarUsuario(u, items, table);
            }
        });

        showInCenter(table);
    }

    // ===================== EDITAR (misma clase, view aparte) =====================
    /** Abre la vista editar_user.fxml pero manejada por este mismo CrudController. */
    @FXML
    private void onEditarUsuario() {
        if (tablaUsuarios == null) {
            alert(Alert.AlertType.INFORMATION, "Info", "Primero abre la lista de usuarios.");
            return;
        }
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert(Alert.AlertType.WARNING, "Selecciona", "Selecciona un usuario de la tabla.");
            return;
        }
        this.usuarioEditando = sel;

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/piscicultura/monitoreo/view/editar_user.fxml")
            );
            // Usamos ESTE MISMO controller (porque ya tiene campos y lógica)
            loader.setController(this);
            Node root = loader.load();

            fillFormForEdit(sel); // precargar datos
            showInCenter(root);   // mostrar en el panel central
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Error", "No se pudo abrir el editor:\n" + e.getMessage());
            e.printStackTrace();
        }
    }


    /** Copia datos del usuario al formulario de edición. */
    private void fillFormForEdit(Usuario u) {
        if (u == null) return;
        if (cbRol.getItems().isEmpty()) {
            cbRol.getItems().setAll("ADMIN", "TECNICO", "INVESTIGADOR");
        }
        if (txtId != null)             txtId.setText(String.valueOf(u.getId()));
        if (txtNombreUsuario != null)  txtNombreUsuario.setText(u.getNombre());
        if (txtEmail != null)          txtEmail.setText(u.getEmail());
        if (txtTelefono != null)       txtTelefono.setText(String.valueOf(u.getTelefono()));
        if (cbRol != null)             cbRol.setValue(u.getRol() == null ? null : u.getRol().name());
        if (chkActivo != null)         chkActivo.setSelected(u.isActivo());
        if (txtContrasena != null)     txtContrasena.clear();
        if (txtConfirmar != null)      txtConfirmar.clear();
    }

    /** Guarda cambios del editor (UPDATE vía DAO). */
    @FXML
    private void onGuardarCambiosEdicion() {
        if (usuarioEditando == null) {
            alert(Alert.AlertType.WARNING, "Sin selección", "No hay usuario cargado para editar.");
            return;
        }
        if (txtNombreUsuario.getText().isBlank()) {
            alert(Alert.AlertType.WARNING, "Valida", "El nombre de usuario es obligatorio");
            return;
        }
        if (cbRol.getValue() == null || cbRol.getValue().isBlank()) {
            alert(Alert.AlertType.WARNING, "Valida", "Selecciona un rol.");
            return;
        }

        int telefono;
        try {
            String t = txtTelefono.getText().trim();
            telefono = t.isEmpty() ? 0 : Integer.parseInt(t);
        } catch (NumberFormatException nfe) {
            alert(Alert.AlertType.WARNING, "Valida", "El teléfono debe ser numérico.");
            return;
        }

        final String nuevaPass = (txtContrasena != null && !txtContrasena.getText().isBlank())
                ? txtContrasena.getText()
                : null;
        if (nuevaPass != null && !nuevaPass.equals(txtConfirmar.getText())) {
            alert(Alert.AlertType.WARNING, "Valida", "Las contraseñas no coinciden");
            return;
        }

        try (Connection cn = ConexionDB.getConnection()) {
            UsuarioDAO dao = new UsuarioDAO(cn);
            boolean ok = dao.actualizarUsuario(
                    usuarioEditando.getId(),
                    txtNombreUsuario.getText().trim(),
                    txtEmail.getText().trim(),
                    telefono,
                    cbRol.getValue(),
                    chkActivo.isSelected(),
                    nuevaPass // null = no cambia la contraseña
            );
            if (ok) {
                alert(Alert.AlertType.INFORMATION, "Éxito", "Usuario actualizado.");
                usuarioEditando = null;
                onListarUsuarios(); // refresca la lista
            } else {
                alert(Alert.AlertType.WARNING, "Sin cambios", "No se actualizó ningún registro.");
            }
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Error", "Ocurrió un error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===================== ELIMINAR =====================
    private void eliminarUsuario(Usuario u, ObservableList<Usuario> items, TableView<Usuario> table) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al usuario \"" + u.getNombre() + "\" (ID " + u.getId() + ")?",
                ButtonType.YES, ButtonType.NO);
        conf.setHeaderText("Confirmar eliminación");
        conf.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.YES) return;

            try (Connection cn = ConexionDB.getConnection()) {
                UsuarioDAO dao = new UsuarioDAO(cn);
                boolean ok = dao.eliminarPorId(u.getId());
                if (ok) {
                    items.remove(u);
                    if (!items.isEmpty()) {
                        table.getSelectionModel().select(
                                Math.min(table.getSelectionModel().getSelectedIndex(), items.size() - 1)
                        );
                    }
                    alert(Alert.AlertType.INFORMATION, "Eliminado", "Usuario eliminado correctamente.");
                } else {
                    alert(Alert.AlertType.WARNING, "Sin cambios", "No se encontró el registro para eliminar.");
                }
            } catch (Exception e) {
                alert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar:\n" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ===================== UTILIDADES UI =====================
    @FXML
    private void onCancelar() {
        close();
    }


    private void showInCenter(Node node) {
        StackPane sp = findContentPane();
        if (sp != null) {
            sp.getChildren().setAll(node);
        } else {
            // fallback: avisa en consola para depurar si no lo encuentra
            System.err.println("No se encontró #contentPane en la escena actual.");
        }
    }


    private StackPane findContentPane() {
        if (tablaUsuarios != null && tablaUsuarios.getScene() != null) {
            Node n = tablaUsuarios.getScene().lookup("#contentPane");
            if (n instanceof StackPane) return (StackPane) n;
        }
        // 2) Si hay campos de formulario ya montados
        if (txtNombreUsuario != null && txtNombreUsuario.getScene() != null) {
            Node n = txtNombreUsuario.getScene().lookup("#contentPane");
            if (n instanceof StackPane) return (StackPane) n;
        }
        // 3) Como último recurso, usa el stage 
        if (stage != null && stage.getScene() != null) {
            Node n = stage.getScene().lookup("#contentPane");
            if (n instanceof StackPane) return (StackPane) n;
        }
        return null;
    }
    

    private void close() {
        if (txtNombreUsuario != null && txtNombreUsuario.getScene() != null) {
            Node content = txtNombreUsuario.getScene().lookup("#contentPane");
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


    private void alert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
