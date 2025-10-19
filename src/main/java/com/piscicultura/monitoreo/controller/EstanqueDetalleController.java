package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.model.Estanque;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class EstanqueDetalleController {

    @FXML private Label lblTitulo;
    @FXML private Label lblTemp, lblPh, lblOx, lblNh3, lblCap;
    @FXML private Label semTemp, semPh, semOx, semNh3;
    @FXML private BarChart<String, Number> barActuales;
    @FXML private LineChart<String, Number> lineHistorico;



    private Estanque estanque;
    private Connection conn;

    // ===== RANGOS CONFIGURABLES (ajústalos según tu especie / proyecto) =====
    // Temperatura ideal ~ 24-30 °C (ejemplo genérico)
    private static final double TEMP_OK_MIN = 24.0, TEMP_OK_MAX = 30.0;
    private static final double TEMP_WARN_MIN = 22.0, TEMP_WARN_MAX = 32.0;

    // pH ideal 6.5 - 9.0 (FAO general)
    private static final double PH_OK_MIN = 6.5, PH_OK_MAX = 9.0;
    private static final double PH_WARN_MIN = 6.0, PH_WARN_MAX = 9.5;

    // Oxígeno disuelto ideal >= 5 mg/L
    private static final double OX_OK_MIN = 5.0;
    private static final double OX_WARN_MIN = 4.0;

    // Amoniaco (NH3) ideal < 0.05 mg/L
    private static final double NH3_OK_MAX = 0.05;
    private static final double NH3_WARN_MAX = 0.1;

    public void init(Estanque estanque, Connection conn) {
        this.estanque = estanque;
        this.conn = conn;
        lblTitulo.setText("Estanque #" + estanque.getIdEstanque() + " — " + safe(estanque.getTipo()));
        refrescar();
    }

    @FXML
    private void onRefrescar() {
        refrescar();
    }

    private void refrescar() {
        // 1) Valores actuales del objeto Estanque
        double t = estanque.getTemperaturaAgua();
        double ph = estanque.getPhAgua();
        double ox = estanque.getOxigeno();
        double nh3 = estanque.getAmoniaco();
        double cap = estanque.getCapacidad();

        lblTemp.setText(fmt(t));
        lblPh.setText(fmt(ph));
        lblOx.setText(fmt(ox));
        lblNh3.setText(fmt(nh3));
        lblCap.setText(fmt(cap));

        // 2) Semáforos
        setSemaforo(semTemp, rango3(t, TEMP_OK_MIN, TEMP_OK_MAX, TEMP_WARN_MIN, TEMP_WARN_MAX), "Temp");
        setSemaforo(semPh,   rango3(ph, PH_OK_MIN, PH_OK_MAX, PH_WARN_MIN, PH_WARN_MAX), "pH");
        setSemaforo(semOx,   rangoMin(ox, OX_OK_MIN, OX_WARN_MIN), "O₂");
        setSemaforo(semNh3,  rangoMax(nh3, NH3_OK_MAX, NH3_WARN_MAX), "NH₃");

        // 3) BarChart actuales
        barActuales.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Actual");
        s.getData().add(new XYChart.Data<>("Temp (°C)", t));
        s.getData().add(new XYChart.Data<>("pH", ph));
        s.getData().add(new XYChart.Data<>("O₂ (mg/L)", ox));
        s.getData().add(new XYChart.Data<>("NH₃ (mg/L)", nh3));
        barActuales.getData().add(s);

        // 4) Histórico (si hay tabla medicion)
        lineHistorico.getData().clear();
        if (conn != null) {
            cargarHistoricoYGraficar(estanque.getIdEstanque());
        }
    }

    // ===== Semáforos =====
    // devuelve "green" | "yellow" | "red"
    private String rango3(double v, double okMin, double okMax, double warnMin, double warnMax) {
        if (Double.isNaN(v)) return "red";
        if (v >= okMin && v <= okMax) return "green";
        if (v >= warnMin && v <= warnMax) return "yellow";
        return "red";
    }

    // valor mínimo deseable (más es mejor)
    private String rangoMin(double v, double okMin, double warnMin) {
        if (Double.isNaN(v)) return "red";
        if (v >= okMin) return "green";
        if (v >= warnMin) return "yellow";
        return "red";
    }

    // valor máximo deseable (menos es mejor)
    private String rangoMax(double v, double okMax, double warnMax) {
        if (Double.isNaN(v)) return "red";
        if (v <= okMax) return "green";
        if (v <= warnMax) return "yellow";
        return "red";
    }

    private void setSemaforo(Label lbl, String color, String name) {
        switch (color) {
            case "green"  -> lbl.setText("✔ Óptimo");
            case "yellow" -> lbl.setText("⚠ Atención");
            default       -> lbl.setText("✖ Crítico");
        }
        String c = switch (color) {
            case "green"  -> "#2e7d32";
            case "yellow" -> "#f9a825";
            default       -> "#c62828";
        };
        lbl.setStyle("-fx-text-fill:" + c + "; -fx-font-weight:bold;");
        lbl.setAccessibleText(name + " " + lbl.getText());
    }

    private void cargarHistoricoYGraficar(int idEstanque) {
        final String sql = """
            SELECT fecha_medicion, temperatura_agua, ph_agua, oxigeno, amoniaco
            FROM mediciones
            WHERE id_estanque = ?
            ORDER BY fecha_medicion ASC
            LIMIT 200
        """;

        // Series para cada variable
        XYChart.Series<String, Number> sT = new XYChart.Series<>(); sT.setName("Temp (°C)");
        XYChart.Series<String, Number> sP = new XYChart.Series<>(); sP.setName("pH");
        XYChart.Series<String, Number> sO = new XYChart.Series<>(); sO.setName("O₂ (mg/L)");
        XYChart.Series<String, Number> sN = new XYChart.Series<>(); sN.setName("NH₃ (mg/L)");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_medicion");
                    String label = fecha.toLocalDateTime().format(fmt);

                    double temp = rs.getDouble("temperatura_agua");
                    double ph   = rs.getDouble("ph_agua");
                    double ox   = rs.getDouble("oxigeno");
                    double nh3  = rs.getDouble("amoniaco");

                    sT.getData().add(new XYChart.Data<>(label, temp));
                    sP.getData().add(new XYChart.Data<>(label, ph));
                    sO.getData().add(new XYChart.Data<>(label, ox));
                    sN.getData().add(new XYChart.Data<>(label, nh3));
                }
            }
        } catch (SQLException ex) {
            System.out.println("[Histórico] Error consultando mediciones: " + ex.getMessage());
            return;
        }

        // Si no hay datos, salimos
        if (sT.getData().isEmpty()) {
            System.out.println("[Histórico] No hay datos de mediciones para el estanque " + idEstanque);
            return;
        }

        // Limpiamos y añadimos las series
        lineHistorico.getData().clear();
        lineHistorico.getData().addAll(sT, sP, sO, sN);

        // Tooltips bonitos
        for (XYChart.Series<String, Number> serie : lineHistorico.getData()) {
            for (XYChart.Data<String, Number> punto : serie.getData()) {
                Tooltip tip = new Tooltip(serie.getName() + "\n" +
                        punto.getXValue() + " → " + punto.getYValue());
                Tooltip.install(punto.getNode(), tip);
            }
        }
    }


    /** Estados */
    private enum Status { OK, WARN, CRIT }

    /** Clasifica una métrica en OK/WARN/CRIT usando rango deseable + tolerancia */
    private Status eval(double v, double min, double max, double warnPct) {
        if (Double.isNaN(v)) return Status.CRIT;
        double span  = max - min;
        double warn  = span * warnPct;   // margen de advertencia
        double loW   = min - warn;
        double hiW   = max + warn;

        if (v >= min && v <= max) return Status.OK;
        if (v >= loW && v <= hiW) return Status.WARN;
        return Status.CRIT;
    }

    /** Texto bonito del estado */
    private String label(Status s) {
        return switch (s) {
            case OK   -> "Óptimo";
            case WARN -> "Atención";
            case CRIT -> "Crítico";
        };
    }

    /** Icono unicode del estado */
    private String icon(Status s) {
        return switch (s) {
            case OK   -> "✓";
            case WARN -> "⚠";
            case CRIT -> "⛔";
        };
    }

    /** Crea un chip visual para una métrica */
    private HBox chip(String nombre, String unidad, double valor, double min, double max, double warnPct) {
        Status s = eval(valor, min, max, warnPct);

        // Pildora de color
        Region dot = new Region();
        dot.getStyleClass().addAll("color-dot",
                s == Status.OK ? "ok" : s == Status.WARN ? "warn" : "crit");

        // Métrica (nombre + valor + unidad)
        Label lName  = new Label(nombre);
        lName.getStyleClass().add("metric-name");

        Label lVal   = new Label(String.format("%.2f", valor));
        lVal.getStyleClass().add("metric-value");

        Label lUnit  = new Label(" " + unidad);
        lUnit.getStyleClass().add("metric-unit");

        HBox metric = new HBox(lName, new Region());
        HBox.setHgrow(metric.getChildren().get(1), Priority.ALWAYS);

        // Estado (icono + texto)
        Label lIcon  = new Label(icon(s));
        Label lState = new Label(label(s));
        lState.getStyleClass().add("state-label");
        HBox state = new HBox(6, lIcon, lState);
        state.setAlignment(Pos.CENTER_LEFT);

        // Separadores
        Region sep1 = new Region(); sep1.getStyleClass().add("vsep");
        Region sep2 = new Region(); sep2.getStyleClass().add("vsep");

        // Chip
        HBox chip = new HBox(10,
                dot,
                new HBox(6, new Label(nombre), sep1, new HBox(new Label(String.format("%.2f", valor)), lUnit)),
                sep2,
                state
        );
        chip.getStyleClass().add("status-chip");
        chip.setAlignment(Pos.CENTER_LEFT);

        // Tooltip con rango recomendado
        String tip = String.format("%s recomendado: %.2f – %.2f %s",
                nombre, min, max, unidad);
        Tooltip.install(chip, new Tooltip(tip));

        return chip;
    }

    /** Sección completa del semáforo en una tarjetita */
    private VBox buildSemaforoCard(Estanque e) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("Semáforo de calidad");
        title.getStyleClass().add("card-title");

        // Ajusta rangos según tu criterio/tipo de especie
        HBox chipTemp = chip("Temperatura", "°C", e.getTemperaturaAgua(), 27.5, 29.5, 0.15);
        HBox chipPh   = chip("pH", "",             e.getPhAgua(),         7.0,  7.5,  0.10);
        HBox chipO2   = chip("Oxígeno", "mg/L",    e.getOxigeno(),        5.8,  7.2,  0.12);
        HBox chipNH3  = chip("Amoniaco", "mg/L",   e.getAmoniaco(),       0.00, 0.05, 0.20);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid-2col");
        grid.addRow(0, chipTemp, chipPh);
        grid.addRow(1, chipO2,   chipNH3);

        card.getChildren().addAll(title, grid);
        return card;
    }
    private static String safe(String s) {
    return (s == null || s.isBlank()) ? "" : s.trim();
    }
    
    private static String fmt(double v) {
        if (Double.isNaN(v)) return "-";
        if (Double.isInfinite(v)) return "∞";

        // Redondeo visual a 2 decimales
        String s = String.format("%.2f", v);

        // Quita .00 si el número es entero
        if (s.endsWith(".00")) {
            s = s.substring(0, s.length() - 3);
        }

        return s;
    }



}
