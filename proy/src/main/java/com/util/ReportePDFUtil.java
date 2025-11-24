package com.util;

import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Estanque;
import com.mycompany.model.Lote;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportePDFUtil {

    // Colores corporativos
    private static final DeviceRgb COLOR_PRIMARIO   = new DeviceRgb(0, 82, 155);   // Azul corporativo
    private static final DeviceRgb COLOR_SECUNDARIO = new DeviceRgb(240, 245, 249); // Gris azulado claro
    private static final DeviceRgb COLOR_ACENTO     = new DeviceRgb(76, 175, 80);   // Verde éxito
    private static final DeviceRgb COLOR_ALERTA     = new DeviceRgb(255, 152, 0);   // Naranja alerta
    private static final DeviceRgb COLOR_PELIGRO    = new DeviceRgb(244, 67, 54);   // Rojo peligro

    private static final DateTimeFormatter FMT_FECHA   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_GRAFICO = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    public static void generarReporteFincaEstanqueLote(
            GranjaPiscicola finca,
            Estanque estanque,
            Lote lote,
            Connection conn
    ) throws Exception {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(
                "Reporte_Produccion_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf"
        ));
        int option = fileChooser.showSaveDialog(null);
        if (option != JFileChooser.APPROVE_OPTION) return;

        String nombreArchivo = fileChooser.getSelectedFile().getAbsolutePath();

        PdfWriter writer = new PdfWriter(nombreArchivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 60, 40);

        PdfFont fontTitulo    = PdfFontFactory.createFont();
        PdfFont fontSubtitulo = PdfFontFactory.createFont();
        PdfFont fontNormal    = PdfFontFactory.createFont();

        // ========== ENCABEZADO ==========
        agregarEncabezado(doc, fontTitulo);

        // ========== INFORMACIÓN GENERAL ==========
        agregarSeccionInformacionGeneral(doc, fontSubtitulo, fontNormal, finca, estanque, lote);

        // ========== DATOS TÉCNICOS ==========
        agregarSeccionDatosDetallados(doc, fontSubtitulo, fontNormal, finca, estanque, lote);

        // ========== HISTÓRICO DE pH ==========
        List<Object[]> historico = obtenerHistoricoPH(conn, lote.getIdLote());
        if (!historico.isEmpty()) {
            agregarSeccionHistoricoPH(doc, fontSubtitulo, fontNormal, historico);
            agregarGraficoPH(pdf, doc, historico, lote);
        }

        // ========== ANÁLISIS ==========
        agregarSeccionAnalisis(doc, fontSubtitulo, fontNormal, lote, historico);

        // ========== PIE ==========
        agregarPiePagina(doc, fontNormal);

        doc.close();

        JOptionPane.showMessageDialog(null,
                "✅ Reporte PDF generado exitosamente!\n\n" +
                        "Archivo: " + nombreArchivo + "\n" +
                        "Características:\n" +
                        "• Diseño profesional corporativo\n" +
                        "• Gráficos de tendencia\n" +
                        "• Análisis detallado\n" +
                        "• Recomendaciones técnicas",
                "Reporte Completado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------------------------------------
    // ENCABEZADO
    // -------------------------------------------------------
    private static void agregarEncabezado(Document doc, PdfFont fontTitulo) {
        Div headerDiv = new Div()
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(20)
                .setMarginBottom(20)
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph titulo = new Paragraph("REPORTE DE PRODUCCIÓN PISCÍCOLA")
                .setFont(fontTitulo)
                .setFontSize(18)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(5);

        Paragraph subtitulo = new Paragraph("Sistema de Monitoreo de Calidad de Agua")
                .setFontSize(12)
                .setFontColor(new DeviceRgb(200, 230, 255))
                .setMarginBottom(5);

        Paragraph fecha = new Paragraph("Generado: " + LocalDateTime.now().format(FMT_FECHA))
                .setFontSize(10)
                .setFontColor(new DeviceRgb(180, 220, 255));

        headerDiv.add(titulo);
        headerDiv.add(subtitulo);
        headerDiv.add(fecha);
        doc.add(headerDiv);
    }

    // -------------------------------------------------------
    // INFORMACIÓN GENERAL
    // -------------------------------------------------------
    private static void agregarSeccionInformacionGeneral(Document doc, PdfFont fontSubtitulo, PdfFont fontNormal,
                                                         GranjaPiscicola finca, Estanque estanque, Lote lote) {

        Paragraph seccionTitulo = new Paragraph("INFORMACIÓN GENERAL DEL SISTEMA")
                .setFont(fontSubtitulo)
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARIO)
                .setMarginBottom(15)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 1))
                .setPaddingBottom(5);

        doc.add(seccionTitulo);

        float[] columnWidths = {120f, 200f, 120f, 200f};
        Table table = new Table(columnWidths);
        table.setMarginBottom(20);

        agregarCeldaEncabezado(table, "Granja Piscícola:");
        agregarCeldaDato(table, finca.getNombre());
        agregarCeldaEncabezado(table, "Ubicación:");
        agregarCeldaDato(table, finca.getUbicacion());

        agregarCeldaEncabezado(table, "Estanque:");
        agregarCeldaDato(table, "ID " + estanque.getIdEstanque() + " - " + estanque.getTipo());
        agregarCeldaEncabezado(table, "Capacidad:");
        agregarCeldaDato(table, estanque.getCapacidad() + " m³");

        agregarCeldaEncabezado(table, "Lote:");
        agregarCeldaDato(table, "ID " + lote.getIdLote());
        agregarCeldaEncabezado(table, "Estado:");
        agregarCeldaDato(table, estanque.getEstado());

        agregarCeldaEncabezado(table, "Descripción:");
        Cell descCell = new Cell(1, 3).add(new Paragraph(lote.getDescripcion()));
        descCell.setPadding(5);
        descCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(descCell);

        doc.add(table);
    }

    // -------------------------------------------------------
    // DATOS TÉCNICOS
    // -------------------------------------------------------
    private static void agregarSeccionDatosDetallados(Document doc, PdfFont fontSubtitulo, PdfFont fontNormal,
                                                      GranjaPiscicola finca, Estanque estanque, Lote lote) {

        Paragraph seccionTitulo = new Paragraph("DATOS TÉCNICOS DEL LOTE")
                .setFont(fontSubtitulo)
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARIO)
                .setMarginBottom(15)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 1))
                .setPaddingBottom(5);

        doc.add(seccionTitulo);

        float[] columnWidths = {150f, 200f, 150f, 200f};
        Table table = new Table(columnWidths);
        table.setMarginBottom(25);

        double phActual = lote.getPhActual() != null ? lote.getPhActual() : 0;
        DeviceRgb colorPH = obtenerColorPH(phActual);
        String estadoPH = evaluarEstadoPH(phActual);

        agregarCeldaEncabezado(table, "pH Actual:");
        Cell phCell = new Cell().add(new Paragraph(String.format("%.2f", phActual)))
                .setFontColor(colorPH)
                .setBold()
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(phCell);

        agregarCeldaEncabezado(table, "Estado pH:");
        Cell estadoCell = new Cell().add(new Paragraph(estadoPH))
                .setFontColor(colorPH)
                .setBold()
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(estadoCell);

        agregarCeldaEncabezado(table, "Especie:");
        agregarCeldaDato(table, estanque.buscarEspecie(lote.getIdEspecie()));

        agregarCeldaEncabezado(table, "Tipo de Estanque:");
        agregarCeldaDato(table, estanque.getTipo());

        doc.add(table);
    }

    // -------------------------------------------------------
    // HISTÓRICO (TABLA)
    // -------------------------------------------------------
    private static void agregarSeccionHistoricoPH(Document doc, PdfFont fontSubtitulo, PdfFont fontNormal,
                                                  List<Object[]> historico) {

        Paragraph seccionTitulo = new Paragraph("HISTÓRICO DE MEDICIONES DE pH")
                .setFont(fontSubtitulo)
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARIO)
                .setMarginBottom(15)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 1))
                .setPaddingBottom(5);

        doc.add(seccionTitulo);

        Table table = new Table(new float[]{150f, 80f, 100f});
        table.setMarginBottom(20);

        agregarCeldaEncabezadoTabla(table, "Fecha y Hora");
        agregarCeldaEncabezadoTabla(table, "Valor pH");
        agregarCeldaEncabezadoTabla(table, "Estado");

        for (Object[] registro : historico) {
            LocalDateTime fecha = (LocalDateTime) registro[0];
            Double ph = (Double) registro[1];
            String estado = evaluarEstadoPH(ph);
            DeviceRgb color = obtenerColorPH(ph);

            Cell fechaCell = new Cell().add(new Paragraph(fecha.format(FMT_FECHA)))
                    .setPadding(5)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            table.addCell(fechaCell);

            Cell phCell = new Cell().add(new Paragraph(String.format("%.2f", ph)))
                    .setFontColor(color)
                    .setBold()
                    .setPadding(5)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            table.addCell(phCell);

            Cell estadoCell = new Cell().add(new Paragraph(estado))
                    .setFontColor(color)
                    .setPadding(5)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            table.addCell(estadoCell);
        }

        doc.add(table);
    }

    // -------------------------------------------------------
    // GRÁFICO DINÁMICO
    // -------------------------------------------------------
    private static void agregarGraficoPH(PdfDocument pdf, Document doc, List<Object[]> historico, Lote lote) {
        if (historico == null || historico.isEmpty()) return;

        try {
            // 1. Calcular rango dinámico de pH
            double minData = historico.stream().mapToDouble(r -> (Double) r[1]).min().orElse(7.0);
            double maxData = historico.stream().mapToDouble(r -> (Double) r[1]).max().orElse(7.0);

            double padding = 0.5;
            double phMin = Math.max(0.0, minData - padding);
            double phMax = Math.min(14.0, maxData + padding);

            if (phMax <= phMin) { // caso extremo
                phMin = Math.max(0.0, phMin - 1.0);
                phMax = Math.min(14.0, phMax + 1.0);
            }

            PdfPage page = pdf.addNewPage();
            PdfCanvas canvas = new PdfCanvas(page);
            PdfFont font = PdfFontFactory.createFont();

            // Título centrado
            Paragraph titulo = new Paragraph("EVOLUCIÓN DEL pH - Lote " + lote.getIdLote())
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(COLOR_PRIMARIO);

            doc.showTextAligned(
                    titulo,
                    page.getPageSize().getWidth() / 2,
                    page.getPageSize().getHeight() - 40,
                    pdf.getNumberOfPages(),
                    TextAlignment.CENTER,
                    VerticalAlignment.MIDDLE,
                    0
            );

            float marginLeft   = 60;
            float marginBottom = 100;
            float width        = page.getPageSize().getWidth() - 100;
            float height       = 260;

            int n = historico.size();
            float stepX = (n > 1) ? width / (n - 1) : 0;

            // Función de mapeo pH -> coordenada Y
            final double phm = phMin;
            final double phmx = phMax;
            java.util.function.DoubleFunction<Float> mapPH = (ph) ->
                    marginBottom + (float) ((ph - phm) / (phmx - phm) * height);

          
            // Fondo general
            canvas.setFillColor(COLOR_SECUNDARIO);
            canvas.rectangle(marginLeft, marginBottom, width, height);
            canvas.fill();

            // Banda óptima 6.5-8.5 solo si cae dentro del rango
            double bandLow  = Math.max(6.5, phMin);
            double bandHigh = Math.min(8.5, phMax);
            if (bandHigh > bandLow) {
                float yOptMin = mapPH.apply(bandLow);
                float yOptMax = mapPH.apply(bandHigh);
                canvas.setFillColor(new DeviceRgb(220, 245, 220));
                canvas.rectangle(marginLeft, yOptMin, width, yOptMax - yOptMin);
                canvas.fill();
            }

            // Ejes
            canvas.setStrokeColor(ColorConstants.BLACK);
            canvas.setLineWidth(1f);
            // eje Y
            canvas.moveTo(marginLeft, marginBottom);
            canvas.lineTo(marginLeft, marginBottom + height);
            // eje X
            canvas.moveTo(marginLeft, marginBottom);
            canvas.lineTo(marginLeft + width, marginBottom);
            canvas.stroke();

            // Etiquetas y líneas horizontales de referencia (grid Y)
            int ticks = 5;
            canvas.beginText();
            canvas.setFontAndSize(font, 9);
            canvas.setFillColor(ColorConstants.BLACK);
            for (int i = 0; i <= ticks; i++) {
                double val = phMin + i * (phMax - phMin) / ticks;
                float y = mapPH.apply(val);

                // línea de grid
                canvas.endText();
                canvas.setLineWidth(0.3f);
                canvas.setStrokeColor(new DeviceRgb(210, 210, 210));
                canvas.moveTo(marginLeft, y);
                canvas.lineTo(marginLeft + width, y);
                canvas.stroke();

                canvas.beginText();
                canvas.setFontAndSize(font, 9);
                canvas.setFillColor(ColorConstants.BLACK);
                canvas.setTextMatrix(marginLeft - 30, y - 3);
                canvas.showText(String.format(java.util.Locale.US, "%.1f", val));
            }
            canvas.endText();

            // Etiquetas eje X (fechas)
            canvas.beginText();
            canvas.setFontAndSize(font, 8);
            canvas.setFillColor(ColorConstants.BLACK);

            int maxEtiquetas = 6;
            int salto = Math.max(1, n / maxEtiquetas);

            for (int i = 0; i < n; i += salto) {
                LocalDateTime fecha = (LocalDateTime) historico.get(i)[0];
                String label = fecha.format(FMT_GRAFICO);

                float x = (n > 1)
                        ? marginLeft + i * stepX
                        : marginLeft + width / 2;

                canvas.setTextMatrix(x - 20, marginBottom - 15);
                canvas.showText(label);
            }
            canvas.endText();

            // Títulos de ejes
            canvas.beginText();
            canvas.setFontAndSize(font, 10);
            canvas.setTextMatrix(marginLeft - 40, marginBottom + height + 5);
            canvas.showText("pH");
            canvas.endText();

            canvas.beginText();
            canvas.setFontAndSize(font, 10);
            canvas.setTextMatrix(marginLeft + width / 2 - 45, marginBottom - 30);
            canvas.showText("Fecha de medición");
            canvas.endText();

            // Línea de tendencia y puntos
            canvas.setLineWidth(2f);
            canvas.setStrokeColor(COLOR_PRIMARIO);

            for (int i = 0; i < n; i++) {
                Double ph = (Double) historico.get(i)[1];
                float x = (n > 1)
                        ? marginLeft + i * stepX
                        : marginLeft + width / 2;
                float y = mapPH.apply(ph);

                if (i > 0 && n > 1) {
                    Double phPrev = (Double) historico.get(i - 1)[1];
                    float xPrev = marginLeft + (i - 1) * stepX;
                    float yPrev = mapPH.apply(phPrev);
                    canvas.moveTo(xPrev, yPrev);
                    canvas.lineTo(x, y);
                    canvas.stroke();
                }

                DeviceRgb puntoColor = obtenerColorPH(ph);
                canvas.setFillColor(puntoColor);
                canvas.setStrokeColor(puntoColor);
                canvas.circle(x, y, 3);
                canvas.fillStroke();

                canvas.setStrokeColor(COLOR_PRIMARIO);
            }

            // Leyenda
            agregarLeyendaGrafico(canvas, marginLeft + width - 160, marginBottom + height - 20);

        } catch (Exception e) {
            System.err.println("Error al crear gráfico PDF: " + e.getMessage());
        }
    }

    private static void agregarLeyendaGrafico(PdfCanvas canvas, float x, float y) {
        String[][] leyenda = {
                {"Óptimo (6.5-8.5)", "0,150,0"},
                {"Alerta (6.0-6.5 / 8.5-9.0)", "255,152,0"},
                {"Peligro (<6.0 / >9.0)", "244,67,54"}
        };

        try {
            PdfFont font = PdfFontFactory.createFont();
            canvas.setFontAndSize(font, 8);

            for (int i = 0; i < leyenda.length; i++) {
                String[] rgb = leyenda[i][1].split(",");
                DeviceRgb color = new DeviceRgb(
                        Integer.parseInt(rgb[0]),
                        Integer.parseInt(rgb[1]),
                        Integer.parseInt(rgb[2])
                );

                canvas.setFillColor(color);
                canvas.circle(x, y - i * 15, 3);
                canvas.fill();

                canvas.beginText();
                canvas.setFillColor(ColorConstants.BLACK);
                canvas.setTextMatrix(x + 8, y - i * 15 - 3);
                canvas.showText(leyenda[i][0]);
                canvas.endText();
            }
        } catch (IOException e) {
            System.err.println("Error en leyenda del gráfico: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // ANÁLISIS Y ESTADÍSTICAS
    // -------------------------------------------------------
    private static void agregarSeccionAnalisis(Document doc, PdfFont fontSubtitulo, PdfFont fontNormal,
                                               Lote lote, List<Object[]> historico) {

        Paragraph seccionTitulo = new Paragraph("ANÁLISIS Y RECOMENDACIONES")
                .setFont(fontSubtitulo)
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARIO)
                .setMarginBottom(15)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARIO, 1))
                .setPaddingBottom(5);

        doc.add(seccionTitulo);

        double phActual = lote.getPhActual() != null ? lote.getPhActual() : 0;
        String recomendacion = obtenerRecomendacionPH(phActual);

        Div recomendacionDiv = new Div()
                .setBackgroundColor(COLOR_SECUNDARIO)
                .setPadding(15)
                .setMarginBottom(20)
                .setBorder(new SolidBorder(COLOR_PRIMARIO, 1));

        Paragraph tituloRecomendacion = new Paragraph("RECOMENDACIÓN TÉCNICA")
                .setFont(fontSubtitulo)
                .setFontSize(12)
                .setFontColor(COLOR_PRIMARIO)
                .setMarginBottom(10);

        Paragraph textoRecomendacion = new Paragraph(recomendacion)
                .setFont(fontNormal)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.JUSTIFIED);

        recomendacionDiv.add(tituloRecomendacion);
        recomendacionDiv.add(textoRecomendacion);
        doc.add(recomendacionDiv);

        if (!historico.isEmpty()) {
            agregarEstadisticasPH(doc, fontNormal, historico);
        }
    }

    private static void agregarEstadisticasPH(Document doc, PdfFont fontNormal, List<Object[]> historico) {
        Table statsTable = new Table(new float[]{150f, 100f});
        statsTable.setMarginBottom(20);

        double phMin = historico.stream().mapToDouble(r -> (Double) r[1]).min().orElse(0);
        double phMax = historico.stream().mapToDouble(r -> (Double) r[1]).max().orElse(0);
        double phPromedio = historico.stream().mapToDouble(r -> (Double) r[1]).average().orElse(0);

        agregarCeldaEncabezado(statsTable, "Estadísticas");
        agregarCeldaEncabezado(statsTable, "Valor");

        agregarFilaEstadistica(statsTable, "Mínimo registrado", String.format("%.2f", phMin));
        agregarFilaEstadistica(statsTable, "Máximo registrado", String.format("%.2f", phMax));
        agregarFilaEstadistica(statsTable, "Promedio", String.format("%.2f", phPromedio));
        agregarFilaEstadistica(statsTable, "Total mediciones", String.valueOf(historico.size()));

        doc.add(statsTable);
    }

    private static void agregarPiePagina(Document doc, PdfFont fontNormal) {
        Div footerDiv = new Div()
                .setPadding(10)
                .setBorderTop(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph footer = new Paragraph("Sistema de Monitoreo Piscícola • Reporte generado automáticamente")
                .setFont(fontNormal)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY);

        footerDiv.add(footer);
        doc.add(footerDiv);
    }

    // -------------------------------------------------------
    // AUXILIARES GENERALES
    // -------------------------------------------------------
    private static List<Object[]> obtenerHistoricoPH(Connection conn, int idLote) throws Exception {
        List<Object[]> historico = new ArrayList<>();
        String sql = "SELECT medido_at, ph FROM lotes_ph_historial WHERE id_lote = ? ORDER BY medido_at DESC LIMIT 20";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime fecha = rs.getTimestamp("medido_at").toLocalDateTime();
                    Double ph = rs.getDouble("ph");
                    historico.add(0, new Object[]{fecha, ph}); // orden cronológico
                }
            }
        }
        return historico;
    }

    private static void agregarCeldaEncabezado(Table table, String texto) {
        Cell cell = new Cell().add(new Paragraph(texto))
                .setFontColor(COLOR_PRIMARIO)
                .setBold()
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(cell);
    }

    private static void agregarCeldaEncabezadoTabla(Table table, String texto) {
        Cell cell = new Cell().add(new Paragraph(texto))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setFontColor(ColorConstants.WHITE)
                .setBold()
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private static void agregarCeldaDato(Table table, String texto) {
        Cell cell = new Cell().add(new Paragraph(texto))
                .setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(cell);
    }

    private static void agregarFilaEstadistica(Table table, String parametro, String valor) {
        table.addCell(new Cell().add(new Paragraph(parametro)).setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
        table.addCell(new Cell().add(new Paragraph(valor)).setPadding(5)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
    }

    private static DeviceRgb obtenerColorPH(double ph) {
        if (ph >= 6.5 && ph <= 8.5) return COLOR_ACENTO;
        else if ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) return COLOR_ALERTA;
        else return COLOR_PELIGRO;
    }

    private static String evaluarEstadoPH(double ph) {
        if (ph >= 6.5 && ph <= 8.5) return "ÓPTIMO";
        else if (ph >= 6.0 && ph < 6.5) return "LIGERAMENTE ÁCIDO";
        else if (ph > 8.5 && ph <= 9.0) return "LIGERAMENTE ALCALINO";
        else if (ph < 6.0) return "MUY ÁCIDO";
        else return "MUY ALCALINO";
    }

    private static String obtenerRecomendacionPH(double ph) {
        if (ph >= 6.5 && ph <= 8.5) {
            return "Las condiciones de pH son óptimas para el desarrollo de especies piscícolas. " +
                    "Mantener el monitoreo regular y continuar con las prácticas actuales de manejo.";
        } else if (ph < 6.5) {
            return "El agua presenta condiciones ácidas. Se recomienda aplicar carbonato de calcio (CaCO3) " +
                    "o hidróxido de calcio (Ca(OH)2) para elevar el pH gradualmente. Monitorear cada 4-6 horas " +
                    "hasta alcanzar el rango óptimo.";
        } else {
            return "El agua presenta condiciones alcalinas. Considerar el uso de acidificantes naturales " +
                    "o incrementar la ventilación para favorecer la disolución de CO2. Evaluar fuentes de " +
                    "alcalinidad en el sistema.";
        }
    }
}
