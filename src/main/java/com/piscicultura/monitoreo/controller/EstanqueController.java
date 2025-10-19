package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.model.Estanque;
import com.piscicultura.monitoreo.model.GranjaPiscicola;
import com.piscicultura.monitoreo.model.Especie;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; 
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Pos;

public class EstanqueController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField txtBuscar;
    @FXML private VBox boxListado;
    @FXML private Label lblEstado;

    private Runnable onBack;
    private Pane hostContainer;
    private Connection conn;
    private GranjaPiscicola finca;

    private List<Estanque> estanquesCache = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { }

    public void initData(GranjaPiscicola finca, Connection conn, Runnable onBack) {
        this.finca  = finca;
        this.conn   = conn;
        this.onBack = onBack;
        cargarDesdeModelo();
    }

    // ==========================================
    // Carga desde BD con especies (JOIN)
    // ==========================================
    private void cargarDesdeModelo() {
        try {
            List<Estanque> origen;
            if (conn != null) {
                try {
                    com.piscicultura.monitoreo.dao.EstanqueDAO dao =
                            new com.piscicultura.monitoreo.dao.EstanqueDAO(conn);
                    origen = dao.listarPorFincaConEspecies(finca.getIdGranja());
                } catch (Exception ex) {
                    setEstado("No se pudo leer BD: " + ex.getMessage() + ". Usando cache en memoria.", true);
                    origen = (finca.getEstanques() != null) ? finca.getEstanques() : List.of();
                }
            } else {
                origen = (finca.getEstanques() != null) ? finca.getEstanques() : List.of();
            }

            estanquesCache = new ArrayList<>(origen);
            finca.setEstanques(new ArrayList<>(origen));

            poblarListado(estanquesCache);
            setEstado("Se cargaron " + estanquesCache.size() + " estanque(s).", false);

        } catch (Exception e) {
            setEstado("Error cargando estanques: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void poblarListado(List<Estanque> data) {
        boxListado.getChildren().clear();
        if (data == null || data.isEmpty()) {
            Label vacio = new Label("No hay estanques registrados para esta finca.");
            vacio.setStyle("-fx-text-fill:#607d8b; -fx-font-size:13px;");
            boxListado.getChildren().add(vacio);
            return;
        }
        for (Estanque est : data) {
            Node card = crearTarjetaEstanque(est);
            boxListado.getChildren().add(card);
        }
    }

    private Node crearTarjetaEstanque(Estanque est) {
       BorderPane card = new BorderPane();
       card.setStyle("""
           -fx-background-color: white;
           -fx-background-radius: 12;
           -fx-border-radius: 12;
           -fx-border-color: #dfe6e9;
           -fx-border-width: 1;
           -fx-cursor: hand;
       """);

       Label t = new Label("Estanque #" + est.getIdEstanque() + " — " + safe(est.getTipo()));
       t.setStyle("-fx-font-weight: 700; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

       // --- Botón Abrir ---
       Button btnAbrir = new Button("Abrir");
       btnAbrir.setStyle("""
           -fx-background-color:#1976d2;
           -fx-text-fill:white;
           -fx-font-weight:bold;
           -fx-background-radius:6;
           -fx-cursor: hand;
       """);
       btnAbrir.setPadding(new Insets(4, 10, 4, 10));
       btnAbrir.setOnAction(e -> abrirDetalleEstanque(est));

       // --- Botón Eliminar ---
       Button btnEliminar = new Button("Eliminar");
       btnEliminar.setStyle("""
           -fx-background-color:#e53935;
           -fx-text-fill:white;
           -fx-font-weight:bold;
           -fx-background-radius:6;
           -fx-cursor: hand;
       """);
       btnEliminar.setPadding(new Insets(4, 10, 4, 10));
       btnEliminar.setOnAction(e -> {
           e.consume(); // evita que abra el detalle
           eliminarEstanque(est, card);
       });

       // --- Encabezado con separación estética ---
       Region spacer = new Region();
       HBox.setHgrow(spacer, Priority.ALWAYS);

       HBox header = new HBox(10, t, spacer, btnAbrir, btnEliminar); // 10 px de espacio
       header.setAlignment(Pos.CENTER_LEFT);
       header.setPadding(new Insets(10, 12, 8, 12));

       // --- Contenido principal ---
       String especiesTexto = (est.getEspecies() == null || est.getEspecies().isEmpty())
               ? "—"
               : est.getEspecies().stream()
                   .map(esp -> safe(esp.getNombreComun()))
                   .collect(Collectors.joining(", "));

       String lineaCalidad = String.format("pH: %s · O₂: %s mg/L · NH₃: %s mg/L",
               fmt(est.getPhAgua()), fmt(est.getOxigeno()), fmt(est.getAmoniaco()));

       VBox info = new VBox(4,
               new Label("Estado: " + safe(est.getEstado())),
               new Label("Capacidad: " + fmt(est.getCapacidad()) + " m³"),
               new Label("Especies: " + especiesTexto),
               new Label("T° agua: " + fmt(est.getTemperaturaAgua()) + " °C"),
               new Label(lineaCalidad)
       );
       info.setStyle("-fx-text-fill:#455a64; -fx-font-size:12px;");
       BorderPane.setMargin(info, new Insets(0, 12, 12, 12));

       // --- Efectos hover ---
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

       // --- Click en tarjeta abre detalle (excepto si se presiona Eliminar) ---
       card.setOnMouseClicked(e -> abrirDetalleEstanque(est));

       card.setTop(header);
       card.setCenter(info);
       return card;
   }


    // ================== Acciones UI ==================
    @FXML
    private void onFiltrar() {
        String q = safe(txtBuscar.getText()).toLowerCase();
        if (q.isBlank()) {
            poblarListado(estanquesCache);
            setEstado("Filtro vacío. Se muestran " + estanquesCache.size() + " estanques.", false);
            return;
        }
        List<Estanque> filtrados = estanquesCache.stream()
                .filter(e -> safe(e.getTipo()).toLowerCase().contains(q)
                          || safe(e.getEstado()).toLowerCase().contains(q)
                          || (e.getEspecies() != null && e.getEspecies().stream().anyMatch(
                                 s -> safe(s.getNombreComun()).toLowerCase().contains(q)
                                   || safe(s.getNombreCientifico()).toLowerCase().contains(q)
                             )))
                .collect(Collectors.toList());
        poblarListado(filtrados);
        setEstado("Filtro aplicado: " + filtrados.size() + " coinciden.", false);
    }

    @FXML
    private void onLimpiarFiltro() {
        txtBuscar.clear();
        poblarListado(estanquesCache);
        setEstado("Filtro limpiado.", false);
    }
    
    @FXML
    private void onNuevoEstanque() {
        if (finca == null) {
            setEstado("No hay finca seleccionada para asociar el estanque.", true);
            return;
        }

        // 1) Captura en diálogo (con especie, cantidad, fecha)
        DialogResult capture = buildNuevoEstanqueDialog();
        if (capture == null) {
            setEstado("Creación cancelada.", false);
            return;
        }

        Estanque nuevo             = capture.estanque;
        Especie especieElegida     = capture.especie;         // puede ser null
        Integer cantElegida        = capture.cantidad;        // puede ser null
        java.sql.Date fechaElegida = capture.fechaSiembra;    // puede ser null (¡nota: java.sql.Date!)

        // 2) Persistencia
        try {
            if (conn != null) {
                com.piscicultura.monitoreo.dao.EstanqueDAO dao =
                        new com.piscicultura.monitoreo.dao.EstanqueDAO(conn);

                int idGen = dao.insertar(nuevo, finca.getIdGranja());
                if (idGen <= 0) {
                    setEstado("No se pudo insertar el estanque en la BD.", true);
                    return;
                }
                nuevo.setIdEstanque(idGen);

                // Inserta la relación estanque-especie si el usuario eligió una
                if (especieElegida != null) {
                    dao.insertarRelacionEstanqueEspecie(
                            idGen,
                            especieElegida.getIdEspecie(),
                            cantElegida,
                            fechaElegida
                    );
                    // Refleja en el modelo en memoria para que la tarjeta la muestre sin recargar
                    if (nuevo.getEspecies() == null) {
                        nuevo.setEspecies(new ArrayList<>());
                    }
                    nuevo.agregarEspecie(especieElegida);
                }
            } else {
                // Sin BD: id temporal y agregamos la especie localmente
                int tempId = - (finca.getEstanques().size() + 1);
                nuevo.setIdEstanque(tempId);

                if (especieElegida != null) {
                    if (nuevo.getEspecies() == null) {
                        nuevo.setEspecies(new ArrayList<>());
                    }
                    nuevo.agregarEspecie(especieElegida);
                }
            }

            // Añade al modelo de la finca
            finca.getEstanques().add(nuevo);

            // 3) Refrescar UI
            poblarListado(finca.getEstanques());
            setEstado("Estanque creado: #" + nuevo.getIdEstanque() + " (" + safe(nuevo.getTipo()) + ")", false);

        } catch (Exception ex) {
            setEstado("Error creando estanque: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }


    // ==========================================
    // Diálogo NUEVO estanque (Opción B)
    // ==========================================
    private static class DialogResult {
        Estanque estanque;
        Especie  especie;
        Integer  cantidad;
        Date     fechaSiembra;
    }

    private DialogResult buildNuevoEstanqueDialogReturn() { return buildNuevoEstanqueDialog(); }

    private DialogResult buildNuevoEstanqueDialog() {
        Dialog<DialogResult> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Estanque");
        dialog.setHeaderText("Ingrese los datos del nuevo estanque");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Campos del estanque
        TextField txtTipo = new TextField();
        txtTipo.setPromptText("Ej: Circular, Rectangular, Tanque...");

        ComboBox<String> cboEstado = new ComboBox<>();
        cboEstado.getItems().addAll("Activo", "Inactivo", "Mantenimiento");
        cboEstado.setEditable(false);
        cboEstado.setPromptText("Seleccione...");

        TextField txtCapacidad = new TextField();
        txtCapacidad.setPromptText("m³");

        // Especie (opcional)
        ComboBox<Especie> cboEspecie = new ComboBox<>();
        cboEspecie.setPromptText("Elige una especie…");
        List<Especie> especies = cargarEspeciesDesdeBD();
        cboEspecie.getItems().addAll(especies);
        cboEspecie.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Especie item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null :
                        safe(item.getNombreComun()) + " (" + safe(item.getNombreCientifico()) + ")");
            }
        });
        cboEspecie.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Especie item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null :
                        safe(item.getNombreComun()) + " (" + safe(item.getNombreCientifico()) + ")");
            }
        });

        if (conn == null) {
            cboEspecie.setDisable(true);
            cboEspecie.setPromptText("Conéctate a la BD para listar especies");
        } else if (especies.isEmpty()) {
            cboEspecie.setDisable(true);
            cboEspecie.setPromptText("No hay especies cargadas en BD");
        }

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cant. (opcional)");

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha siembra (opcional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        int r = 0;
        grid.add(new Label("Tipo:"), 0, r);            grid.add(txtTipo, 1, r++);
        grid.add(new Label("Estado:"), 0, r);          grid.add(cboEstado, 1, r++);
        grid.add(new Label("Capacidad (m³):"), 0, r);  grid.add(txtCapacidad, 1, r++);
        grid.add(new Label("Especie:"), 0, r);         grid.add(cboEspecie, 1, r++);
        grid.add(new Label("Cantidad:"), 0, r);        grid.add(txtCantidad, 1, r++);
        grid.add(new Label("Fecha siembra:"), 0, r);   grid.add(dpFecha, 1, r++);

        dialog.getDialogPane().setContent(grid);

        Node btnOkNode = dialog.getDialogPane().lookupButton(btnGuardar);
        btnOkNode.setDisable(true);

    Runnable validate = () -> {
        boolean ok = (!safe(txtTipo.getText()).isBlank()
                   && cboEstado.getValue() != null
                   && (parseFloat(txtCapacidad.getText()) != null));
        btnOkNode.setDisable(!ok);
    };


        txtTipo.textProperty().addListener((o, a, b) -> validate.run());
        cboEstado.valueProperty().addListener((o, a, b) -> validate.run());
        txtCapacidad.textProperty().addListener((o, a, b) -> validate.run());
        validate.run();

        dialog.setResultConverter(bt -> {
            if (bt == btnGuardar) {
                DialogResult res = new DialogResult();

                Estanque e = new Estanque();
                e.setTipo(safe(txtTipo.getText()));
                e.setEstado(safe(cboEstado.getValue()));
                e.setCapacidad(parseFloat(txtCapacidad.getText()));

                res.estanque = e;

                Especie sel = cboEspecie.getValue();
                if (sel != null) {
                    // Clon sencillo para no tocar el objeto original
                    Especie esp = new Especie();
                    esp.setIdEspecie(sel.getIdEspecie());
                    esp.setNombreComun(sel.getNombreComun());
                    esp.setNombreCientifico(sel.getNombreCientifico());

                    Integer cant = tryParseInt(txtCantidad.getText());
                    res.cantidad = cant;

                    LocalDate ld = dpFecha.getValue();
                    res.fechaSiembra = (ld == null) ? null : Date.valueOf(ld);

                    // guarda la especie escogida (sólo para refrescar UI inmediata)
                    esp.setCantidad(cant == null ? 0 : cant);
                    if (res.fechaSiembra != null) esp.setFechaSiembra(res.fechaSiembra);

                    res.especie = esp;
                }
                return res;
            }
            return null;
        });

        Optional<DialogResult> out = dialog.showAndWait();
        return out.orElse(null);
    }

    // ================== Navegación ==================
    @FXML
    private void onSalir() {
        if (onBack != null) {
            onBack.run();
        } else {
            setEstado("Volver: acción no configurada (onBack = null).", true);
        }
    }

private void abrirDetalleEstanque(Estanque est) {
    try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/com/piscicultura/monitoreo/view/estanque_detalle.fxml")
        );
        Pane root = loader.load();

        com.piscicultura.monitoreo.controller.EstanqueDetalleController ctrl = loader.getController();
        // Le pasas el estanque actual y la conexión (para histórico opcional)
        ctrl.init(est, conn);

        javafx.stage.Stage dlg = new javafx.stage.Stage();
        dlg.setTitle("Detalle del Estanque #" + est.getIdEstanque());
        dlg.setScene(new javafx.scene.Scene(root));
        //dlg.initOwner(lblTitulo.getScene().getWindow());
        dlg.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dlg.setResizable(true);
        dlg.show();

        setEstado("Detalle abierto del estanque #" + est.getIdEstanque(), false);

    } catch (Exception ex) {
        setEstado("No se pudo abrir el detalle: " + ex.getMessage(), true);
        ex.printStackTrace();
    }
}


    // ================== Utilidades ==================
    private void setEstado(String msg, boolean error) {
        if (lblEstado != null) {
            lblEstado.setText(msg);
            lblEstado.setStyle(error ? "-fx-text-fill:#c62828;" : "-fx-text-fill:#2e7d32;");
        } else {
            System.out.println((error ? "[ERROR] " : "[INFO] ") + msg);
        }
    }

    private List<Especie> cargarEspeciesDesdeBD() {
        List<Especie> lista = new ArrayList<>();
        if (conn == null) return lista;

        final String sql = """
            SELECT id_especie, nombre_cientifico, nombre_comun, edad_dias, 
                   peso_promedio_g, fecha_siembra, cantidad
            FROM especie
            ORDER BY nombre_comun
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Especie esp = new Especie();
                esp.setIdEspecie(rs.getInt("id_especie"));
                esp.setNombreCientifico(nullSafe(rs.getString("nombre_cientifico")));
                esp.setNombreComun(nullSafe(rs.getString("nombre_comun")));
                esp.setEdadDias(rs.getInt("edad_dias"));
                esp.setPesoPromedioG(rs.getFloat("peso_promedio_g"));
                esp.setFechaSiembra(rs.getDate("fecha_siembra"));
                esp.setCantidad(rs.getInt("cantidad"));
                lista.add(esp);
            }
        } catch (SQLException ex) {
            setEstado("Error cargando especies: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
        return lista;
    }

    private Integer tryParseInt(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (NumberFormatException e) { return null; }
    }

    private Float parseFloat(String s) {
        if (s == null) return null;
        String n = s.trim().replace(',', '.');
        if (n.isEmpty()) return null;
        try { return Float.parseFloat(n); } catch (NumberFormatException ex) { return null; }
    }

    private String nullSafe(String s) { return (s == null) ? "" : s; }
    private String safe(String s) { return (s == null) ? "" : s; }

    private String fmt(float v) {
        if (Float.isNaN(v)) return "-";
        if (Float.isInfinite(v)) return "∞";
        String s = String.valueOf(v);
        if (s.endsWith(".0")) s = s.substring(0, s.length()-2);
        return s;
    }
        /** Elimina un estanque (confirmando con el usuario), borra en BD si aplica, 
     *  lo quita de las listas en memoria y refresca el listado. */
    private void eliminarEstanque(Estanque est, Node cardNode) {
        if (est == null) {
            setEstado("Estanque nulo.", true);
            return;
        }

        // 1) Confirmación
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar estanque");
        confirm.setHeaderText("¿Eliminar el estanque #" + est.getIdEstanque() + "?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) {
            setEstado("Eliminación cancelada.", false);
            return;
        }

        try {
            // 2) Si hay BD y el id es real (>0), borramos en BD
            if (conn != null && !conn.isClosed() && est.getIdEstanque() > 0) {
                com.piscicultura.monitoreo.dao.EstanqueDAO dao =
                        new com.piscicultura.monitoreo.dao.EstanqueDAO(conn);

                // Sugerido: si tu DAO tiene un método "eliminarCompleto" que borre hijas (especie, mediciones, alarmas)
                // boolean ok = dao.eliminarCompleto(est.getIdEstanque());
                // Si no, usa eliminar() simple:
                boolean ok = dao.eliminar(est.getIdEstanque());

                if (!ok) {
                    setEstado("No se pudo eliminar el estanque en BD (id " + est.getIdEstanque() + ").", true);
                    return;
                }
            }

            // 3) Quitar de listas en memoria (finca y caché)
            if (finca != null && finca.getEstanques() != null) {
                finca.getEstanques().removeIf(e -> Objects.equals(e.getIdEstanque(), est.getIdEstanque()));
            }
            if (estanquesCache != null) {
                estanquesCache.removeIf(e -> Objects.equals(e.getIdEstanque(), est.getIdEstanque()));
            }

            // 4) Refrescar UI
            poblarListado(estanquesCache);
            setEstado("Estanque #" + est.getIdEstanque() + " eliminado.", false);

        } catch (Exception ex) {
            setEstado("Error eliminando estanque: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

}
