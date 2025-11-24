// com.mycompany.proy.MainFrame
package com.mycompany.proy;

import com.mycompany.model.Usuario;
import com.mycompany.vista.DashboardAdminPanel;
import com.mycompany.vista.DashboardInvestigadorPanel;
import com.mycompany.vista.DashboardTecnicoPanel;
import com.mycompany.vista.LoginView;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {


    public MainFrame() {
        setTitle("Sistema de Monitoreo de Piscicultura");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null); // centrar
        setLayout(new BorderLayout());

        LoginView loginView = new LoginView(this);
        add(loginView, BorderLayout.CENTER);
    }

    public void mostrarDashboard(Usuario usuario) throws Exception {
        getContentPane().removeAll();

        JPanel panelDashboard;

        switch (usuario.getRol().name().toUpperCase()) {
            case "ADMIN":
                panelDashboard = new DashboardAdminPanel(this, usuario);
                break;
            case "TECNICO":
                panelDashboard = new DashboardTecnicoPanel(this, usuario);
                break;
            case "INVESTIGADOR":
                panelDashboard = new DashboardInvestigadorPanel(this, usuario);
                break;
            default:
                panelDashboard = new JPanel();
                panelDashboard.add(new JLabel("Rol no reconocido."));
        }

        add(panelDashboard, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void volverAlLogin() {
        getContentPane().removeAll();
        add(new LoginView(this), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

}
