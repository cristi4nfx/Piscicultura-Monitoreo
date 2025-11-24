package com.mycompany.vista;

import com.mycompany.dao.AuditoriaDAO;
import com.mycompany.model.Auditoria;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class AuditoriaPanel extends JPanel {

    private final AuditoriaDAO auditoriaDAO;

    private JTable tabla;
    private JLabel lblEstado;

    public AuditoriaPanel(Connection conn) {
        this.auditoriaDAO = new AuditoriaDAO(conn);
        initUI();
        cargarDatos();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblTitulo = new JLabel("Registro de Auditoría");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        top.add(lblTitulo, BorderLayout.WEST);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarDatos());
        top.add(btnActualizar, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        tabla = new JTable();
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        lblEstado = new JLabel("Listo.");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(lblEstado, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        List<Auditoria> lista = auditoriaDAO.listar();

        String[] columnNames = {"Fecha", "Usuario", "Acción", "Entidad", "ID", "Descripción"};

        Object[][] data = new Object[lista.size()][6];

        for (int i = 0; i < lista.size(); i++) {
            Auditoria a = lista.get(i);

            data[i][0] = a.getFechaHora();
            data[i][1] = a.getNombreUsuario();
            data[i][2] = a.getAccion();
            data[i][3] = a.getEntidad();
            data[i][4] = a.getIdRegistro();
            data[i][5] = a.getDescripcion();
        }

        tabla.setModel(new javax.swing.table.DefaultTableModel(
                data,
                columnNames
        ));

        lblEstado.setText("Mostrando " + lista.size() + " registro(s).");
    }
}
