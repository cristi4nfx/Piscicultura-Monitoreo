package com.mycompany.vista;

import com.mycompany.dao.UsuarioDAO;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;
import com.util.ConexionDB;
import com.util.EmailService;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class LoginView extends JPanel {

    private final MainFrame mainFrame;

    // Ya no necesitamos tener EmailService aquí directamente
    // private final EmailService emailService;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnForgotPassword;
    private Connection conexion;
    private UsuarioDAO usuarioDAO;

    public LoginView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ====== PANEL IZQUIERDO (SIDEBAR AZUL) ======
        JPanel leftPanel = new GradientPanel(
                new Color(25, 118, 210), // azul claro
                new Color(13, 71, 161)   // azul más oscuro
        );
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        leftPanel.setPreferredSize(new Dimension(350, 0)); // ancho fijo similar a la imagen

        JLabel lblTitulo = new JLabel("Piscicultura Inteligente");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblSubtitulo = new JLabel("<html>Monitoreo de Ph<br> en tiempo real</html>");
        lblSubtitulo.setForeground(new Color(227, 242, 253));
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        leftPanel.add(lblTitulo);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(lblSubtitulo);
        leftPanel.add(Box.createVerticalGlue());

        // Aquí podrías añadir pequeños iconos de menú como en la imagen:
        // leftPanel.add(iconPanelAbajo);

        // ====== PANEL DERECHO (FONDO BLANCO + CARD CENTRADA) ======
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = createFormCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // para que se mantenga como card compacto
        rightPanel.add(formCard, gbc);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        int y = 0;

        JLabel lblLoginTitle = new JLabel("Iniciar sesión");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLoginTitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = y++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(lblLoginTitle, gbc);

        // Etiqueta correo
        JLabel lblEmail = new JLabel("Usuario");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(lblEmail, gbc);

        // Campo correo
        txtUsuario = new JTextField(20);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsuario.setMargin(new Insets(4, 6, 4, 6));
        gbc.gridy = y++;
        card.add(txtUsuario, gbc);

        // Etiqueta contraseña
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(lblPassword, gbc);

        // Campo contraseña
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setMargin(new Insets(4, 6, 4, 6));
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(txtPassword, gbc);

        // Botón Entrar
        btnLogin = new JButton("Entrar");
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(25, 118, 210));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(new Color(21, 101, 192));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(new Color(25, 118, 210));
            }
        });

        gbc.gridy = y++;
        gbc.insets = new Insets(12, 0, 5, 0);
        card.add(btnLogin, gbc);

        // Link "¿Olvidaste tu contraseña?"
        btnForgotPassword = new JButton("¿Olvidaste tu contraseña?");
        btnForgotPassword.setFocusPainted(false);
        btnForgotPassword.setBorderPainted(false);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setForeground(new Color(25, 118, 210));
        btnForgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgotPassword.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = y++;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnForgotPassword, gbc);

        // Eventos
        btnLogin.addActionListener(e -> {
            try {
                realizarLogin();
            } catch (Exception ex) {
                System.getLogger(LoginView.class.getName())
                        .log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });

        btnForgotPassword.addActionListener(e -> recuperarContrasena());

        return card;
    }

    private void realizarLogin() throws Exception {
        try {
            this.conexion = ConexionDB.getConnection();
            this.usuarioDAO = new UsuarioDAO(conexion);
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos.",
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String user = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (user.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor ingresa el correo y la contraseña.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario = usuarioDAO.validarCredenciales(user, password);

        if (usuario != null) {
            JOptionPane.showMessageDialog(this,
                    "Bienvenido al sistema.",
                    "Acceso permitido",
                    JOptionPane.INFORMATION_MESSAGE);
            mainFrame.mostrarDashboard(usuario);

        } else {
            JOptionPane.showMessageDialog(this,
                    "Correo o contraseña incorrectos.",
                    "Error de autenticación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recuperarContrasena() {
       try {
        this.conexion = ConexionDB.getConnection();
        this.usuarioDAO = new UsuarioDAO(conexion);
        
        RecuperarPasswordDialog dialog = new RecuperarPasswordDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            usuarioDAO
        );
        dialog.setVisible(true);
        
        } catch (Exception e) {
            System.err.println("❌ Error al abrir recuperación: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "No se pudo conectar con la base de datos.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Panel con degradado vertical (para el sidebar azul).
     */
    private static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, color1,
                    0, h, color2
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }
}
