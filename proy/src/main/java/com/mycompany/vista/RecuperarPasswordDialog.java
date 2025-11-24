package com.mycompany.vista;

import com.mycompany.dao.UsuarioDAO;

import javax.swing.*;
import java.awt.*;

public class RecuperarPasswordDialog extends JDialog {
    
    private final UsuarioDAO usuarioDAO;
    private final JFrame parent;
    
    private JTextField txtUsuarioOEmail;
    private JTextField txtCodigo;
    private JPasswordField txtNuevaPassword;
    private JPasswordField txtConfirmarPassword;
    
    private JButton btnContinuar;
    private JButton btnCancelar;
    
    private JPanel panelStep1;
    private JPanel panelStep2;
    private JPanel panelStep3;
    
    private int currentStep = 1;
    private String emailRecuperacion;
    private String usuarioRecuperacion;

    public RecuperarPasswordDialog(JFrame parent, UsuarioDAO usuarioDAO) {
        super(parent, "Recuperar Contraseña", true);
        this.parent = parent;
        this.usuarioDAO = usuarioDAO;
        initUI();
    }

    private void initUI() {
        setSize(450, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        
        // Header con icono y título
        JPanel headerPanel = createHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel de contenido (aquí irán los 3 pasos)
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        panelStep1 = createStep1();
        panelStep2 = createStep2();
        panelStep3 = createStep3();
        
        contentPanel.add(panelStep1, "step1");
        contentPanel.add(panelStep2, "step2");
        contentPanel.add(panelStep3, "step3");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(25, 118, 210));
        header.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel iconLabel = new JLabel("🔒");
        iconLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Recuperar Contraseña");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sigue los pasos para restablecer tu contraseña");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(227, 242, 253));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        header.add(iconLabel);
        header.add(Box.createVerticalStrut(10));
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitleLabel);
        
        return header;
    }
    
    private JPanel createStep1() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        JLabel stepLabel = new JLabel("Paso 1 de 3");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stepLabel.setForeground(new Color(25, 118, 210));
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Verifica tu identidad");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html>Ingresa tu nombre de usuario o correo electrónico para recuperar tu contraseña.</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(stepLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Campo Usuario o Email
        JLabel lblUsuarioOEmail = new JLabel("Usuario o correo electrónico");
        lblUsuarioOEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuarioOEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtUsuarioOEmail = new JTextField();
        txtUsuarioOEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuarioOEmail.setPreferredSize(new Dimension(350, 35));
        txtUsuarioOEmail.setMaximumSize(new Dimension(350, 35));
        txtUsuarioOEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtUsuarioOEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblUsuarioOEmail);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtUsuarioOEmail);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createStep2() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        JLabel stepLabel = new JLabel("Paso 2 de 3");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stepLabel.setForeground(new Color(25, 118, 210));
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Ingresa el código");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html>Hemos enviado un código de verificación a tu correo electrónico. Revisa tu bandeja de entrada.</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(stepLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Email indicator
        JLabel emailSentLabel = new JLabel("✉ Código enviado a: ");
        emailSentLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
        emailSentLabel.setForeground(new Color(100, 100, 100));
        emailSentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(emailSentLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Campo Código
        JLabel lblCodigo = new JLabel("Código de verificación");
        lblCodigo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCodigo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        txtCodigo.setPreferredSize(new Dimension(350, 45));
        txtCodigo.setMaximumSize(new Dimension(350, 45));
        txtCodigo.setMinimumSize(new Dimension(350, 45));
        txtCodigo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(25, 118, 210)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        txtCodigo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblCodigo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtCodigo);
        panel.add(Box.createVerticalStrut(15));
        
        // Link reenviar código
        JButton btnReenviar = new JButton("¿No recibiste el código? Reenviar");
        btnReenviar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReenviar.setForeground(new Color(25, 118, 210));
        btnReenviar.setBorderPainted(false);
        btnReenviar.setContentAreaFilled(false);
        btnReenviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReenviar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnReenviar.addActionListener(e -> reenviarCodigo());
        
        panel.add(btnReenviar);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createStep3() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        JLabel stepLabel = new JLabel("Paso 3 de 3");
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stepLabel.setForeground(new Color(25, 118, 210));
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Nueva contraseña");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html>Crea una contraseña segura para tu cuenta.</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(stepLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Campo Nueva Contraseña
        JLabel lblNueva = new JLabel("Nueva contraseña");
        lblNueva.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNueva.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtNuevaPassword = new JPasswordField();
        txtNuevaPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNuevaPassword.setPreferredSize(new Dimension(350, 35));
        txtNuevaPassword.setMaximumSize(new Dimension(350, 35));
        txtNuevaPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtNuevaPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblNueva);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtNuevaPassword);
        panel.add(Box.createVerticalStrut(20));
        
        // Campo Confirmar Contraseña
        JLabel lblConfirmar = new JLabel("Confirmar contraseña");
        lblConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConfirmar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtConfirmarPassword.setPreferredSize(new Dimension(350, 35));
        txtConfirmarPassword.setMaximumSize(new Dimension(350, 35));
        txtConfirmarPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtConfirmarPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblConfirmar);
        panel.add(Box.createVerticalStrut(5));
        panel.add(txtConfirmarPassword);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.setForeground(new Color(100, 100, 100));
        btnCancelar.setBackground(Color.WHITE);
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(e -> dispose());
        
        btnContinuar = new JButton("Continuar");
        btnContinuar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setBackground(new Color(25, 118, 210));
        btnContinuar.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        btnContinuar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnContinuar.setFocusPainted(false);
        btnContinuar.addActionListener(e -> handleContinuar());
        
        btnContinuar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnContinuar.setBackground(new Color(21, 101, 192));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnContinuar.setBackground(new Color(25, 118, 210));
            }
        });
        
        panel.add(btnCancelar);
        panel.add(btnContinuar);
        
        return panel;
    }
    
    private void handleContinuar() {
        if (currentStep == 1) {
            verificarUsuarioYEnviarCodigo();
        } else if (currentStep == 2) {
            verificarCodigo();
        } else if (currentStep == 3) {
            cambiarPassword();
        }
    }
    
    private void verificarUsuarioYEnviarCodigo() {
        String usuarioOEmail = txtUsuarioOEmail.getText().trim();
        
        if (usuarioOEmail.isEmpty()) {
            mostrarError("Por favor ingresa tu usuario o correo electrónico.");
            return;
        }
        
        // Determinar si es email o usuario
        boolean esEmail = usuarioOEmail.contains("@");
        String email = null;
        String usuario = null;
        
        if (esEmail) {
            // Es un correo, normalizar a minúsculas
            email = usuarioOEmail.toLowerCase();
            usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
            
            if (usuario == null) {
                mostrarError("No existe ninguna cuenta asociada a este correo.");
                return;
            }
        } else {
            // Es un usuario, buscar el email asociado
            usuario = usuarioOEmail;
            email = usuarioDAO.obtenerEmailPorUsuario(usuario);
            
            if (email == null) {
                mostrarError("No existe ninguna cuenta con este nombre de usuario.");
                return;
            }
        }
        
        // Generar y enviar token
        String resultado = usuarioDAO.generarTokenRecuperacion(email);
        
        if (resultado == null) {
            mostrarError("No se pudo enviar el correo. Verifica la configuración.");
            return;
        }
        
        emailRecuperacion = email;
        usuarioRecuperacion = usuario;
        cambiarPaso(2);
    }
    
    private void verificarCodigo() {
        String codigo = txtCodigo.getText().trim();
        
        if (codigo.isEmpty()) {
            mostrarError("Por favor ingresa el código de verificación.");
            return;
        }
        
        // Verificar que el código sea válido ANTES de avanzar
        // Necesitamos un método en UsuarioDAO que solo valide el token sin cambiar password
        if (!usuarioDAO.validarToken(codigo)) {
            mostrarError("El código ingresado es inválido o ha expirado.\nVerifica e inténtalo de nuevo.");
            return;
        }
        
        // Si el código es válido, avanzar al paso 3
        cambiarPaso(3);
    }
    
    private void cambiarPassword() {
        String nuevaPass = new String(txtNuevaPassword.getPassword());
        String confirmarPass = new String(txtConfirmarPassword.getPassword());
        
        if (nuevaPass.isEmpty() || confirmarPass.isEmpty()) {
            mostrarError("Por favor completa todos los campos.");
            return;
        }
        
        if (!nuevaPass.equals(confirmarPass)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }
        
        if (nuevaPass.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        
        String codigo = txtCodigo.getText().trim();
        boolean cambiado = usuarioDAO.cambiarPasswordConToken(codigo, nuevaPass);
        
        if (cambiado) {
            JOptionPane.showMessageDialog(this,
                "¡Contraseña actualizada correctamente!\nYa puedes iniciar sesión.",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            mostrarError("El código es inválido o ha expirado.");
        }
    }
    
    private void reenviarCodigo() {
        if (emailRecuperacion != null) {
            String resultado = usuarioDAO.generarTokenRecuperacion(emailRecuperacion);
            if (resultado != null) {
                JOptionPane.showMessageDialog(this,
                    "Código reenviado a " + emailRecuperacion,
                    "Código enviado",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                mostrarError("No se pudo reenviar el código.");
            }
        }
    }
    
    private void cambiarPaso(int step) {
        currentStep = step;
        CardLayout cl = (CardLayout) panelStep1.getParent().getLayout();
        
        if (step == 1) {
            cl.show(panelStep1.getParent(), "step1");
            btnContinuar.setText("Continuar");
        } else if (step == 2) {
            cl.show(panelStep1.getParent(), "step2");
            btnContinuar.setText("Verificar código");
            // Actualizar el label con el email
            Component[] components = panelStep2.getComponents();
            for (Component c : components) {
                if (c instanceof JLabel && ((JLabel) c).getText().startsWith("✉")) {
                    ((JLabel) c).setText("✉ Código enviado a: " + emailRecuperacion);
                }
            }
        } else if (step == 3) {
            cl.show(panelStep1.getParent(), "step3");
            btnContinuar.setText("Cambiar contraseña");
        }
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}