package com.mycompany.vista;

import com.mycompany.dao.EstanqueDAO;
import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.dao.LotesDAO;
import com.mycompany.dao.AuditoriaDAO;
import com.mycompany.dao.SensorDAO;                 // 👈 nuevo

import com.mycompany.model.Especie;
import com.mycompany.model.Estanque;
import com.mycompany.model.Lote;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;
import com.util.ConexionDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.List;

public class EstanquesPanel extends JPanel {

    private final MainFrame mainFrame;
    private final Usuario usuarioActual;
    private final boolean soloLectura;
    private final boolean puedeAsignarSensor;   // 👈 solo para Técnico

    private Connection conn;
    private EstanqueDAO estanqueDAO;
    private LotesDAO loteDAO;
    private Granja_PiscicolaDAO fincaDAO;
    private AuditoriaDAO auditoriaDAO;
    private SensorDAO sensorDAO;                // 👈 nuevo

    private JPanel gridPanel;
    private JLabel lblEstado;
    private JLabel lblTitulo;

    /** Si es distinto de null, se filtra por esa finca (se usa con FincasPanel). */
    private Integer idFincaFiltro = null;

    // ==== ctor normal (Admin, etc.) ====
    public EstanquesPanel(MainFrame mainFrame, Usuario usuarioActual, boolean soloLectura) {
        this(mainFrame, usuarioActual, soloLectura, false);
    }

    // ==== ctor con flag de asignar sensor (Técnico) ====
    public EstanquesPanel(MainFrame mainFrame,
                          Usuario usuarioActual,
                          boolean soloLectura,
                          boolean puedeAsignarSensor) {
        this.mainFrame = mainFrame;
        this.usuarioActual = usuarioActual;
        this.soloLectura = soloLectura;
        this.puedeAsignarSensor = puedeAsignarSensor;
        initDB();
        initUI();
        cargarEstanques(); // carga inicial
    }

    private void initDB() {
        try {
            conn = ConexionDB.getConnection();
            estanqueDAO = new EstanqueDAO(conn);
            loteDAO = new LotesDAO(conn);
            fincaDAO = new Granja_PiscicolaDAO(conn);
            auditoriaDAO = new AuditoriaDAO(conn);
            sensorDAO = new SensorDAO(conn);     // 👈 inicializamos SensorDAO
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        lblTitulo = new JLabel("Estanques del sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarEstanques());

        JButton btnCrear = new JButton("Crear estanque");
        btnCrear.addActionListener(e -> abrirFormularioEstanque(null));

        if (soloLectura) {
            btnCrear.setVisible(false);
        }

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnRefrescar);
        acciones.add(btnCrear);

        top.add(lblTitulo, BorderLayout.WEST);
        top.add(acciones, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        gridPanel.setLayout(new GridLayout(0, 3, 16, 16)); // cuadrícula

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        lblEstado = new JLabel("Cargando estanques...");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(lblEstado, BorderLayout.SOUTH);
    }

    // =============================
    //     CARGA DE ESTANQUES
    // =============================

    public void cargarEstanques() {
        cargarEstanques(idFincaFiltro);
    }

    /** Llamar esto desde FincasPanel cuando quieras ver solo los estanques de una finca. */
    public void cargarEstanquesDeFinca(int idFinca, String nombreFinca) {
        this.idFincaFiltro = idFinca;
        setTitulo("Estanques de la finca: " + nombreFinca);
        cargarEstanques(idFinca);
    }

    private void cargarEstanques(Integer idFinca) {
        gridPanel.removeAll();

        if (estanqueDAO == null) {
            setEstado("No se pudo inicializar EstanqueDAO (error de conexión).", true);
            revalidate();
            repaint();
            return;
        }

        try {
            List<Estanque> estanques;
            if (idFinca != null) {
                estanques = estanqueDAO.listarPorFincaConEspecies(idFinca);
            } else {
                estanques = estanqueDAO.listarTodosConEspecies();
            }

            if (estanques.isEmpty()) {
                setEstado("No hay estanques registrados.", false);
            } else {
                for (Estanque est : estanques) {
                    JPanel card = crearTarjetaEstanque(est);
                    gridPanel.add(card);
                }
                setEstado("Mostrando " + estanques.size() + " estanque(s).", false);
            }

            gridPanel.revalidate();
            gridPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            setEstado("Error cargando estanques: " + e.getMessage(), true);
        }
    }

    // =============================
    //        TARJETA
    // =============================

    private JPanel crearTarjetaEstanque(Estanque est) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x81D4FA)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setPreferredSize(new Dimension(260, 140));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String titulo = "Estanque #" + est.getIdEstanque()
                + " — " + safe(est.getTipo());

        JLabel lblTituloCard = new JLabel(titulo);
        lblTituloCard.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTituloCard.setForeground(new Color(0x01579B));

        JLabel lblEstadoEstanque = new JLabel("Estado: " + safe(est.getEstado()));
        lblEstadoEstanque.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadoEstanque.setForeground(new Color(0x546E7A));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(lblTituloCard);
        header.add(lblEstadoEstanque);

        // Especie
        String nombreEspecie = "Sin especie";
        if (est.getEspecies() != null && !est.getEspecies().isEmpty()) {
            Especie esp = est.getEspecies().get(0);
            if (esp != null) {
                nombreEspecie = safe(esp.getNombreComun());
                if (nombreEspecie.isBlank()) {
                    nombreEspecie = safe(esp.getNombreCientifico());
                }
            }
        }
        JLabel lblEspecie = new JLabel("Especie: " + nombreEspecie);
        lblEspecie.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEspecie.setForeground(new Color(0x006064));

        // Nombre de la finca (usando solo el DAO, sin campo en Estanque)
        String textoFinca;
        try {
            String nombreFinca = estanqueDAO.obtenerNombreFincaPorEstanque(est.getIdEstanque());
            if (nombreFinca != null && !nombreFinca.isBlank()) {
                textoFinca = "Finca: " + nombreFinca;
            } else {
                textoFinca = "Finca: no registrada";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            textoFinca = "Finca: error al consultar";
        }
        JLabel lblFinca = new JLabel(textoFinca);
        lblFinca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFinca.setForeground(new Color(0x004D40));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(Box.createVerticalStrut(8));
        body.add(lblEspecie);
        body.add(Box.createVerticalStrut(2));
        body.add(lblFinca);
        body.add(Box.createVerticalStrut(4));

        // Footer: botones
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        footer.setOpaque(false);

        JButton btnVer = new JButton("Ver");
        btnVer.addActionListener(e -> abrirDetalleEstanque(est));
        footer.add(btnVer);

        // Para admin u otros roles que pueden CRUD
        if (!soloLectura) {
            JButton btnEditar = new JButton("Editar");
            JButton btnEliminar = new JButton("Eliminar");

            btnEditar.addActionListener(e -> abrirFormularioEstanque(est));
            btnEliminar.addActionListener(e -> eliminarEstanque(est));

            footer.add(btnEditar);
            footer.add(btnEliminar);
        }

        // Para TÉCNICO: botones de sensor (asignar / quitar)
        if (puedeAsignarSensor) {
            JButton btnAsignar = new JButton("Asignar sensor");
            btnAsignar.addActionListener(e -> abrirAsignarSensor(est));
            footer.add(btnAsignar);

            JButton btnQuitar = new JButton("Quitar sensor");
            btnQuitar.addActionListener(e -> desasignarSensorDeEstanque(est));
            footer.add(btnQuitar);
        }

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        // Click en la tarjeta también abre detalle
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirDetalleEstanque(est);
            }
        });

        return card;
    }

    // =============================
    //  CREAR / EDITAR / ELIMINAR
    // =============================

    private void abrirFormularioEstanque(Estanque estEditar) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        // Si vienes filtrado por finca, usamos ese id_finca; si no, el form te deja escoger finca.
        Integer idFincaContext = idFincaFiltro; // puede ser null

        EstanqueFormDialog dialog = new EstanqueFormDialog(
                owner,
                conn,
                estanqueDAO,
                loteDAO,
                fincaDAO,
                auditoriaDAO,
                usuarioActual,
                estEditar,
                idFincaContext,
                this::cargarEstanques
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarEstanque(Estanque est) {
        int opt = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el estanque #" + est.getIdEstanque() + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            String resumenAntes = resumenEstanque(est);

            boolean ok = estanqueDAO.eliminar(est.getIdEstanque());
            if (ok) {
                setEstado("Estanque #" + est.getIdEstanque() + " eliminado.", false);
                cargarEstanques();

                if (auditoriaDAO != null) {
                    auditoriaDAO.registrarEliminacion(
                            "ESTANQUE",
                            est.getIdEstanque(),
                            usuarioActual,
                            "Eliminación de estanque desde EstanquesPanel",
                            resumenAntes
                    );
                }
            } else {
                setEstado("No se eliminó ningún registro.", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setEstado("Error eliminando estanque: " + e.getMessage(), true);
        }
    }

    // =============================
    //     DETALLE / HISTÓRICO
    // =============================

    private void abrirDetalleEstanque(Estanque est) {
        try {
            if (loteDAO == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo inicializar LoteDAO.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Lote> lotes = loteDAO.listarPorEstanque(est.getIdEstanque());
            if (lotes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay lotes asociados a este estanque.",
                        "Sin lotes",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Lote seleccionado = (Lote) JOptionPane.showInputDialog(
                    this,
                    "Selecciona el lote que quieres visualizar:",
                    "Seleccionar lote",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    lotes.toArray(),
                    lotes.get(0)
            );

            if (seleccionado == null) return;

            boolean activo = (seleccionado.getActivo() == null) || seleccionado.getActivo();

            if (!activo) {
                JOptionPane.showMessageDialog(
                        this,
                        "Este lote está INACTIVO.\n" +
                                "Solo se puede consultar el histórico de pH.",
                        "Lote inactivo",
                        JOptionPane.INFORMATION_MESSAGE
                );

                EstanqueDetalleDialog.mostrarSoloHistorico(
                        mainFrame,
                        est,
                        seleccionado,
                        conn
                );
            } else {
                // 🔒 Verificamos sensor antes del monitoreo en tiempo real
                boolean tieneSensor = false;
                if (sensorDAO != null) {
                    try {
                        tieneSensor = sensorDAO.tieneSensorActivo(est.getIdEstanque());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (!tieneSensor) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Este estanque no tiene un sensor activo asignado.\n" +
                                    "Un técnico debe asignar uno para habilitar los datos en tiempo real.\n" +
                                    "Solo se mostrará el histórico de pH.",
                            "Sin sensor asignado",
                            JOptionPane.WARNING_MESSAGE
                    );

                    EstanqueDetalleDialog.mostrarSoloHistorico(
                            mainFrame,
                            est,
                            seleccionado,
                            conn
                    );
                    return;
                }

                // Tiene sensor activo → monitor completo
                EstanqueDetalleDialog dialog =
                        new EstanqueDetalleDialog(mainFrame, est, seleccionado, conn);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error cargando lotes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============================
    //   ASIGNAR SENSOR (Técnico)
    // =============================

    private void abrirAsignarSensor(Estanque est) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        try {
            AsignarSensorDialog dlg = new AsignarSensorDialog(owner, conn, est.getIdEstanque());
            dlg.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error abriendo asignación de sensor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =============================
    //       UTILIDADES
    // =============================

    private void setEstado(String msg, boolean error) {
        if (lblEstado != null) {
            lblEstado.setText(msg);
            lblEstado.setForeground(error ? new Color(0xC62828) : new Color(0x2E7D32));
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    public void setTitulo(String titulo) {
        if (lblTitulo != null) {
            lblTitulo.setText(titulo);
        }
    }

    /** Construye un resumen simple del estanque para guardar en la auditoría. */
    private String resumenEstanque(Estanque est) {
        StringBuilder sb = new StringBuilder();
        sb.append("idEstanque=").append(est.getIdEstanque());
        sb.append(", tipo=").append(safe(est.getTipo()));
        sb.append(", estado=").append(safe(est.getEstado()));

        if (est.getEspecies() != null && !est.getEspecies().isEmpty()) {
            Especie esp = est.getEspecies().get(0);
            if (esp != null) {
                sb.append(", especie=");
                sb.append(safe(esp.getNombreComun()));
            }
        }
        return sb.toString();
    }
    
    private void desasignarSensorDeEstanque(Estanque est) {
    if (sensorDAO == null) {
        JOptionPane.showMessageDialog(this,
                "No se pudo inicializar el DAO de sensores.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    int opt = JOptionPane.showConfirmDialog(
            this,
            "Se desasignarán los sensores del estanque #" + est.getIdEstanque() +
            " y quedarán disponibles para otros estanques.\n\n¿Deseas continuar?",
            "Quitar sensor",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );
    if (opt != JOptionPane.YES_OPTION) return;

    try {
        boolean ok = sensorDAO.desasignarSensoresDeEstanque(est.getIdEstanque());
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Sensor(es) desasignado(s) correctamente.",
                    "Operación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            // refrescamos tarjetas por si quieres mostrar si tiene/no tiene sensor en el futuro
            cargarEstanques();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Este estanque no tenía sensores asignados.",
                    "Sin cambios",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error al desasignar sensor: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

}
