package com.mycompany.vista;

import com.mycompany.dao.EstanqueDAO;
import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;
import com.mycompany.vista.HeaderPanel;
import com.util.ConexionDB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class DashboardTecnicoPanel extends JPanel {

    private final Usuario usuario;
    private final MainFrame mainFrame;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnInicio;
    private JButton btnUsuarios;
    private JButton btnFincas;
    private JButton btnEstanques;
    private Connection conn;
    private Granja_PiscicolaDAO fincaDAO;
    private EstanqueDAO estanqueDAO;

    public DashboardTecnicoPanel(MainFrame mainFrame, Usuario usuario) throws Exception {
        this.mainFrame = mainFrame;
        this.usuario = usuario;
        initDB();   // ⬅ primero inicializamos la BD
        initUI();   // luego construimos la interfaz
    }

    private void initDB() {
        try {
            conn = ConexionDB.getConnection();
            fincaDAO = new Granja_PiscicolaDAO(conn);
            estanqueDAO = new EstanqueDAO(conn);
        } catch (Exception e) {
            e.printStackTrace();
            // Si algo falla, dejamos los DAOs en null, y más abajo
            // mostraremos valores por defecto en el resumen.
        }
    }

    private void initUI() throws Exception {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header con usuario + rol + cerrar sesión
        add(new HeaderPanel(mainFrame, usuario), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        // ================= SIDEBAR =================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x0277BD)); // azul técnico
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        JLabel lblBrand = new JLabel("<html><b>Técnico</b><br/>Piscicultura</html>");
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandPanel.add(lblBrand);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(10));

        btnInicio    = crearBotonMenu("Inicio");
        btnFincas    = crearBotonMenu("Fincas");
        btnEstanques = crearBotonMenu("Estanques");

        sidebar.add(btnInicio);
        sidebar.add(btnFincas);
        sidebar.add(btnEstanques);
        sidebar.add(Box.createVerticalGlue());

        centerPanel.add(sidebar, BorderLayout.WEST);

        // ================= CONTENIDO (CardLayout) =================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

     
        // ====== PANEL RESUMEN (nuevo diseño) ======
        ResumenOperativoPanel resumen = new ResumenOperativoPanel(mainFrame, usuario);

        int fincas = 0;
        int estanques = 0;
        double salud = 0;

        try {
            if (fincaDAO != null) {
                fincas = fincaDAO.contarFincasActivas();
            }
            if (estanqueDAO != null) {
                estanques = estanqueDAO.contarMonitoreados();
                salud = estanqueDAO.calculoSaludPromedio(); // 0–100
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        resumen.actualizarMetricas(fincas, estanques, salud, "Hoy, 15:00");

        // Módulos reutilizables
        // Técnico: usuarios = edición, fincas/estanques = solo lectura
        FincasPanel   fincasPanel     = new FincasPanel(mainFrame, usuario, true);
        EstanquesPanel estanquesPanel = new EstanquesPanel(mainFrame, usuario, true,true);
        
        contentPanel.add(resumen,        "INICIO");
        contentPanel.add(fincasPanel,    "FINCAS");
        contentPanel.add(estanquesPanel, "ESTANQUES");

        centerPanel.add(contentPanel, BorderLayout.CENTER);

        // Eventos de navegación
        btnInicio.addActionListener(e -> mostrarVista("INICIO", btnInicio));
        btnFincas.addActionListener(e -> mostrarVista("FINCAS", btnFincas));
        btnEstanques.addActionListener(e -> mostrarVista("ESTANQUES", btnEstanques));

        // Vista inicial
        mostrarVista("INICIO", btnInicio);
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(new Color(0xE1F5FE));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        return btn;
    }

    private void mostrarVista(String nombre, JButton origen) {
        cardLayout.show(contentPanel, nombre);
        resaltarBoton(origen);
    }

    private void resaltarBoton(JButton activo) {
        JButton[] botones = {btnInicio, btnUsuarios, btnFincas, btnEstanques};
        for (JButton b : botones) {
            if (b == null) continue;
            if (b == activo) {
                b.setForeground(Color.WHITE);
                b.setOpaque(true);
                b.setBackground(new Color(0x0288D1));
            } else {
                b.setForeground(new Color(0xE1F5FE));
                b.setOpaque(false);
                b.setBackground(new Color(0x0277BD));
            }
        }
        repaint();
    }

}
