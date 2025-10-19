package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.util.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

// UI controls
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

// JavaFX layout & geometry
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.net.URL;
import java.sql.*;
import java.util.*;

public class CrudFincaController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtLongitud;
    @FXML private TextField txtAreaTotal;
    @FXML private ComboBox<String> cboDepartamento;
    @FXML private ComboBox<String> cboMunicipio;
    @FXML private Label lblEstado;

    private Connection conn;
    private final ObservableList<String> departamentosItems = FXCollections.observableArrayList();
    private final ObservableList<String> municipiosItems = FXCollections.observableArrayList();

    // Mapas nombre -> id
    private final Map<String, Integer> deptIdByName = new HashMap<>();
    private final Map<String, Long> muniIdByName = new HashMap<>();

    // === NUEVO: contenedores creados por código (no tocas tu FXML) ===
    @FXML private AnchorPane desktopPane; // debe existir en tu FXML
    private ScrollPane spListado;         // se crea en onVerFincas()
    private VBox boxListado;              // se crea en onVerFincas()

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            conn = ConexionDB.getConnection();

            // Solo configurar combos si EXISTEN en este FXML
            if (cboDepartamento != null && cboMunicipio != null) {
                cargarDepartamentos();

                cboDepartamento.setItems(departamentosItems);
                cboMunicipio.setItems(municipiosItems);
                cboMunicipio.setDisable(true);

                cboDepartamento.valueProperty().addListener((obs, oldVal, newVal) -> {
                    municipiosItems.clear();
                    muniIdByName.clear();
                    if (newVal == null) {
                        cboMunicipio.setDisable(true);
                        cboMunicipio.getSelectionModel().clearSelection();
                        return;
                    }
                    Integer depId = deptIdByName.get(newVal);
                    if (depId != null) {
                        cargarMunicipiosPorDepartamento(depId);
                        cboMunicipio.setDisable(false);
                    } else {
                        setEstado("No se encontró id para el departamento seleccionado.", true);
                        cboMunicipio.setDisable(true);
                    }
                });
            }

        } catch (SQLException e) {
            setEstado("Error de conexión: " + e.getMessage(), true);
        } catch (Exception ex) {
            System.getLogger(CrudFincaController.class.getName())
                  .log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }


    private void cargarDepartamentos() throws SQLException {
        departamentosItems.clear();
        deptIdByName.clear();

        String sql = "SELECT id, nombre FROM departamentos ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                departamentosItems.add(nombre);
                deptIdByName.put(nombre, id);
            }
        }
    }

    private void cargarMunicipiosPorDepartamento(int departamentoId) {
        municipiosItems.clear();
        muniIdByName.clear();

        String sql = "SELECT id, nombre FROM municipios WHERE departamento_id = ? ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departamentoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String nombre = rs.getString("nombre");
                    municipiosItems.add(nombre);
                    muniIdByName.put(nombre, id);
                }
            }
        } catch (SQLException e) {
            setEstado("Error cargando municipios: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onGuardarFinca() {
        String nombreFinca = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String nombreDepSel = cboDepartamento.getValue();
        String nombreMunSel = cboMunicipio.getValue();

        // Validaciones de UI
        if (nombreFinca.isEmpty()) {
            setEstado("Ingresa el nombre de la finca.", true);
            txtNombre.requestFocus();
            return;
        }
        if (nombreDepSel == null) {
            setEstado("Selecciona un departamento.", true);
            cboDepartamento.requestFocus();
            return;
        }
        if (nombreMunSel == null) {
            setEstado("Selecciona un municipio.", true);
            cboMunicipio.requestFocus();
            return;
        }

        // (Opcional) Validar que existan en los mapas nombre->id cargados
        Integer departamentoId = deptIdByName.get(nombreDepSel);
        Long municipioId = muniIdByName.get(nombreMunSel);
        if (departamentoId == null) {
            setEstado("Departamento no reconocido en el mapa interno.", true);
            return;
        }
        if (municipioId == null) {
            setEstado("Municipio no reconocido en el mapa interno.", true);
            return;
        }

        // Parseo seguro de números (tu DAO usa float)
        Float longitud = parseFloatUI(txtLongitud.getText());
        if (longitud == null) {
            setEstado("Longitud inválida. Usa números (ej: 120.5).", true);
            txtLongitud.requestFocus();
            return;
        }

        Float areaTotal = parseFloatUI(txtAreaTotal.getText());
        if (areaTotal == null) {
            setEstado("Área total inválida. Usa números (ej: 5000).", true);
            txtAreaTotal.requestFocus();
            return;
        }

        // Armar "ubicacion" con nombres (tu tabla fincas no tiene FK aún)
        String ubicacion = nombreMunSel + ", " + nombreDepSel;

        // Construir objeto y usar TU DAO
        try {
            com.piscicultura.monitoreo.model.GranjaPiscicola finca =
                    new com.piscicultura.monitoreo.model.GranjaPiscicola();
            finca.setNombre(nombreFinca);
            finca.setUbicacion(ubicacion);
            finca.setLongitud(longitud);
            finca.setAreaTotal(areaTotal);

            com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO dao =
                    new com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO(conn);

            boolean ok = dao.insertar(finca);
            if (!ok) {
                setEstado("No se pudo guardar la finca (sin filas afectadas).", true);
                return;
            }

            setEstado("Finca guardada: " + nombreFinca + " (" + ubicacion + ")", false);
            limpiarFormulario();

        } catch (Exception e) {
            setEstado("Error guardando finca: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // =====================================================================================
    // LISTADO EN COLUMNA CON SCROLL (NO MOVIBLE)
    // =====================================================================================

    @FXML
    protected void onVerFincas() {
        try {
            // Crea una sola vez el ScrollPane y el VBox (reutilizables)
            ensureListadoUI();

            // Poblar
            boxListado.getChildren().clear();

            com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO dao =
                    new com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO(conn);
            java.util.List<com.piscicultura.monitoreo.model.GranjaPiscicola> fincas = dao.listarTodos();

            for (com.piscicultura.monitoreo.model.GranjaPiscicola finca : fincas) {
                Node tarjeta = crearTarjetaFinca(finca);
                boxListado.getChildren().add(tarjeta);
            }
            setEstado("Mostrando " + fincas.size() + " finca(s).", false);
        } catch (Exception e) {
            setEstado("Error listando fincas: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /** Garantiza que exista un ScrollPane con un VBox llenando el AnchorPane */
    private void ensureListadoUI() {
        if (spListado == null || boxListado == null) {
            desktopPane.getChildren().clear();

            spListado = new ScrollPane();
            spListado.setFitToWidth(true);
            spListado.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            spListado.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            spListado.setStyle("-fx-background-color:transparent;");

            boxListado = new VBox(10);
            boxListado.setStyle("-fx-padding:16;");
            spListado.setContent(boxListado);

            AnchorPane.setTopAnchor(spListado, 0.0);
            AnchorPane.setRightAnchor(spListado, 0.0);
            AnchorPane.setBottomAnchor(spListado, 0.0);
            AnchorPane.setLeftAnchor(spListado, 0.0);

            desktopPane.getChildren().add(spListado);
        }
    }

    private Node crearTarjetaFinca(com.piscicultura.monitoreo.model.GranjaPiscicola finca) {
     BorderPane card = new BorderPane();
     card.setStyle("""
         -fx-background-color: white;
         -fx-background-radius: 12;
         -fx-border-radius: 12;
         -fx-border-color: #dfe6e9;
         -fx-border-width: 1;
         -fx-cursor: hand;
     """);

     Label lblTitulo = new Label("Finca #" + finca.getIdGranja() + " — " + safe(finca.getNombre()));
     lblTitulo.setStyle("-fx-font-weight: 700; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

     Button btnAbrir = new Button("Abrir");
     btnAbrir.setOnAction(e -> abrirDetalleFinca(finca));
     btnAbrir.setStyle("""
         -fx-background-color:#1976d2;
         -fx-text-fill:white;
         -fx-font-weight:bold;
         -fx-background-radius:6;
         -fx-cursor: hand;
     """);
     btnAbrir.setPadding(new Insets(4, 10, 4, 10));

     Button btnEliminar = new Button("Eliminar");
     btnEliminar.setStyle("""
         -fx-background-color:#e53935;
         -fx-text-fill:white;
         -fx-font-weight:bold;
         -fx-background-radius:6;
         -fx-cursor: hand;
     """);
     // Padding para que no se vea "pegado"
     btnEliminar.setPadding(new Insets(4, 10, 4, 10));
     // Acción con e.consume() para no abrir el detalle al hacer click
     btnEliminar.setOnAction(e -> {
         e.consume();
         eliminarFinca(finca);
     });

     // Separador elástico para empujar botones a la derecha
     Region spacer = new Region();
     HBox.setHgrow(spacer, Priority.ALWAYS);

     // HBox con espacio de 10px entre nodos
     HBox header = new HBox(10, lblTitulo, spacer, btnAbrir, btnEliminar);
     header.setAlignment(javafx.geometry.Pos.CENTER_LEFT); // usa Pos si ya lo importas
     header.setPadding(new Insets(10, 12, 8, 12));

     VBox info = new VBox(4,
             new Label("Ubicación: " + safe(finca.getUbicacion())),
             new Label("Longitud: " + finca.getLongitud() + " m"),
             new Label("Área total: " + finca.getAreaTotal() + " m²")
     );
     info.setStyle("-fx-text-fill:#455a64; -fx-font-size:12px;");
     BorderPane.setMargin(info, new Insets(0, 12, 12, 12));

     card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
             + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 14,0,0,3);"));
     card.setOnMouseExited(e -> card.setStyle("""
         -fx-background-color: white;
         -fx-background-radius: 12;
         -fx-border-radius: 12;
         -fx-border-color: #dfe6e9;
         -fx-border-width: 1;
         -fx-cursor: hand;
     """));

     // Click en tarjeta abre detalle
     card.setOnMouseClicked(e -> abrirDetalleFinca(finca));

     card.setTop(header);
     card.setCenter(info);
     return card;
 }



    private void abrirDetalleFinca(com.piscicultura.monitoreo.model.GranjaPiscicola finca) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/com/piscicultura/monitoreo/view/estanque.fxml"));
            Pane root = loader.load();

            // === SNAPSHOT del contenido actual del desktopPane ===
            // (esto captura EXACTAMENTE lo que había antes de abrir estanques)
            List<Node> prevChildrenSnapshot = new ArrayList<>(desktopPane.getChildren());

            com.piscicultura.monitoreo.controller.EstanqueController ctrl = loader.getController();

            // Acción de volver: restaurar el snapshot tal cual estaba
            Runnable onBack = () -> {
                System.out.println("[CrudFincaController] RUN volver -> restaurando snapshot");
                desktopPane.getChildren().setAll(prevChildrenSnapshot);
            };

            // pasa onBack al controller de estanques
            ctrl.initData(finca, conn, onBack);

            // reemplaza por la vista de estanques
            desktopPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);

            setEstado("Estanques de: " + safe(finca.getNombre()), false);

        } catch (Exception ex) {
            setEstado("No se pudo abrir los estanques: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }


    // =====================================================================================

    /** Parsea un número float desde UI; acepta coma o punto decimal. Devuelve null si es inválido. */
    
    private Float parseFloatUI(String txt) {
        if (txt == null) return null;
        String s = txt.trim();
        if (s.isEmpty()) return null;
        // normaliza coma -> punto
        s = s.replace(',', '.');
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
        setEstado("Operación cancelada.", false);
    }
    private void eliminarFinca(com.piscicultura.monitoreo.model.GranjaPiscicola finca) {
    if (finca == null) {
        setEstado("Finca nula.", true);
        return;
    }

    // Confirmación
    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Eliminar finca");
    confirm.setHeaderText("¿Eliminar la finca #" + finca.getIdGranja() + " ?");
    confirm.setContentText("Se eliminará la finca y todos sus estanques y datos relacionados. Esta acción no se puede deshacer.");

    Optional<javafx.scene.control.ButtonType> r = confirm.showAndWait();
    if (r.isEmpty() || r.get() != javafx.scene.control.ButtonType.OK) {
        setEstado("Eliminación cancelada.", false);
        return;
    }

    // Borrado
    try {
        if (conn == null || conn.isClosed()) {
            setEstado("No hay conexión a BD para eliminar.", true);
            return;
        }

        // Transacción
        boolean prevAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO dao =
                    new com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO(conn);

            // Método que ELIMINA TODO lo relacionado a la finca:
            // - relaciones de estanques (mediciones, alarmas, estanque_especie, etc.)
            // - estanques
            // - la finca
            boolean ok = dao.eliminarCompleta(finca.getIdGranja());
            if (!ok) {
                conn.rollback();
                setEstado("No se pudo eliminar la finca (sin filas afectadas).", true);
                return;
            }
            conn.commit();
            setEstado("Finca #" + finca.getIdGranja() + " eliminada.", false);
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(prevAuto);
        }

        // Refrescar listado
        onVerFincas();

    } catch (Exception ex) {
        setEstado("Error eliminando finca: " + ex.getMessage(), true);
        ex.printStackTrace();
    }
}

    private void limpiarFormulario() {
        txtNombre.clear();
        txtLongitud.clear();
        txtAreaTotal.clear();
        cboDepartamento.getSelectionModel().clearSelection();
        cboMunicipio.getSelectionModel().clearSelection();
        cboMunicipio.setDisable(true);
    }

    // CrudFincaController.java
    private void setEstado(String msg, boolean error) {
        if (lblEstado != null) {
            lblEstado.setText(msg);
            lblEstado.setStyle(error ? "-fx-text-fill:#c62828;" : "-fx-text-fill:#2e7d32;");
        } else {
            System.out.println((error ? "[ERROR] " : "[INFO] ") + msg);
        }
    }

    @FXML
    private void onActualizar() {
        onVerFincas(); // repuebla la lista
    }

    @FXML
    private void onCerrarTodas() {
        // Limpia el contenido del listado (sin destruir el ScrollPane)
        if (boxListado != null) {
            boxListado.getChildren().clear();
            setEstado("Listado vacío.", false);
        } else if (desktopPane != null) {
            desktopPane.getChildren().clear();
            setEstado("Listado vacío.", false);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
