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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            conn = ConexionDB.getConnection(); // tu método existente
            cargarDepartamentos();

            cboDepartamento.setItems(departamentosItems);
            cboMunicipio.setItems(municipiosItems);
            cboMunicipio.setDisable(true);

            // Cuando cambie el departamento, carga sus municipios
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

        } catch (SQLException e) {
            setEstado("Error de conexión: " + e.getMessage(), true);
        } catch (Exception ex) {
            System.getLogger(CrudFincaController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
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
    
    @FXML
    private AnchorPane desktopPane; // debe coincidir con el fx:id del FXML

    @FXML
    protected void onVerFincas() {
        desktopPane.getChildren().clear();

        com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO dao =
                new com.piscicultura.monitoreo.dao.Granja_PiscicolaDAO(conn);
        java.util.List<com.piscicultura.monitoreo.model.GranjaPiscicola> fincas = dao.listarTodos();

        double x = 30, y = 30;   // posición inicial
        double dx = 28, dy = 24; // separación en cascada

        for (com.piscicultura.monitoreo.model.GranjaPiscicola finca : fincas) {
            Node ventana = crearVentanaInterna(finca);
            ((Region) ventana).setPrefSize(320, 180);
            ventana.setLayoutX(x);
            ventana.setLayoutY(y);

            desktopPane.getChildren().add(ventana);

            // Posición en cascada
            x += dx; y += dy;
            if (x + 340 > desktopPane.getWidth()) {
                x = 30; y += 40;
            }
        }

        setEstado("Mostrando " + fincas.size() + " finca(s).", false);
    }

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

    private void limpiarFormulario() {
        txtNombre.clear();
        txtLongitud.clear();
        txtAreaTotal.clear();
        cboDepartamento.getSelectionModel().clearSelection();
        cboMunicipio.getSelectionModel().clearSelection();
        cboMunicipio.setDisable(true);
    }

    private void setEstado(String msg, boolean error) {
        lblEstado.setText(msg);
        lblEstado.setStyle(error ? "-fx-text-fill:#c62828;" : "-fx-text-fill:#2e7d32;");
    }
    
    private Node crearVentanaInterna(com.piscicultura.monitoreo.model.GranjaPiscicola finca) {
    BorderPane root = new BorderPane();
    root.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #b0bec5;
        -fx-border-width: 1;
        -fx-background-radius: 10;
        -fx-border-radius: 10;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10,0,0,2);
    """);

    // ======= Cabecera =======
    Label lblTitulo = new Label("Finca #" + finca.getIdGranja());
    lblTitulo.setStyle("-fx-font-weight:bold; -fx-text-fill:#37474f;");

    Button btnCerrar = new Button("×");
    btnCerrar.setStyle("""
        -fx-background-color:transparent;
        -fx-text-fill:#78909c;
        -fx-font-size:16px;
    """);
    btnCerrar.setOnAction(e -> ((Pane)root.getParent()).getChildren().remove(root));

    HBox header = new HBox(lblTitulo, new Region(), btnCerrar);
    HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
    header.setPadding(new Insets(6,8,6,10));
    header.setStyle("-fx-background-color:linear-gradient(to bottom,#e8f3ff,#dcecff);"
                    + "-fx-background-radius:10 10 0 0; -fx-border-color:#cfd8dc; -fx-border-width:0 0 1 0;");

    // Drag
    makeDraggable(root, header);

    // ======= Contenido =======
    VBox info = new VBox(6);
    info.setPadding(new Insets(8));
    info.getChildren().addAll(
        new Label("Nombre: " + finca.getNombre()),
        new Label("Ubicación: " + finca.getUbicacion()),
        new Label("Longitud: " + finca.getLongitud() + " m"),
        new Label("Área total: " + finca.getAreaTotal() + " m²")
    );
    ScrollPane scroll = new ScrollPane(info);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color:transparent;");

    root.setTop(header);
    root.setCenter(scroll);
    root.setOnMouseClicked(e -> root.toFront());

    return root;
    }

    /** Permite arrastrar la ventana dentro del desktopPane */
    private void makeDraggable(Node node, Node handle) {
        final double[] offset = new double[2];
        handle.setOnMousePressed(e -> {
            offset[0] = e.getSceneX() - node.getLayoutX();
            offset[1] = e.getSceneY() - node.getLayoutY();
            node.toFront();
        });
        handle.setOnMouseDragged(e -> {
            double nx = e.getSceneX() - offset[0];
            double ny = e.getSceneY() - offset[1];
            nx = Math.max(0, Math.min(nx, desktopPane.getWidth() - ((Region) node).getWidth()));
            ny = Math.max(0, Math.min(ny, desktopPane.getHeight() - ((Region) node).getHeight()));
            node.setLayoutX(nx);
            node.setLayoutY(ny);
        });
    }
    @FXML
    private void onActualizar() {
        onVerFincas(); // reutiliza la lógica que ya tienes
    }

    @FXML
    private void onCerrarTodas() {
        if (desktopPane != null) {
            desktopPane.getChildren().clear();
            setEstado("Ventanas cerradas.", false);
        }
    }


}
