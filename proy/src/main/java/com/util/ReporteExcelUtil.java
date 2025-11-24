package com.util;

import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Estanque;
import com.mycompany.model.Lote;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;

import javax.swing.*;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ReporteExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Colores profesionales
    private static final IndexedColors COLOR_CABECERA_PRINCIPAL = IndexedColors.DARK_BLUE;
    private static final IndexedColors COLOR_CABECERA_SECUNDARIA = IndexedColors.LIGHT_BLUE;

    public static void generarReporteFincaEstanqueLote(
            GranjaPiscicola finca,
            Estanque estanque,
            Lote lote,
            Connection conn
    ) throws Exception {

        // Elegir ubicación del archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("Reporte_Produccion_" + System.currentTimeMillis() + ".xlsx"));
        int option = fileChooser.showSaveDialog(null);
        if (option != JFileChooser.APPROVE_OPTION) return;
        String nombreArchivo = fileChooser.getSelectedFile().getAbsolutePath();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Producción");

            // Configurar anchos de columnas
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 8000);

            // Estilos predefinidos
            CellStyle estiloTituloPrincipal = crearEstiloTituloPrincipal(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloCabeceraTabla = crearEstiloCabeceraTabla(workbook);
            CellStyle estiloCelda = crearEstiloCelda(workbook);
            CellStyle estiloValor = crearEstiloValor(workbook);
            CellStyle estiloEncabezadoSeccion = crearEstiloEncabezadoSeccion(workbook);

            int rowNum = 0;

            // === ENCABEZADO PRINCIPAL ===
            Row tituloRow = sheet.createRow(rowNum++);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE PRODUCCIÓN PISCÍCOLA");
            tituloCell.setCellStyle(estiloTituloPrincipal);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            rowNum++; // Espacio

            // === INFORMACIÓN GENERAL ===
            Row infoHeader = sheet.createRow(rowNum++);
            Cell infoCell = infoHeader.createCell(0);
            infoCell.setCellValue("INFORMACIÓN GENERAL");
            infoCell.setCellStyle(estiloEncabezadoSeccion);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            // Fecha de generación
            Row fechaRow = sheet.createRow(rowNum++);
            fechaRow.createCell(0).setCellValue("Generado el:");
            fechaRow.createCell(1).setCellValue(LocalDateTime.now().format(DATE_FORMATTER));
            fechaRow.getCell(0).setCellStyle(estiloCelda);
            fechaRow.getCell(1).setCellStyle(estiloValor);

            rowNum++; // Espacio

            // === DATOS DE LA FINCA ===
            crearSeccionConTitulo(sheet, rowNum++, "DATOS DE LA FINCA", estiloSubtitulo, 0, 3);

            Row fincaRow1 = sheet.createRow(rowNum++);
            fincaRow1.createCell(0).setCellValue("Nombre:");
            fincaRow1.createCell(1).setCellValue(finca.getNombre());
            fincaRow1.createCell(2).setCellValue("Ubicación:");
            fincaRow1.createCell(3).setCellValue(finca.getUbicacion());
            aplicarEstilosFila(fincaRow1, estiloCelda, estiloValor);

            rowNum++; // Espacio

            // === DATOS DEL ESTANQUE ===
            crearSeccionConTitulo(sheet, rowNum++, "DATOS DEL ESTANQUE", estiloSubtitulo, 0, 3);

            Row estanqueRow1 = sheet.createRow(rowNum++);
            estanqueRow1.createCell(0).setCellValue("ID Estanque:");
            estanqueRow1.createCell(1).setCellValue(estanque.getIdEstanque());
            estanqueRow1.createCell(2).setCellValue("Tipo:");
            estanqueRow1.createCell(3).setCellValue(estanque.getTipo());
            aplicarEstilosFila(estanqueRow1, estiloCelda, estiloValor);

            Row estanqueRow2 = sheet.createRow(rowNum++);
            estanqueRow2.createCell(0).setCellValue("Estado:");
            estanqueRow2.createCell(1).setCellValue(estanque.getEstado());
            estanqueRow2.createCell(2).setCellValue("Capacidad:");
            estanqueRow2.createCell(3).setCellValue(estanque.getCapacidad() + " m³");
            aplicarEstilosFila(estanqueRow2, estiloCelda, estiloValor);

            rowNum++; // Espacio

            // === DATOS DEL LOTE ===
            crearSeccionConTitulo(sheet, rowNum++, "DATOS DEL LOTE", estiloSubtitulo, 0, 3);

            Row loteRow1 = sheet.createRow(rowNum++);
            loteRow1.createCell(0).setCellValue("ID Lote:");
            loteRow1.createCell(1).setCellValue(lote.getIdLote());
            loteRow1.createCell(2).setCellValue("pH Actual:");
            loteRow1.createCell(3).setCellValue(lote.getPhActual() != null ?
                    String.format("%.2f", lote.getPhActual()) : "N/A");
            aplicarEstilosFila(loteRow1, estiloCelda, estiloValor);

            Row loteRow2 = sheet.createRow(rowNum++);
            loteRow2.createCell(0).setCellValue("Descripción:");
            loteRow2.createCell(1).setCellValue(lote.getDescripcion());
            loteRow2.createCell(2).setCellValue("Especie:");
            loteRow2.createCell(3).setCellValue(estanque.buscarEspecie(lote.getIdEspecie()));
            aplicarEstilosFila(loteRow2, estiloCelda, estiloValor);

            rowNum += 2; // Doble espacio

            // === HISTÓRICO DE pH ===
            List<Object[]> historico = obtenerHistoricoPH(conn, lote.getIdLote());

            if (!historico.isEmpty()) {
                crearSeccionConTitulo(sheet, rowNum++, "HISTÓRICO DE pH", estiloSubtitulo, 0, 3);

                // Cabecera de la tabla
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"Fecha y Hora", "Valor pH", "Estado", "Recomendación"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(estiloCabeceraTabla);
                }

                int dataStartRow = rowNum;

                // Datos del histórico
                for (Object[] registro : historico) {
                    Row dataRow = sheet.createRow(rowNum++);
                    String fecha = ((LocalDateTime) registro[0]).format(DATE_FORMATTER);
                    Double ph = (Double) registro[1];
                    String estado = obtenerEstadoPH(ph);
                    String recomendacion = obtenerRecomendacionPH(ph);

                    dataRow.createCell(0).setCellValue(fecha);
                    dataRow.createCell(1).setCellValue(ph);
                    dataRow.createCell(2).setCellValue(estado);
                    dataRow.createCell(3).setCellValue(recomendacion);

                    // Estilos
                    CellStyle estiloPH = crearEstiloPH(workbook, ph);
                    dataRow.getCell(1).setCellStyle(estiloPH);
                    dataRow.getCell(0).setCellStyle(estiloCelda);
                    dataRow.getCell(2).setCellStyle(estiloCelda);
                    dataRow.getCell(3).setCellStyle(estiloCelda);
                }

                int dataEndRow = rowNum - 1;

                rowNum += 2; // Espacio para el gráfico
                int chartTopRow = rowNum;

                // === GRÁFICO DE pH ===
                crearGraficoPH(sheet, dataStartRow, dataEndRow, chartTopRow);
                rowNum = chartTopRow + 20;
            } else {
                crearSeccionConTitulo(sheet, rowNum++, "HISTÓRICO DE pH", estiloSubtitulo, 0, 3);
                Row noDataRow = sheet.createRow(rowNum++);
                noDataRow.createCell(0).setCellValue("No hay registros históricos de pH disponibles");
                noDataRow.getCell(0).setCellStyle(estiloCelda);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));
            }

            // === PIE DE PÁGINA ===
            rowNum += 2;
            Row footerRow = sheet.createRow(rowNum++);
            footerRow.createCell(0).setCellValue("Reporte generado automáticamente por Sistema Piscícola");
            footerRow.getCell(0).setCellStyle(crearEstiloPiePagina(workbook));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            // Auto-size
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(nombreArchivo)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(null,
                    "✅ Reporte Excel generado exitosamente!\n\n" +
                            "Archivo: " + nombreArchivo + "\n" +
                            "Secciones incluidas:\n" +
                            "• Información general\n" +
                            "• Datos de finca, estanque y lote\n" +
                            "• Histórico de pH con análisis\n" +
                            "• Gráfico de tendencias",
                    "Reporte Completado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error al generar el reporte: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // ========== ESTILOS ==========

    private static CellStyle crearEstiloTituloPrincipal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(COLOR_CABECERA_PRINCIPAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }

    private static CellStyle crearEstiloSubtitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(COLOR_CABECERA_SECUNDARIA.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloCabeceraTabla(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloCelda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloValor(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle crearEstiloEncabezadoSeccion(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        return style;
    }

    private static CellStyle crearEstiloPH(Workbook workbook, double ph) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);

        if (ph < 6.5 || ph > 8.5) {
            font.setColor(IndexedColors.RED.getIndex());
            style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        } else if (ph < 7.0 || ph > 8.0) {
            font.setColor(IndexedColors.ORANGE.getIndex());
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        } else {
            font.setColor(IndexedColors.DARK_GREEN.getIndex());
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        }

        style.setFont(font);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloPiePagina(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    // ========== FUNCIONALES ==========

    private static void crearSeccionConTitulo(Sheet sheet, int rowNum, String titulo, CellStyle estilo, int startCol, int endCol) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(startCol);
        cell.setCellValue(titulo);
        cell.setCellStyle(estilo);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startCol, endCol));
    }

    private static void aplicarEstilosFila(Row row, CellStyle estiloEtiqueta, CellStyle estiloValor) {
        for (int i = 0; i < 4; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                cell.setCellStyle(i % 2 == 0 ? estiloEtiqueta : estiloValor);
            }
        }
    }

    private static List<Object[]> obtenerHistoricoPH(Connection conn, int idLote) throws Exception {
        List<Object[]> historico = new ArrayList<>();
        String sql = "SELECT medido_at, ph FROM lotes_ph_historial WHERE id_lote = ? ORDER BY medido_at DESC LIMIT 20";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime fecha = rs.getTimestamp("medido_at").toLocalDateTime();
                    Double ph = rs.getDouble("ph");
                    historico.add(0, new Object[]{fecha, ph}); // en orden cronológico
                }
            }
        }
        return historico;
    }

    private static String obtenerEstadoPH(double ph) {
        if (ph >= 7.0 && ph <= 8.0) return "ÓPTIMO";
        else if (ph >= 6.5 && ph < 7.0) return "LIGERAMENTE ÁCIDO";
        else if (ph > 8.0 && ph <= 8.5) return "LIGERAMENTE ALCALINO";
        else if (ph < 6.5) return "DEMASIADO ÁCIDO";
        else return "DEMASIADO ALCALINO";
    }

    private static String obtenerRecomendacionPH(double ph) {
        if (ph >= 7.0 && ph <= 8.0) return "Mantener condiciones actuales";
        else if (ph < 7.0) return "Considerar ajuste con carbonatos";
        else return "Considerar ajuste con acidificantes";
    }

    // Gráfico usando rangos de celdas (más robusto)
    private static void crearGraficoPH(Sheet sheet, int dataStartRow, int dataEndRow, int chartTopRow) {
        try {
            if (dataEndRow < dataStartRow) return;

            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, chartTopRow, 8, chartTopRow + 20);

            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Tendencia del pH - Evolución Temporal");
            chart.setTitleOverlay(false);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Fechas de Medición");

            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Valor de pH");

            // Rango de categorías (fechas) y valores (pH)
            XDDFDataSource<String> fechasData = XDDFDataSourcesFactory.fromStringCellRange((XSSFSheet) sheet,
                    new CellRangeAddress(dataStartRow, dataEndRow, 0, 0)
            );
            XDDFNumericalDataSource<Double> phData = XDDFDataSourcesFactory.fromNumericCellRange((XSSFSheet) sheet,
                    new CellRangeAddress(dataStartRow, dataEndRow, 1, 1)
            );

            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
            XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(fechasData, phData);
            series.setTitle("pH", null);
            series.setSmooth(false);
            series.setMarkerStyle(MarkerStyle.CIRCLE);

            chart.plot(data);

        } catch (Exception e) {
            e.printStackTrace();
            Row errorRow = sheet.createRow(chartTopRow);
            errorRow.createCell(0).setCellValue("Gráfico no disponible (error al generarlo)");
        }
    }
}
