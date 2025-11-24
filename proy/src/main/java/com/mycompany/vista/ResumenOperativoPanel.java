package com.mycompany.vista;

import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;
import com.util.ConexionDB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

/**
 * Panel de resumen que se usa en los dashboards
 * de ADMIN, TÉCNICO e INVESTIGADOR.
 */
public class ResumenOperativoPanel extends JPanel {

    private final MainFrame mainFrame;
    private final Usuario usuario;
    private Connection conn;


    // ====== MÉTRICAS (se pueden alimentar con datos reales) ======
    private int fincasActivas          = 15;      // TODO: reemplazar por datos reales
    private int estanquesMonitoreados  = 120;     // TODO: reemplazar por datos reales
    private double saludPromedioLote   = 98.0;    // TODO: reemplazar por datos reales
    private String proximaAlimentacion = "Hoy, 15:00"; // TODO: reemplazar por datos reales

    private void initDB() {
        try {
            conn = ConexionDB.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error conectando a la base de datos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ResumenOperativoPanel(MainFrame mainFrame, Usuario usuario) {
        this.mainFrame = mainFrame;
        this.usuario = usuario;

        initDB(); 
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    /**
     * En un futuro, desde el Dashboard puedes llamar este método
     * con valores calculados desde la BD.
     */
    public void actualizarMetricas(int fincasActivas,
                                   int estanquesMonitoreados,
                                   double saludPromedioLote,
                                   String proximaAlimentacion) {
        this.fincasActivas = fincasActivas;
        this.estanquesMonitoreados = estanquesMonitoreados;
        this.saludPromedioLote = saludPromedioLote;
        this.proximaAlimentacion = proximaAlimentacion;
        buildContent();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Fondo tipo "agua" sencillo (degradado azul)
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0xE1F5FE),
                0, h, new Color(0xB3E5FC)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }

    private void buildContent() {
        removeAll();

        // ===== 1. TARJETAS SUPERIORES =====
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createMetricCard("FINCAS ACTIVAS",
                String.valueOf(fincasActivas), "🐟", new Color(0x039BE5)));

        cardsPanel.add(createMetricCard("ESTANQUES MONITOREADOS",
                String.valueOf(estanquesMonitoreados), "💧", new Color(0x26C6DA)));

        cardsPanel.add(createMetricCard("SALUD PROMEDIO LOTE",
                String.format("%.0f%%", saludPromedioLote), "📈", new Color(0x43A047)));

        cardsPanel.add(createMetricCard("PRÓXIMA REVISIÓN",
                proximaAlimentacion, "📅", new Color(0x5C6BC0)));

        add(cardsPanel, BorderLayout.NORTH);

        // ===== 2. CENTRO: IZQ = BOTÓN REPORTE, DER = GRÁFICA DE PARÁMETROS =====
        JPanel centerCharts = new JPanel(new GridLayout(1, 2, 16, 0));
        centerCharts.setOpaque(false);

        // --- Tarjeta de "Generar reporte" ---
        JPanel cardReporte = createCard();
        cardReporte.setLayout(new BorderLayout(10, 10));

        JLabel lblTituloReporte = new JLabel("Reportes de producción", SwingConstants.CENTER);
        lblTituloReporte.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardReporte.add(lblTituloReporte, BorderLayout.NORTH);

        JButton btnGenerarReporte = new JButton();
        btnGenerarReporte.setFocusPainted(false);
        btnGenerarReporte.setBorderPainted(false);
        btnGenerarReporte.setContentAreaFilled(false);
        btnGenerarReporte.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Estilos del texto
        btnGenerarReporte.setText("""
        <html>
        <div style='text-align:center; padding:8px;'>
            <span style='font-size:22px;'>📊</span><br>
            <span style='font-size:16px; font-weight:600;'>GENERAR REPORTE</span><br>
            <span style='font-size:11px; color:#455A64;'>Producción, pH, eventos y más</span>
        </div>
        </html>
        """);

        // Panel envolvente para dar estilo visual
        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Fondo degradado
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x42A5F5),
                        0, h, new Color(0x1E88E5)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                // Sombra sutil
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        wrapper.add(btnGenerarReporte, BorderLayout.CENTER);

        // Hover efecto suave
        btnGenerarReporte.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                wrapper.repaint();
                wrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255,255,255,120), 2),
                        BorderFactory.createEmptyBorder(10,10,10,10)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            }
        });

        btnGenerarReporte.addActionListener(e -> {
            if (conn == null) {
                JOptionPane.showMessageDialog(this,
                        "No hay conexión disponible.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ReporteDialog dialog = new ReporteDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    conn
            );

            dialog.setVisible(true);
        });

        cardReporte.add(wrapper, BorderLayout.CENTER);



        // --- Tarjeta de parámetros de agua (gráfico de líneas) ---
        JPanel cardParametros = createCard();
        cardParametros.setLayout(new BorderLayout(5, 5));
        JLabel lblParam = new JLabel("Parámetros de Agua Clave (Últimas 24h)", SwingConstants.CENTER);
        lblParam.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cardParametros.add(lblParam, BorderLayout.NORTH);
        cardParametros.add(new ParametrosAguaChartPanel(), BorderLayout.CENTER);

        centerCharts.add(cardReporte);
        centerCharts.add(cardParametros);

        add(centerCharts, BorderLayout.CENTER);

        // ===== 3. NOTIFICACIONES =====
        JPanel bottomCard = createCard();
        bottomCard.setLayout(new BorderLayout(5, 5));

        JLabel lblNotif = new JLabel("NOTIFICACIONES RECIENTES");
        lblNotif.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bottomCard.add(lblNotif, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("⚠️ Alerta: pH bajo en Estanque #3");
        model.addElement("⚠️ Alerta: pH alto en Estanque #2");
        model.addElement("🛠 Mantenimiento programado Finca #2 – 18/11, 08:00");
        model.addElement("✅ Última sincronización con sensores: hace 5 min");

        JList<String> lst = new JList<>(model);
        lst.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomCard.add(new JScrollPane(lst), BorderLayout.CENTER);

        add(bottomCard, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createMetricCard(String titulo, String valor, String emoji, Color accent) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(5, 5));

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(0x607D8B));

        JLabel lblValor = new JLabel(valor, SwingConstants.CENTER);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValor.setForeground(accent);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(4));
        center.add(lblIcon);
        center.add(Box.createVerticalStrut(4));
        center.add(lblValor);
        center.add(Box.createVerticalStrut(2));
        center.add(lblTitulo);
        center.add(Box.createVerticalGlue());

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCard() {
        return new RoundedCardPanel();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // construir contenido cuando el panel ya tiene tamaño
        buildContent();
    }

    // ======== Panel con borde redondeado ========
    private static class RoundedCardPanel extends JPanel {
        RoundedCardPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(new Color(0xE0E0E0));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ======== Gráfico de líneas dummy (se puede conectar a datos reales después) ========
    private static class ParametrosAguaChartPanel extends JPanel {
        // Oxígeno y Disuelto (valores dummy)
        private final double[] oxigeno  = {2, 3, 5, 4, 4.5, 3.5, 4};
        private final double[] disuelto = {1.5, 2, 3, 2.5, 3, 2.8, 3.2};
        private final String[] labels   = {"0h", "6h", "12h", "18h", "24h", "30h", "36h"};

        ParametrosAguaChartPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int margenIzq = 35, margenDer = 10, margenSup = 15, margenInf = 35;
            int areaW = w - margenIzq - margenDer;
            int areaH = h - margenSup - margenInf;

            g2.setColor(new Color(0xE3F2FD));
            g2.fillRect(margenIzq, margenSup, areaW, areaH);

            double max = 0;
            for (double v : oxigeno) max = Math.max(max, v);
            for (double v : disuelto) max = Math.max(max, v);
            if (max == 0) max = 1;

            int n = oxigeno.length;

            // ejes
            g2.setColor(new Color(0x90A4AE));
            g2.drawLine(margenIzq, margenSup + areaH, margenIzq + areaW, margenSup + areaH);
            g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + areaH);

            // líneas
            drawSerie(g2, oxigeno, new Color(0x1E88E5), margenIzq, margenSup, areaW, areaH, max);
            drawSerie(g2, disuelto, new Color(0x26C6DA), margenIzq, margenSup, areaW, areaH, max);

            // labels eje X
            g2.setColor(new Color(0x546E7A));
            int stepX = areaW / (n - 1);
            for (int i = 0; i < n; i++) {
                int x = margenIzq + i * stepX;
                String lab = labels[i];
                int tw = g2.getFontMetrics().stringWidth(lab);
                g2.drawString(lab, x - tw / 2, margenSup + areaH + 15);
            }

            // leyenda simple
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            int ly = margenSup + 15;
            g2.setColor(new Color(0x1E88E5));
            g2.fillRect(w - 110, ly - 8, 10, 4);
            g2.setColor(new Color(0x263238));
            g2.drawString("Oxígeno", w - 95, ly);

            g2.setColor(new Color(0x26C6DA));
            g2.fillRect(w - 110, ly + 10 - 8, 10, 4);
            g2.setColor(new Color(0x263238));
            g2.drawString("Disuelto", w - 95, ly + 10);

            g2.dispose();
        }

        private void drawSerie(Graphics2D g2,
                               double[] serie,
                               Color color,
                               int margenIzq, int margenSup,
                               int areaW, int areaH,
                               double max) {
            int n = serie.length;
            int stepX = areaW / (n - 1);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));

            int prevX = -1, prevY = -1;
            for (int i = 0; i < n; i++) {
                int x = margenIzq + i * stepX;
                int y = margenSup + (int) ((max - serie[i]) * areaH / max);
                if (prevX != -1) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                g2.fillOval(x - 3, y - 3, 6, 6);
                prevX = x;
                prevY = y;
            }
        }
    }
}
