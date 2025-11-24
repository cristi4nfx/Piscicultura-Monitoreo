package com.mycompany.vista;

import com.mycompany.dao.SensorDAO;
import com.mycompany.model.Sensor;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class AsignarSensorDialog extends JDialog {

    private final SensorDAO sensorDAO;
    private final int idEstanque;

    private JComboBox<Sensor> cboSensores;

    public AsignarSensorDialog(Window owner, Connection conn, int idEstanque) throws Exception {
        super(owner, "Asignar sensor a estanque #" + idEstanque, ModalityType.APPLICATION_MODAL);
        this.sensorDAO = new SensorDAO(conn);
        this.idEstanque = idEstanque;
        initUI();
        cargarSensores();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(new JLabel("Sensor disponible:"), BorderLayout.NORTH);

        cboSensores = new JComboBox<>();
        center.add(cboSensores, BorderLayout.CENTER);

        content.add(center, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAsignar = new JButton("Asignar");

        btnCancelar.addActionListener(e -> dispose());
        btnAsignar.addActionListener(e -> asignar());

        botones.add(btnCancelar);
        botones.add(btnAsignar);
        content.add(botones, BorderLayout.SOUTH);
    }

    private void cargarSensores() throws Exception {
        cboSensores.removeAllItems();
        List<Sensor> libres = sensorDAO.listarDisponibles();
        for (Sensor s : libres) {
            cboSensores.addItem(s);
        }
        if (libres.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay sensores disponibles para asignar.",
                    "Sin sensores",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void asignar() {
        Sensor sel = (Sensor) cboSensores.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un sensor.",
                    "Sensor requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = sensorDAO.asignarASestanque(sel.getIdSensor(), idEstanque);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Sensor asignado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo asignar el sensor.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error asignando sensor: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
