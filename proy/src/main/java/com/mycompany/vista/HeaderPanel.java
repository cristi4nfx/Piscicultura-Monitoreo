// com.mycompany.vista.HeaderPanel
package com.mycompany.vista;

import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private final MainFrame mainFrame;
    private final Usuario usuario;

    public HeaderPanel(MainFrame mainFrame, Usuario usuario) {
        this.mainFrame = mainFrame;
        this.usuario = usuario;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // gris claro
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Izquierda: info usuario
        String texto = "Usuario: " + usuario.getNombre() +
                "    |    Rol: " + usuario.getRol();

        JLabel lblInfo = new JLabel(texto);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Derecha: modo oscuro + cerrar sesión
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Botón cerrar sesión
        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas cerrar sesión?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.volverAlLogin();
            }
        });

        rightPanel.add(btnLogout);

        add(lblInfo, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
}
