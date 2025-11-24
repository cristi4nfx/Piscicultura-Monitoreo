// com.mycompany.vista.EstanqueDetalleDialog
package com.mycompany.vista;

import com.mycompany.dao.EspecieDAO;
import com.mycompany.dao.LotesDAO;
import com.mycompany.model.Estanque;
import com.mycompany.model.Lote;
import com.mycompany.model.Especie;
import com.mycompany.model.Parametro;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.fazecast.jSerialComm.SerialPort;



public class EstanqueDetalleDialog extends JDialog {

    private final Estanque estanque;
    private final EspecieDAO especieDAO;
    private final Lote lote;
    private final LotesDAO lotesDAO; 
    private final Connection conn; // app_db (donde está la foreign table)

    private PhGaugePanel phGaugePanel;
    private JLabel lblEstadoSemaforo;
    private JLabel lblValorPh;
    private JLabel lblRangoPh;
    private JLabel lblEspecieTitulo;
    private JLabel lblEspecieCant;
    private JLabel lblParametros;   // HTML con rangos de la especie
    private JLabel lblImagenEspecie;

    private Timer timer;
    private SerialPort comPort;
    private Thread hiloSerial;
    private volatile boolean lecturaActiva = false;

    private final DecimalFormat df = new DecimalFormat("0.0");

    public EstanqueDetalleDialog(Frame owner, Estanque estanque, Lote lote, Connection conn) throws Exception {
        super(owner, "Monitoreo de pH - Lote #" + lote.getIdLote(), true);
        this.estanque = estanque;
        this.lote = lote;
        this.conn = conn;
        this.especieDAO = new EspecieDAO(conn);
        this.lotesDAO  = new LotesDAO(conn);   // 👈 inicializar DAO

        initUI();

        Double phInicial = lote.getPhActual();
        if (phInicial != null) {
            actualizarUIph(phInicial);
        }

        iniciarSimulacionPh();

        pack();
        setMinimumSize(new Dimension(900, 480));
        setLocationRelativeTo(owner);
    }

    /** Utilidad para cuando el lote está INACTIVO: abre solo el histórico. */
    public static void mostrarSoloHistorico(Frame owner,
                                            Estanque estanque,
                                            Lote lote,
                                            Connection conn) {
        HistoricoPhDialog dialog =
                new HistoricoPhDialog(owner, estanque, lote, conn, false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ================== UI MONITOR ==================

    private void initUI() throws Exception {
        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(root);

        JLabel lblTitulo = new JLabel("Monitoreo de pH", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        root.add(lblTitulo, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);
        root.add(center, BorderLayout.CENTER);

        // Gauge centro
        phGaugePanel = new PhGaugePanel();
        CardPanel gaugeCard = new CardPanel();
        gaugeCard.setLayout(new BorderLayout());
        gaugeCard.add(phGaugePanel, BorderLayout.CENTER);
        gaugeCard.setPreferredSize(new Dimension(320, 320));
        center.add(gaugeCard, BorderLayout.CENTER);

        // Izquierda: especie
        CardPanel especieCard = crearPanelEspecie();
        especieCard.setPreferredSize(new Dimension(260, 260));
        center.add(especieCard, BorderLayout.WEST);

        // Derecha: estado + rangos + histórico
        CardPanel estadoCard = crearPanelEstadoAguaYRangos();
        estadoCard.setPreferredSize(new Dimension(260, 260));
        center.add(estadoCard, BorderLayout.EAST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> {
            // parar lectura desde Arduino
            lecturaActiva = false;

            if (hiloSerial != null) {
                try { hiloSerial.join(1000); } catch (InterruptedException ignored) {}
            }

            dispose();
        });
        bottom.add(btnCerrar);
        root.add(bottom, BorderLayout.SOUTH);
    }

    // ---------- IZQUIERDA: especie (nombre + cantidad + imagen) ----------

    private CardPanel crearPanelEspecie() throws Exception {
        CardPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel lblHeader = new JLabel("Información de la especie");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(0x01579B));

        Especie esp = obtenerEspecieDelLote();
        String nombreComun = esp != null ? safe(esp.getNombreComun()) : "Sin especie";
        String nombreCient = esp != null ? safe(esp.getNombreCientifico()) : "";

        Integer cantidad = (esp != null ? esp.getCantidad() : null);
        String cantTxt =
                (cantidad != null ? (cantidad + " peces") : "Cantidad no registrada");

        lblEspecieTitulo = new JLabel(
                "<html><b>" + nombreComun + "</b>" +
                        (nombreCient.isBlank() ? "" : "  <i>(" + nombreCient + ")</i>") +
                        "</html>"
        );
        lblEspecieTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblEspecieCant = new JLabel("• " + cantTxt);
        lblEspecieCant.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEspecieCant.setForeground(new Color(0x00796B));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(lblHeader);
        top.add(Box.createVerticalStrut(6));
        top.add(lblEspecieTitulo);
        top.add(Box.createVerticalStrut(3));
        top.add(lblEspecieCant);

        lblImagenEspecie = new JLabel();
        lblImagenEspecie.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagenEspecie.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon icon = cargarIconoEspecieDesdeBD(lote.getIdEspecie());
        if (icon != null) {
            lblImagenEspecie.setIcon(icon);
        } else {
            lblImagenEspecie.setText("<html><i>Sin imagen registrada.</i></html>");
            lblImagenEspecie.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblImagenEspecie.setForeground(new Color(0x607D8B));
        }

        panel.add(top, BorderLayout.NORTH);
        panel.add(lblImagenEspecie, BorderLayout.CENTER);

        return panel;
    }

    private Especie obtenerEspecieDelLote() {
        if (lote == null) return null;

        int idEspLote = lote.getIdEspecie();

        // 1) Intentar en la lista que ya trae el estanque
        if (estanque.getEspecies() != null && !estanque.getEspecies().isEmpty()) {
            for (Especie e : estanque.getEspecies()) {
                if (e != null && e.getIdEspecie() == idEspLote) {
                    return e;
                }
            }
        }

        // 2) Buscar en tabla especie (nombre, científico, cantidad, imagen_ruta)
        String sql = """
            SELECT nombre_comun, nombre_cientifico, cantidad
            FROM especie
            WHERE id_especie = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEspLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Especie e = new Especie();
                    e.setIdEspecie(idEspLote);
                    e.setNombreComun(rs.getString("nombre_comun"));
                    e.setNombreCientifico(rs.getString("nombre_cientifico"));
                    try {
                        int cant = rs.getInt("cantidad");
                        if (!rs.wasNull()) {
                            e.setCantidad(cant);
                        }
                    } catch (SQLException ignore) {}
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private ImageIcon cargarIconoEspecieDesdeBD(int idEspecie) {
        String sql = """
            SELECT imagen_ruta
            FROM especie
            WHERE id_especie = ?
        """;

        String ruta = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEspecie);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ruta = rs.getString("imagen_ruta");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        ImageIcon icon;
        try {
            URL url = getClass().getResource(ruta);
            if (url != null) {
                icon = new ImageIcon(url);
            } else {
                icon = new ImageIcon(ruta);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return null;
        }

        int targetSize = 120;
        Image img = icon.getImage().getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // ---------- DERECHA: Estado + rangos + botón histórico ----------

    private CardPanel crearPanelEstadoAguaYRangos() {
        CardPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblHeaderEstado = new JLabel("Estado del agua");
        lblHeaderEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderEstado.setForeground(new Color(0x01579B));
        lblHeaderEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValorPh = new JLabel("pH actual: --");
        lblValorPh.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValorPh.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblEstadoSemaforo = new JLabel("Estado: --");
        lblEstadoSemaforo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstadoSemaforo.setOpaque(true);
        lblEstadoSemaforo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblEstadoSemaforo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblRangoPh = new JLabel(
                "<html><small>Rango recomendado de pH para esta especie: se muestra abajo.</small></html>"
        );
        lblRangoPh.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTip = new JLabel(
                "<html><small>Consejo: vigila variaciones rápidas de pH, " +
                        "podrían indicar problemas en el estanque.</small></html>"
        );
        lblTip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeaderRangos = new JLabel("Rangos recomendados de la especie");
        lblHeaderRangos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderRangos.setForeground(new Color(0x01579B));
        lblHeaderRangos.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblParametros = new JLabel(buildParametrosHtmlDesdeBD());
        lblParametros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblParametros.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnHistorico = new JButton("Ver histórico de pH…");
        btnHistorico.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHistorico.addActionListener(e -> abrirHistorico());

        info.add(lblHeaderEstado);
        info.add(Box.createVerticalStrut(8));
        info.add(lblValorPh);
        info.add(Box.createVerticalStrut(8));
        info.add(lblEstadoSemaforo);
        info.add(Box.createVerticalStrut(8));
        info.add(lblRangoPh);
        info.add(Box.createVerticalStrut(8));
        info.add(lblTip);
        info.add(Box.createVerticalStrut(12));
        info.add(lblHeaderRangos);
        info.add(Box.createVerticalStrut(4));
        info.add(lblParametros);
        info.add(Box.createVerticalGlue());
        info.add(btnHistorico);

        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    private String buildParametrosHtmlDesdeBD() {
        if (lote == null) {
            return "<html><i>No hay información de especie para este lote.</i></html>";
        }

        List<Parametro> parametros;
        try {
            parametros = especieDAO.obtenerParametrosPorEspecie(lote.getIdEspecie());
        } catch (SQLException e) {
            e.printStackTrace();
            return "<html><i>Error cargando parámetros de la especie.</i></html>";
        }

        if (parametros == null || parametros.isEmpty()) {
            return "<html><i>No hay parámetros configurados para esta especie.</i></html>";
        }

        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<ul style='margin-top:0; padding-left:14px;'>");

        for (Parametro p : parametros) {
            String nombre = safe(p.getNombre());
            String unidad = safe(p.getUnidad());

            Double vMin = null;
            Double vMax = null;
            try { vMin = (double) p.getRangoMin(); } catch (Exception ignored) {}
            try { vMax = (double) p.getRangoMax(); } catch (Exception ignored) {}

            String rangoTxt;
            if (vMin != null && vMax != null) {
                rangoTxt = df.format(vMin) + " – " + df.format(vMax);
                if (!unidad.isBlank()) rangoTxt += " " + unidad;
            } else {
                rangoTxt = unidad.isBlank() ? "-" : unidad;
            }

            sb.append("<li><b>")
              .append(nombre)
              .append(":</b> ")
              .append(rangoTxt)
              .append("</li>");
        }

        sb.append("</ul></html>");
        return sb.toString();
    }

    // ================== pH (simulación) ==================

    private void actualizarUIph(double ph) {
        phGaugePanel.setPhValue(ph);
        lblValorPh.setText("pH actual: " + df.format(ph));

        if (ph >= 6.5 && ph <= 8.5) {
            lblEstadoSemaforo.setText("Estado: Óptimo");
            lblEstadoSemaforo.setBackground(new Color(0x2E7D32));
            lblEstadoSemaforo.setForeground(Color.WHITE);
        } else if (ph >= 6.0 && ph <= 9.0) {
            lblEstadoSemaforo.setText("Estado: Atención");
            lblEstadoSemaforo.setBackground(new Color(0xF9A825));
            lblEstadoSemaforo.setForeground(Color.BLACK);
        } else {
            lblEstadoSemaforo.setText("Estado: Crítico");
            lblEstadoSemaforo.setBackground(new Color(0xC62828));
            lblEstadoSemaforo.setForeground(Color.WHITE);
        }
    }
    
    
    //simulación
    
    private void iniciarSimulacionPh() {
        // Ajusta a tu puerto y baud rate reales
        final String puerto = "COM3";   // CAMBIA ESTO SI ES OTRO PUERTO
        final int baudRate = 9600;

        lecturaActiva = true;

        hiloSerial = new Thread(() -> {
            BufferedReader reader = null;

            try {
                // 1. Abrir puerto serie
                comPort = SerialPort.getCommPort(puerto);
                comPort.setBaudRate(baudRate);
                comPort.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                        0,
                        0
                );

                if (!comPort.openPort()) {
                    System.err.println("[ARDUINO] No se pudo abrir el puerto " + puerto);
                    return;
                }
                System.out.println("[ARDUINO] Puerto " + puerto + " abierto.");

                // 2. Enviar comando 'S' al Arduino para iniciar envío
                try {
                    comPort.getOutputStream().write('S');
                    comPort.getOutputStream().flush();
                    System.out.println("[ARDUINO] Comando 'S' enviado.");
                } catch (Exception ex) {
                    System.err.println("[ARDUINO] No se pudo enviar comando 'S'");
                    ex.printStackTrace();
                }

                reader = new BufferedReader(
                        new InputStreamReader(comPort.getInputStream())
                );

                // 3. Bucle de lectura
                while (lecturaActiva) {
                    String line = reader.readLine(); // bloquea hasta que llega algo

                    if (line == null) {
                        continue;
                    }
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    System.out.println("[ARDUINO] Línea recibida: " + line);

                    try {
                        // IMPORTANTE: el Arduino debe mandar SOLO el número de pH
                        double ph = Double.parseDouble(line);

                        // 3.1 Actualizar modelo en memoria
                        lote.setPhActual(ph);

                        // 3.2 Actualizar UI en el EDT
                        final double phFinal = ph;
                        SwingUtilities.invokeLater(() -> actualizarUIph(phFinal));

                        // 3.3 Actualizar BD → dispara trigger hacia lotes_ph_historial
                        try {
                            System.out.println("[ARDUINO] Actualizando lote "
                                    + lote.getIdLote() + " con pH=" + ph);

                            boolean ok = lotesDAO.actualizarPh(lote.getIdLote(), ph);

                            if (!ok) {
                                System.err.println("[ARDUINO] UPDATE devolvió 0 filas. "
                                        + "Revisa que exista lotes.id_lote = " + lote.getIdLote());
                            } else {
                                System.out.println("[ARDUINO] UPDATE OK para lote " + lote.getIdLote());
                            }
                        } catch (SQLException ex) {
                            System.err.println("[ARDUINO] ERROR al actualizar pH en BD");
                            System.err.println("Mensaje: " + ex.getMessage());
                            ex.printStackTrace();
                        }

                    } catch (NumberFormatException ex) {
                        // La línea no era solo un número → se ignora
                        System.out.println("[ARDUINO] No pude convertir a número, se omite: " + line);
                    }
                }

            } catch (Exception e) {
                System.err.println("[ARDUINO] Error general en hilo de lectura");
                e.printStackTrace();

            } finally {
                // 4. Cierre ordenado del puerto
                if (comPort != null && comPort.isOpen()) {
                    try {
                        // opcional: enviar 'P' para que Arduino pare
                        comPort.getOutputStream().write('P');
                        comPort.getOutputStream().flush();
                    } catch (Exception ignored) {}
                    comPort.closePort();
                    System.out.println("[ARDUINO] Puerto cerrado.");
                }

                if (reader != null) {
                    try { reader.close(); } catch (Exception ignored) {}
                }
            }

        }, "LectorPH-Lote-" + lote.getIdLote());

        hiloSerial.start();
    }
    // ================== Histórico ==================

    private void abrirHistorico() {
        HistoricoPhDialog dialog =
                new HistoricoPhDialog((Frame) getOwner(), estanque, lote, conn, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ================== Helpers de estilo ==================

    private static class PhGaugePanel extends JPanel {

        private double phValue = 7.2;

        public PhGaugePanel() {
            setPreferredSize(new Dimension(280, 280));
            setOpaque(false);
        }

        public void setPhValue(double value) {
            this.phValue = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, new Color(0x0288D1),
                    0, h, new Color(0x4FC3F7));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 40, 40);

            int size = Math.min(w, h) - 90;
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillOval(x - 15, y - 15, size + 30, size + 30);

            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillOval(x, y, size, size);

            String txt = new DecimalFormat("0.0").format(phValue);
            g2.setColor(new Color(0x0277BD));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 42));

            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(txt)) / 2;
            int ty = y + (size + fm.getAscent()) / 2 - 12;
            g2.drawString(txt, tx, ty);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            String label = "pH";
            int lx = x + (size - g2.getFontMetrics().stringWidth(label)) / 2;
            int ly = ty + 26;
            g2.drawString(label, lx, ly);

            g2.dispose();
        }
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0x003B5C),
                    0, h, new Color(0x0288D1)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    private static class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
