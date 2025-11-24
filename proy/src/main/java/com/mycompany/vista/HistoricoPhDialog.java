package com.mycompany.vista;

import com.mycompany.model.Estanque;
import com.mycompany.model.Lote;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Diálogo que muestra el histórico de pH de un lote,
 * leyendo de la tabla lotes_ph_historial.
 */
public class HistoricoPhDialog extends JDialog {

    private final Estanque estanque;
    private final Lote lote;
    private final Connection conn;
    private final boolean loteActivo;

    private final List<MedicionPh> mediciones = new ArrayList<>();
    private JLabel lblResumen;
    private JTable tblDatos;
    private GraficaPanel graficaPanel;

    // estadísticas
    private int totalMediciones = 0;
    private double minPh = Double.NaN;
    private double maxPh = Double.NaN;
    private double avgPh = Double.NaN;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());

    /**
     * @param loteActivo true si el lote sigue activo; false si es histórico de un lote inactivo
     */
    public HistoricoPhDialog(Frame owner,
                             Estanque estanque,
                             Lote lote,
                             Connection conn,
                             boolean loteActivo) {
        super(owner,
                "Histórico de pH - Lote #" + lote.getIdLote(),
                true);
        this.estanque = estanque;
        this.lote = lote;
        this.conn = conn;
        this.loteActivo = loteActivo;

        cargarDesdeBD();
        initUI();
        pack();
        setMinimumSize(new Dimension(900, 520));
        setLocationRelativeTo(owner);
    }

    // ===================== UI =====================

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.setBackground(new Color(0xE3F2FD)); // fondo celeste suave
        setContentPane(content);

        // --------- Cabecera ---------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblEstadoLote = new JLabel(
                loteActivo
                        ? "Lote ACTIVO – se siguen registrando mediciones de pH."
                        : "Lote INACTIVO – solo histórico disponible."
        );
        lblEstadoLote.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstadoLote.setForeground(
                loteActivo ? new Color(0x2E7D32) : new Color(0xC62828)
        );

        lblResumen = new JLabel(buildResumen());
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumen.setForeground(new Color(0x37474F));

        header.add(lblEstadoLote);
        header.add(Box.createVerticalStrut(3));
        header.add(lblResumen);
        header.add(Box.createVerticalStrut(8));

        // Píldoras de métricas
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pills.setOpaque(false);

        pills.add(crearPill("Mediciones", String.valueOf(totalMediciones)));
        if (!Double.isNaN(minPh)) {
            pills.add(crearPill("pH mín", String.format(Locale.US, "%.2f", minPh)));
            pills.add(crearPill("pH máx", String.format(Locale.US, "%.2f", maxPh)));
            pills.add(crearPill("pH promedio", String.format(Locale.US, "%.2f", avgPh)));
        }

        header.add(pills);

        content.add(header, BorderLayout.NORTH);

        // --------- Split: gráfica + tabla ---------
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        // Card de la gráfica
        CardPanel cardGraf = new CardPanel();
        cardGraf.setLayout(new BorderLayout());
        JLabel lblGrafTitulo = new JLabel("Gráfica de pH en el tiempo");
        lblGrafTitulo.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        lblGrafTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGrafTitulo.setForeground(new Color(0x01579B));

        graficaPanel = new GraficaPanel(mediciones);
        JScrollPane grafScroll = new JScrollPane(graficaPanel);
        grafScroll.setBorder(null);
        grafScroll.getVerticalScrollBar().setUnitIncrement(16);

        cardGraf.add(lblGrafTitulo, BorderLayout.NORTH);
        cardGraf.add(grafScroll, BorderLayout.CENTER);

        // Card de la tabla
        CardPanel cardTabla = new CardPanel();
        cardTabla.setLayout(new BorderLayout());
        JLabel lblTablaTitulo = new JLabel("Mediciones registradas");
        lblTablaTitulo.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        lblTablaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTablaTitulo.setForeground(new Color(0x01579B));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Fecha / hora", "pH"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (MedicionPh m : mediciones) {
            model.addRow(new Object[]{
                    fmt.format(m.instante),
                    String.format(Locale.US, "%.2f", m.ph)
            });
        }
        tblDatos = new JTable(model);
        tblDatos.setFillsViewportHeight(true);
        tblDatos.setRowHeight(22);
        tblDatos.setShowVerticalLines(false);
        tblDatos.setGridColor(new Color(0xCFD8DC));

        // header de la tabla
        tblDatos.getTableHeader().setReorderingAllowed(false);
        tblDatos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDatos.getTableHeader().setBackground(new Color(0x0288D1));
        tblDatos.getTableHeader().setForeground(Color.WHITE);

        // filas alternadas
        DefaultTableCellRenderer stripeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(0xE1F5FE));
                    }
                }
                return c;
            }
        };
        tblDatos.setDefaultRenderer(Object.class, stripeRenderer);

        JScrollPane spTabla = new JScrollPane(tblDatos);
        spTabla.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        cardTabla.add(lblTablaTitulo, BorderLayout.NORTH);
        cardTabla.add(spTabla, BorderLayout.CENTER);

        split.setTopComponent(cardGraf);
        split.setBottomComponent(cardTabla);

        content.add(split, BorderLayout.CENTER);

        // --------- Botón cerrar ---------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        bottom.add(btnCerrar);
        content.add(bottom, BorderLayout.SOUTH);
    }

    private JPanel crearPill(String titulo, String valor) {
        JPanel pill = new JPanel();
        pill.setOpaque(false);
        pill.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(true);
        inner.setBackground(new Color(0x0288D1));
        inner.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitulo.setForeground(new Color(0xE3F2FD));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValor.setForeground(Color.WHITE);

        inner.add(lblTitulo);
        inner.add(lblValor);

        pill.add(inner, BorderLayout.CENTER);
        pill.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return pill;
    }

    // ===================== BD =====================

    /**
     * Lee el histórico desde la tabla lotes_ph_historial.
     *
     * SUPUESTO de columnas:
     *   - id_lote (int)
     *   - ph (numeric)
     *   - medido_at (timestamp)
     */
    private void cargarDesdeBD() {
        String sql = """
                SELECT ph, medido_at
                FROM lotes_ph_historial
                WHERE id_lote = ?
                ORDER BY medido_at
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lote.getIdLote());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double ph = rs.getBigDecimal("ph").doubleValue();
                    Timestamp ts = rs.getTimestamp("medido_at");
                    Instant inst = ts.toInstant();
                    mediciones.add(new MedicionPh(inst, ph));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error cargando histórico: " + e.getMessage(),
                    "Error BD",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (!mediciones.isEmpty()) {
            totalMediciones = mediciones.size();
            minPh = mediciones.stream().mapToDouble(m -> m.ph).min().orElse(Double.NaN);
            maxPh = mediciones.stream().mapToDouble(m -> m.ph).max().orElse(Double.NaN);
            avgPh = mediciones.stream().mapToDouble(m -> m.ph).average().orElse(Double.NaN);
        }
    }

    private String buildResumen() {
        if (mediciones.isEmpty()) {
            return "No hay mediciones históricas registradas para este lote.";
        }
        return String.format(
                Locale.US,
                "Mediciones: %d   |   pH mín: %.2f   |   pH máx: %.2f   |   pH promedio: %.2f",
                totalMediciones, minPh, maxPh, avgPh
        );
    }

    // ===================== Helpers internos =====================

    private record MedicionPh(Instant instante, double ph) { }

    /**
     * Panel de tarjeta con fondo blanco y bordes redondeados.
     */
    private static class CardPanel extends JPanel {
        CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, 240));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.setColor(new Color(0xB0BEC5));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Panel de la gráfica con fondo degradado y curva de pH.
     */
        /**
     * Panel de la gráfica con fondo degradado y curva de pH.
     */
    private static class GraficaPanel extends JPanel {

        private final List<MedicionPh> datos;
        private final DateTimeFormatter timeFmt =
                DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

        GraficaPanel(List<MedicionPh> datos) {
            this.datos = datos;
            setPreferredSize(new Dimension(800, 260));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (datos == null || datos.isEmpty()) {
                g.setColor(Color.DARK_GRAY);
                g.drawString("No hay datos de histórico para graficar.", 20, 25);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fondo degradado
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0xE1F5FE),
                    0, h, new Color(0xB3E5FC)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            int margenIzq = 55;
            int margenDer = 25;
            int margenSup = 25;
            int margenInf = 45;

            int areaW = w - margenIzq - margenDer;
            int areaH = h - margenSup - margenInf;

            // ====== Estadísticos ======
            double minPh = datos.stream().mapToDouble(m -> m.ph).min().orElse(0);
            double maxPh = datos.stream().mapToDouble(m -> m.ph).max().orElse(14);
            if (maxPh == minPh) {
                maxPh += 0.1;
                minPh -= 0.1;
            }
            long minT = datos.get(0).instante.toEpochMilli();
            long maxT = datos.get(datos.size() - 1).instante.toEpochMilli();
            if (maxT == minT) maxT += 1000;

            // ====== Cuadrícula y ejes ======
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            // Y: 5 divisiones
            g2.setColor(new Color(255, 255, 255, 150));
            int yTicks = 5;
            for (int i = 0; i <= yTicks; i++) {
                double v = minPh + i * (maxPh - minPh) / yTicks;
                int y = margenSup + (int) ((maxPh - v) * areaH / (maxPh - minPh));
                g2.drawLine(margenIzq, y, margenIzq + areaW, y);

                g2.setColor(new Color(0x37474F));
                String txt = String.format(Locale.US, "%.2f", v);
                g2.drawString(txt, 5, y + 4);
                g2.setColor(new Color(255, 255, 255, 150));
            }

            // X: 4 divisiones
            int xTicks = 4;
            for (int i = 0; i <= xTicks; i++) {
                long t = minT + (long) (i * (maxT - minT) / (double) xTicks);
                int x = margenIzq + (int) ((t - minT) * areaW / (double) (maxT - minT));

                // línea vertical suave
                g2.drawLine(x, margenSup, x, margenSup + areaH);

                // etiqueta de tiempo
                g2.setColor(new Color(0x37474F));
                String txt = timeFmt.format(Instant.ofEpochMilli(t));
                int tw = g2.getFontMetrics().stringWidth(txt);
                g2.drawString(txt, x - tw / 2, margenSup + areaH + 15);
                g2.setColor(new Color(255, 255, 255, 150));
            }

            // Ejes
            g2.setColor(new Color(0x90A4AE));
            g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + areaH);
            g2.drawLine(margenIzq, margenSup + areaH, margenIzq + areaW, margenSup + areaH);

            // ====== Curva (con submuestreo) ======
            // Si hay demasiados puntos, solo dibujamos ~400 para que se vea legible
            int n = datos.size();
            int maxPuntos = 400;
            int step = (n <= maxPuntos) ? 1 : (int) Math.ceil(n / (double) maxPuntos);

            // Área bajo la curva
            GeneralPath area = new GeneralPath();
            boolean first = true;
            int lastX = -1, lastY = -1;

            for (int i = 0; i < n; i += step) {
                MedicionPh m = datos.get(i);
                double tNorm = (m.instante.toEpochMilli() - minT) * 1.0 / (maxT - minT);

                int x = margenIzq + (int) (tNorm * areaW);
                int y = margenSup + (int) ((maxPh - m.ph) * areaH / (maxPh - minPh));

                if (first) {
                    area.moveTo(x, margenSup + areaH);
                    area.lineTo(x, y);
                    first = false;
                } else {
                    area.lineTo(x, y);
                }
                lastX = x;
                lastY = y;
            }

            // incluye el último punto real si no cayó justo en el step
            if ((n - 1) % step != 0) {
                MedicionPh last = datos.get(n - 1);
                int x = margenIzq + (int) ((last.instante.toEpochMilli() - minT) * areaW / (double) (maxT - minT));
                int y = margenSup + (int) ((maxPh - last.ph) * areaH / (maxPh - minPh));
                area.lineTo(x, y);
                lastX = x;
                lastY = y;
            }

            if (!first) {
                area.lineTo(lastX, margenSup + areaH);
                area.closePath();
            }

            g2.setColor(new Color(30, 136, 229, 90));
            g2.fill(area);

            // Línea
            g2.setColor(new Color(0x1565C0));
            g2.setStroke(new BasicStroke(1.6f));

            int prevX = -1, prevY = -1;
            for (int i = 0; i < n; i += step) {
                MedicionPh m = datos.get(i);
                int x = margenIzq + (int) ((m.instante.toEpochMilli() - minT) * areaW / (double) (maxT - minT));
                int y = margenSup + (int) ((maxPh - m.ph) * areaH / (maxPh - minPh));

                if (prevX != -1) {
                    g2.drawLine(prevX, prevY, x, y);
                }

                // solo dibujamos puntos si hay pocos datos
                if (n <= 300) {
                    g2.fillOval(x - 3, y - 3, 6, 6);
                }

                prevX = x;
                prevY = y;
            }

            g2.dispose();
        }
    }
}