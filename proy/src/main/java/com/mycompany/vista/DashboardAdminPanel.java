package com.mycompany.vista;

import com.mycompany.dao.EstanqueDAO;
import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;
import com.mycompany.vista.HeaderPanel; // si tu HeaderPanel está en otro paquete, ajusta esto
import com.util.ConexionDB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class DashboardAdminPanel extends JPanel {

    private final Usuario usuario;
    private final MainFrame mainFrame;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnInicio;
    private JButton btnUsuarios;
    private JButton btnFincas;
    private JButton btnAuditoria;
    private JButton btnEstanques;
    
        
    Granja_PiscicolaDAO fincaDAO;
    EstanqueDAO estanqueDAO;
    Connection conn;

    public DashboardAdminPanel(MainFrame mainFrame, Usuario usuario) throws Exception {
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

        // 🔹 Header arriba
        add(new HeaderPanel(mainFrame, usuario), BorderLayout.NORTH);

        // 🔹 Panel central con sidebar + contenido
        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        // ===== SIDEBAR (MENÚ LATERAL) =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x0D47A1)); // azul profundo
        sidebar.setPreferredSize(new Dimension(220, 0));

        
        // Branding / título
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        JLabel lblBrand = new JLabel("<html><b>Piscicultura</b><br/>Inteligente</html>");
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandPanel.add(lblBrand);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // Botones de menú
        btnInicio = crearBotonMenu("Inicio");
        btnUsuarios = crearBotonMenu("Usuarios");
        btnFincas = crearBotonMenu("Fincas");
        btnEstanques = crearBotonMenu("Estanques");
        btnAuditoria = crearBotonMenu("Auditoría");
        
        sidebar.add(btnAuditoria);
        sidebar.add(btnInicio);
        sidebar.add(btnUsuarios);
        sidebar.add(btnFincas);
        sidebar.add(btnEstanques);
        sidebar.add(Box.createVerticalGlue());

        centerPanel.add(sidebar, BorderLayout.WEST);

        // ===== CONTENIDO (CardLayout) =====
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
            
        // Paneles modulares (por ahora esqueletos, ver abajo)
        UsuariosPanel usuariosPanel = new UsuariosPanel(mainFrame, usuario, false); // false = puede editar
        FincasPanel fincasPanel = new FincasPanel(mainFrame, usuario, false);
        EstanquesPanel estanquesPanel = new EstanquesPanel(mainFrame,usuario,false, false);
        AuditoriaPanel auditoriaPanel = new AuditoriaPanel(conn);
        
        contentPanel.add(auditoriaPanel, "AUDITORIA");
        contentPanel.add(resumen,        "INICIO");
        contentPanel.add(usuariosPanel, "USUARIOS");
        contentPanel.add(fincasPanel, "FINCAS");
        contentPanel.add(estanquesPanel, "ESTANQUES");

        centerPanel.add(contentPanel, BorderLayout.CENTER);

        // Eventos navegación
        btnAuditoria.addActionListener(e -> mostrarVista("AUDITORIA", btnAuditoria));
        btnInicio.addActionListener(e -> mostrarVista("INICIO", btnInicio));
        btnUsuarios.addActionListener(e -> mostrarVista("USUARIOS", btnUsuarios));
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
        btn.setForeground(new Color(0xBBDEFB)); // azul claro
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
                b.setBackground(new Color(0x1565C0)); // azul más claro
            } else {
                b.setForeground(new Color(0xBBDEFB));
                b.setOpaque(false);
                b.setBackground(new Color(0x0D47A1));
            }
        }
        repaint();
    }
}
