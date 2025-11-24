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

public class DashboardInvestigadorPanel extends JPanel {

    private final Usuario usuario;
    private final MainFrame mainFrame;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnInicio;
    private JButton btnFincas;
    private JButton btnEstanques;
    
    Granja_PiscicolaDAO fincaDAO;
    EstanqueDAO estanqueDAO;
    Connection conn;

    public DashboardInvestigadorPanel(MainFrame mainFrame, Usuario usuario) {
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
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(new HeaderPanel(mainFrame, usuario), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x01579B));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        JLabel lblBrand = new JLabel("<html><b>Investigador</b><br/>Piscicultura</html>");
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandPanel.add(lblBrand);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(10));

        btnInicio = crearBotonMenu("Inicio");
        btnFincas = crearBotonMenu("Fincas");
        btnEstanques = crearBotonMenu("Estanques");

        sidebar.add(btnInicio);
        sidebar.add(btnFincas);
        sidebar.add(btnEstanques);
        sidebar.add(Box.createVerticalGlue());

        centerPanel.add(sidebar, BorderLayout.WEST);

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

        // Reutilizamos los mismos paneles, pero en soloLectura = true
        FincasPanel fincasPanel = new FincasPanel(mainFrame, usuario, true);
        EstanquesPanel estanquesPanel = new EstanquesPanel(mainFrame, usuario, true, false);
        
        contentPanel.add(resumen,        "INICIO");
        contentPanel.add(fincasPanel, "FINCAS");
        contentPanel.add(estanquesPanel, "ESTANQUES");

        centerPanel.add(contentPanel, BorderLayout.CENTER);

        btnInicio.addActionListener(e -> mostrarVista("INICIO", btnInicio));
        btnFincas.addActionListener(e -> mostrarVista("FINCAS", btnFincas));
        btnEstanques.addActionListener(e -> mostrarVista("ESTANQUES", btnEstanques));

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
        // podrías reusar aquí la lógica de resaltado de botones como en admin
    }
}
