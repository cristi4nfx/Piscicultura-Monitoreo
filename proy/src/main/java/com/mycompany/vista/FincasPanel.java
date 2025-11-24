package com.mycompany.vista;

import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.dao.AuditoriaDAO;
import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import com.util.ConexionDB;

public class FincasPanel extends JPanel {

    private final MainFrame mainFrame;
    private final Usuario usuarioActual;
    private final boolean soloLectura;

    private Connection conn;
    private Granja_PiscicolaDAO fincaDAO;
    private AuditoriaDAO auditoriaDAO;

    private JPanel listadoPanel;
    private JLabel lblEstado;
    private JTextField txtBuscar;
    private List<GranjaPiscicola> todasLasFincas;

    public FincasPanel(MainFrame mainFrame, Usuario usuarioActual, boolean soloLectura) {
        this.mainFrame = mainFrame;
        this.usuarioActual = usuarioActual;
        this.soloLectura = soloLectura;
        initDB();
        initUI();
        cargarFincas();
    }

    private void initDB() {
        try {
            conn = ConexionDB.getConnection();
            fincaDAO = new Granja_PiscicolaDAO(conn);
            auditoriaDAO = new AuditoriaDAO(conn);
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

        JLabel lblTitulo = new JLabel("Fincas piscícolas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Panel central con buscador
        JPanel centroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        centroPanel.setOpaque(false);
        
        JLabel lblBuscar = new JLabel("⌕");
        lblBuscar.setFont(new Font("Arial Unicode MS", Font.BOLD, 18));
        lblBuscar.setForeground(new Color(25, 118, 210));
        
        txtBuscar = new JTextField(25);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Placeholder
        txtBuscar.setForeground(Color.GRAY);
        txtBuscar.setText("Buscar por nombre o ubicación...");
        
        txtBuscar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtBuscar.getText().equals("Buscar por nombre o ubicación...")) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtBuscar.getText().isEmpty()) {
                    txtBuscar.setForeground(Color.GRAY);
                    txtBuscar.setText("Buscar por nombre o ubicación...");
                }
            }
        });
        
        // Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarFincas();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarFincas();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarFincas();
            }
        });
        
        centroPanel.add(lblBuscar);
        centroPanel.add(txtBuscar);

        // Panel de acciones (derecha)
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnNuevaFinca = new JButton("Nueva finca");

        btnRefrescar.addActionListener(e -> cargarFincas());
        btnNuevaFinca.addActionListener(e -> abrirFormularioFinca(null));

        if (soloLectura) {
            btnNuevaFinca.setVisible(false);
        }

        acciones.add(btnRefrescar);
        acciones.add(btnNuevaFinca);

        top.add(lblTitulo, BorderLayout.WEST);
        top.add(centroPanel, BorderLayout.CENTER);
        top.add(acciones, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        listadoPanel = new JPanel();
        listadoPanel.setLayout(new BoxLayout(listadoPanel, BoxLayout.Y_AXIS));
        listadoPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listadoPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        lblEstado = new JLabel("Listo.");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(lblEstado, BorderLayout.SOUTH);
    }

    // ==============================
    //   CARGAR LISTA DE FINCAS
    // ==============================
    private void cargarFincas() {
        if (fincaDAO == null) {
            setEstado("No se pudo cargar el DAO de fincas.", true);
            return;
        }

        try {
            todasLasFincas = fincaDAO.listarTodos();
            mostrarFincas(todasLasFincas);
            setEstado("Mostrando " + todasLasFincas.size() + " finca(s).", false);
        } catch (Exception e) {
            e.printStackTrace();
            setEstado("Error listando fincas: " + e.getMessage(), true);
        }
    }
    
    // ==============================
    //   FILTRAR FINCAS
    // ==============================
    private void filtrarFincas() {
        if (todasLasFincas == null || todasLasFincas.isEmpty()) {
            return;
        }
        
        String textoBusqueda = txtBuscar.getText().trim();
        
        // Si es el placeholder, mostrar todas
        if (textoBusqueda.equals("Buscar por nombre o ubicación...") || textoBusqueda.isEmpty()) {
            mostrarFincas(todasLasFincas);
            setEstado("Mostrando " + todasLasFincas.size() + " finca(s).", false);
            return;
        }
        
        // Filtrar por nombre o ubicación (case-insensitive)
        List<GranjaPiscicola> fincasFiltradas = todasLasFincas.stream()
            .filter(f -> {
                String nombre = safe(f.getNombre()).toLowerCase();
                String ubicacion = safe(f.getUbicacion()).toLowerCase();
                String busqueda = textoBusqueda.toLowerCase();
                return nombre.contains(busqueda) || ubicacion.contains(busqueda);
            })
            .collect(Collectors.toList());
        
        mostrarFincas(fincasFiltradas);
        
        if (fincasFiltradas.isEmpty()) {
            setEstado("No se encontraron fincas que coincidan con \"" + textoBusqueda + "\"", true);
        } else {
            setEstado("Mostrando " + fincasFiltradas.size() + " finca(s) de " + todasLasFincas.size(), false);
        }
    }
    
    // ==============================
    //   MOSTRAR FINCAS EN EL PANEL
    // ==============================
    private void mostrarFincas(List<GranjaPiscicola> fincas) {
        listadoPanel.removeAll();

        if (fincas.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay fincas para mostrar");
            lblVacio.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblVacio.setForeground(Color.GRAY);
            lblVacio.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            listadoPanel.add(lblVacio);
        } else {
            for (GranjaPiscicola finca : fincas) {
                JPanel card = crearTarjetaFinca(finca);
                listadoPanel.add(card);
                listadoPanel.add(Box.createVerticalStrut(8));
            }
        }

        listadoPanel.revalidate();
        listadoPanel.repaint();
    }

    // ==============================
    //   TARJETA DE CADA FINCA
    // ==============================
    private JPanel crearTarjetaFinca(GranjaPiscicola finca) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB3E5FC)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTitulo = new JLabel("Finca #" + finca.getIdGranja() + " — " + safe(finca.getNombre()));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(0x01579B));

        JLabel lblUb = new JLabel("Ubicación: " + safe(finca.getUbicacion()));
        JLabel lblLong = new JLabel("Altitud: " + finca.getAltitud() + " m");
        JLabel lblArea = new JLabel("Área total: " + finca.getAreaTotal() + " m²");

        for (JLabel l : new JLabel[]{lblUb, lblLong, lblArea}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(0x455A64));
        }

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(lblUb);
        info.add(lblLong);
        info.add(lblArea);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitulo, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        JButton btnVer = new JButton("Ver estanques");
        btnVer.addActionListener(e -> abrirDetalleFinca(finca));
        acciones.add(btnVer);

        if (!soloLectura) {
            JButton btnEditar = new JButton("Editar");
            JButton btnEliminar = new JButton("Eliminar");

            btnEditar.addActionListener(e -> abrirFormularioFinca(finca));
            btnEliminar.addActionListener(e -> eliminarFinca(finca));

            acciones.add(btnEditar);
            acciones.add(btnEliminar);
        }

        header.add(acciones, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    // ==============================
    //   FORM CREAR / EDITAR FINCA
    // ==============================
    private void abrirFormularioFinca(GranjaPiscicola fincaEditar) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        FincaFormDialog dialog = new FincaFormDialog(
                owner,
                conn,
                fincaDAO,
                auditoriaDAO,
                usuarioActual,
                fincaEditar,
                this::cargarFincas
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==============================
    //   ELIMINAR FINCA
    // ==============================
    private void eliminarFinca(GranjaPiscicola finca) {
        int opt = JOptionPane.showConfirmDialog(
                this,
                "Se eliminará la finca \"" + finca.getNombre() + "\" y todos sus estanques.\n" +
                        "¿Quieres continuar?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            String resumenAntes = resumenFinca(finca);

            boolean ok = fincaDAO.eliminarCompleta(finca.getIdGranja());
            if (ok) {
                setEstado("Finca eliminada correctamente.", false);
                cargarFincas();

                if (auditoriaDAO != null) {
                    auditoriaDAO.registrarEliminacion(
                            "FINCA",
                            finca.getIdGranja(),
                            usuarioActual,
                            "Eliminación de finca desde FincasPanel (incluye estanques asociados)",
                            resumenAntes
                    );
                }
            } else {
                setEstado("No se encontró la finca para eliminar.", true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setEstado("Error eliminando finca: " + ex.getMessage(), true);
        }
    }

    // ==============================
    //   VER DETALLE (ESTANQUES)
    // ==============================
    private void abrirDetalleFinca(GranjaPiscicola finca) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner,
                "Estanques de la finca: " + finca.getNombre(),
                Dialog.ModalityType.APPLICATION_MODAL);

        EstanquesPanel panelEstanques = new EstanquesPanel(mainFrame, usuarioActual, soloLectura);
        panelEstanques.cargarEstanquesDeFinca(finca.getIdGranja(), finca.getNombre());
        panelEstanques.setTitulo("Estanques de \"" + finca.getNombre() + "\"");

        dialog.setContentPane(panelEstanques);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==============================
    //   UTILS
    // ==============================
    private void setEstado(String msg, boolean error) {
        if (lblEstado != null) {
            lblEstado.setText(msg);
            lblEstado.setForeground(error ? new Color(0xC62828) : new Color(0x2E7D32));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String resumenFinca(GranjaPiscicola f) {
        StringBuilder sb = new StringBuilder();
        sb.append("idGranja=").append(f.getIdGranja());
        sb.append(", nombre=").append(safe(f.getNombre()));
        sb.append(", ubicacion=").append(safe(f.getUbicacion()));
        sb.append(", altitud=").append(f.getAltitud());
        sb.append(", areaTotal=").append(f.getAreaTotal());
        return sb.toString();
    }
}