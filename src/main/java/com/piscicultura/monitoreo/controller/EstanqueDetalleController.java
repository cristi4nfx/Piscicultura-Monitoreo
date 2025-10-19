package com.piscicultura.monitoreo.controller;

import com.piscicultura.monitoreo.dao.EspecieDAO;
import com.piscicultura.monitoreo.model.Estanque;
import com.piscicultura.monitoreo.model.Especie;
import com.piscicultura.monitoreo.model.Parametro;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EstanqueDetalleController {

    @FXML private Label lblTitulo;
    @FXML private Label lblTemp, lblPh, lblOx, lblNh3, lblCap;
    @FXML private Label semTemp, semPh, semOx, semNh3;
    @FXML private Label icoTemp, icoPh, icoOx, icoNh3;
    @FXML private Region dotTemp, dotPh, dotOx, dotNh3;
    @FXML private BarChart<String, Number> barActuales;
    @FXML private LineChart<String, Number> lineHistorico;
    @FXML private VBox panelEspecieInfo;

    private Estanque estanque;
    private Connection conn;
    private EspecieDAO especieDAO;

    public void init(Estanque estanque, Connection conn) throws Exception {
        this.estanque = estanque;
        this.conn = conn;
        this.especieDAO = new EspecieDAO(conn);
        
        lblTitulo.setText("Estanque #" + estanque.getIdEstanque() + " — " + safe(estanque.getTipo()));
        cargarInfoEspecie();
        refrescar();
    }

    @FXML
    private void onRefrescar() throws Exception {
        refrescar();
    }

    private void refrescar() throws Exception {
        // 1) Valores actuales
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

        // 2) SEMÁFOROS DINÁMICOS (1 especie)
        setSemaforosDinamicos();

        // 3) Gráficos
        actualizarGraficoBarras(t, ph, ox, nh3);

        // 4) Histórico
        lineHistorico.getData().clear();
        if (conn != null) {
            cargarHistoricoYGraficar(estanque.getIdEstanque());
        }
    }

    // ==================== INFO DE LA ESPECIE ====================

    private void cargarInfoEspecie() throws Exception {
        panelEspecieInfo.getChildren().clear();
        
        // Verificar si hay especie asignada
        Especie especie = obtenerEspecieEstanque();
        if (especie == null) {
            Label sinEspecie = new Label("🐟 No hay especie asignada a este estanque");
            sinEspecie.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            panelEspecieInfo.getChildren().add(sinEspecie);
            return;
        }

        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #bdc3c7; -fx-border-width: 1;");

        // Header
        HBox header = new HBox(10);
        Label nombre = new Label(especie.getNombreComun());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label cientifico = new Label("(" + especie.getNombreCientifico() + ")");
        cientifico.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        Label cantidad = new Label("• " + especie.getCantidad() + " peces");
        cantidad.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(nombre, cientifico, spacer, cantidad);

        // Parámetros
        GridPane gridParametros = new GridPane();
        gridParametros.setHgap(15);
        gridParametros.setVgap(8);

        if (especie.getParametros() != null && !especie.getParametros().isEmpty()) {
            int row = 0;
            for (Parametro param : especie.getParametros()) {
                Label paramLabel = new Label("• " + param.getNombre() + ":");
                paramLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
                
                Label rangoLabel = new Label(String.format("%.1f - %.1f %s", 
                    param.getRangoMin(), param.getRangoMax(), param.getUnidad()));
                rangoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                
                gridParametros.add(paramLabel, 0, row);
                gridParametros.add(rangoLabel, 1, row);
                row++;
            }
        } else {
            Label sinParametros = new Label("No hay parámetros definidos para esta especie");
            sinParametros.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");
            gridParametros.add(sinParametros, 0, 0, 2, 1);
        }

        tarjeta.getChildren().addAll(header, gridParametros);
        panelEspecieInfo.getChildren().add(tarjeta);
    }

    private Especie obtenerEspecieEstanque() {
        if (estanque.getEspecies() != null && !estanque.getEspecies().isEmpty()) {
            return estanque.getEspecies().get(0);
        }
        return null;
    }

    // ==================== SEMÁFOROS DINÁMICOS (1 ESPECIE) ====================

    private void setSemaforosDinamicos() throws Exception {
        Especie especie = obtenerEspecieEstanque();
        
        // Obtener parámetros de la especie (si existe)
        Parametro paramTemp = obtenerParametroEspecie("Temperatura", especie);
        Parametro paramPh = obtenerParametroEspecie("pH", especie);
        Parametro paramOx = obtenerParametroEspecie("Oxígeno", especie);
        Parametro paramNh3 = obtenerParametroEspecie("Amoniaco", especie);

        double t = estanque.getTemperaturaAgua();
        double ph = estanque.getPhAgua();
        double ox = estanque.getOxigeno();
        double nh3 = estanque.getAmoniaco();

        // Configurar semáforos
        configurarSemaforo("Temperatura", t, paramTemp, semTemp, icoTemp, dotTemp);
        configurarSemaforo("pH", ph, paramPh, semPh, icoPh, dotPh);
        configurarSemaforo("Oxígeno", ox, paramOx, semOx, icoOx, dotOx);
        configurarSemaforo("Amoniaco", nh3, paramNh3, semNh3, icoNh3, dotNh3);

        // Tooltip informativo
        actualizarTooltipGlobal(paramTemp, paramPh, paramOx, paramNh3, especie);
    }

    private Parametro obtenerParametroEspecie(String nombreParametro, Especie especie) throws Exception {
        if (especie == null || especie.getParametros() == null) {
            return crearParametroPorDefecto(nombreParametro);
        }

        for (Parametro param : especie.getParametros()) {
            if (param.getNombre().equalsIgnoreCase(nombreParametro)) {
                return param;
            }
        }

        return crearParametroPorDefecto(nombreParametro);
    }

    private void configurarSemaforo(String nombre, double valor, Parametro parametro, 
                                   Label label, Label icono, Region punto) {
        String estado = calcularEstado(valor, parametro);
        String texto, color, simbolo;

        switch (estado) {
            case "green":
                texto = "Óptimo";
                color = "#12b886";
                simbolo = "✓";
                break;
            case "yellow":
                texto = "Atención";
                color = "#f59e0b";
                simbolo = "⚠";
                break;
            default: // "red"
                texto = "Crítico";
                color = "#ef4444";
                simbolo = "✗";
                break;
        }

        label.setText(texto);
        icono.setText(simbolo);
        punto.setStyle("-fx-background-color: " + color + ";");

        // Tooltip detallado
        String tooltip = String.format("%s: %.2f\nRango ideal: %.1f - %.1f %s\nEstado: %s", 
            nombre, valor, parametro.getRangoMin(), parametro.getRangoMax(), 
            parametro.getUnidad(), texto);
        Tooltip.install(label, new Tooltip(tooltip));
    }

    private String calcularEstado(double valor, Parametro parametro) {
        if (Double.isNaN(valor)) return "red";
        
        float min = parametro.getRangoMin();
        float max = parametro.getRangoMax();
        float tolerancia = (max - min) * 0.15f;

        if (valor >= min && valor <= max) return "green";
        if (valor >= (min - tolerancia) && valor <= (max + tolerancia)) return "yellow";
        return "red";
    }

    private void actualizarTooltipGlobal(Parametro temp, Parametro ph, Parametro ox, Parametro nh3, Especie especie) {
        String especieNombre = (especie != null) ? especie.getNombreComun() : "No asignada";
        
        String tooltipText = String.format(
            "Especie: %s\nParámetros utilizados:\n\n" +
            "🌡️ Temperatura: %.1f-%.1f %s\n" +
            "💧 pH: %.1f-%.1f %s\n" + 
            "🌀 Oxígeno: ≥%.1f %s\n" +
            "☣️ Amoniaco: ≤%.3f %s",
            especieNombre,
            temp.getRangoMin(), temp.getRangoMax(), temp.getUnidad(),
            ph.getRangoMin(), ph.getRangoMax(), ph.getUnidad(),
            ox.getRangoMin(), ox.getUnidad(),
            nh3.getRangoMax(), nh3.getUnidad()
        );
        
        Tooltip.install(lblTitulo, new Tooltip(tooltipText));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void actualizarGraficoBarras(double t, double ph, double ox, double nh3) {
        barActuales.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valores Actuales");
        
        series.getData().add(new XYChart.Data<>("Temp (°C)", t));
        series.getData().add(new XYChart.Data<>("pH", ph));
        series.getData().add(new XYChart.Data<>("O₂ (mg/L)", ox));
        series.getData().add(new XYChart.Data<>("NH₃ (mg/L)", nh3));
        
        barActuales.getData().add(series);
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

    private Parametro crearParametroPorDefecto(String nombre) {
        return switch (nombre.toLowerCase()) {
            case "temperatura" -> new Parametro("Temperatura", "°C", 24.0f, 30.0f);
            case "ph" -> new Parametro("pH", "unidades", 6.5f, 8.0f);
            case "oxígeno", "oxigeno" -> new Parametro("Oxígeno", "mg/L", 5.0f, 8.0f);
            case "amoniaco" -> new Parametro("Amoniaco", "mg/L", 0.0f, 0.05f);
            default -> new Parametro(nombre, "", 0.0f, 100.0f);
        };
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "" : s.trim();
    }
    
    private static String fmt(double v) {
        if (Double.isNaN(v)) return "-";
        String s = String.format("%.2f", v);
        return s.endsWith(".00") ? s.substring(0, s.length() - 3) : s;
    }
}