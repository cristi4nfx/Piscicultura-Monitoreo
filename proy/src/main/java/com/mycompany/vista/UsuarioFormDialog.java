package com.mycompany.vista;

import com.mycompany.model.Rol;
import com.mycompany.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class UsuarioFormDialog extends JDialog {

    private JTextField txtNombre;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JComboBox<String> cbRol;
    private JCheckBox chkActivo;
    private JPasswordField txtPassword;

    private boolean confirmado = false;
    private final boolean esNuevo;

    public UsuarioFormDialog(Window parent, Usuario usuarioEditar) {
        super(parent,
                (usuarioEditar == null ? "Nuevo usuario" : "Editar usuario"),
                ModalityType.APPLICATION_MODAL);

        this.esNuevo = (usuarioEditar == null);

        initUI();
        if (usuarioEditar != null) {
            cargarDatos(usuarioEditar);
        }

        pack();
        setMinimumSize(new Dimension(420, 280));
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Nombre usuario
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Nombre de usuario:"), gbc);
        txtNombre = new JTextField(20);
        gbc.gridx = 1;
        form.add(txtNombre, gbc);
        row++;

        // Email
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        form.add(txtEmail, gbc);
        row++;

        // Teléfono
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Teléfono:"), gbc);
        txtTelefono = new JTextField(15);
        gbc.gridx = 1;
        form.add(txtTelefono, gbc);
        row++;

        // Rol
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Rol:"), gbc);
        cbRol = new JComboBox<>(new String[]{"ADMIN", "TECNICO", "INVESTIGADOR"});
        gbc.gridx = 1;
        form.add(cbRol, gbc);
        row++;

        // Activo
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Activo:"), gbc);
        chkActivo = new JCheckBox("Usuario habilitado");
        chkActivo.setSelected(true);
        gbc.gridx = 1;
        form.add(chkActivo, gbc);
        row++;

        // Password
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel(esNuevo ? "Contraseña:" : "Nueva contraseña:"), gbc);
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        form.add(txtPassword, gbc);
        row++;

        if (!esNuevo) {
            gbc.gridx = 1; gbc.gridy = row;
            JLabel lblHint = new JLabel("<html><i>Deja en blanco si no quieres cambiarla.</i></html>");
            lblHint.setForeground(new Color(0x607D8B));
            form.add(lblHint, gbc);
            row++;
        }

        content.add(form, BorderLayout.CENTER);

        // BOTONES
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");

        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        btnGuardar.addActionListener(e -> onGuardar());

        buttons.add(btnCancelar);
        buttons.add(btnGuardar);

        content.add(buttons, BorderLayout.SOUTH);
    }

    private void cargarDatos(Usuario u) {
        txtNombre.setText(u.getNombre());
        txtEmail.setText(u.getEmail());
        txtTelefono.setText(String.valueOf(u.getTelefono()));
        Rol rol = u.getRol();
        cbRol.setSelectedItem(rol.name());
        chkActivo.setSelected(u.isActivo());
    }

    private void onGuardar() {
        // Validaciones mínimas
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario es obligatorio.");
            return;
        }
        if (txtEmail.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El email es obligatorio.");
            return;
        }
        int tel;
        try {
            tel = Integer.parseInt(txtTelefono.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El teléfono debe ser numérico.");
            return;
        }

        if (esNuevo && txtPassword.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria para un usuario nuevo.");
            return;
        }

        confirmado = true;
        setVisible(false);
        dispose();
    }

    // ===== GETTERS PARA EL PANEL =====

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getNombreUsuario() {
        return txtNombre.getText().trim();
    }

    public String getEmail() {
        return txtEmail.getText().trim();
    }

    public int getTelefono() {
        try {
            return Integer.parseInt(txtTelefono.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getRol() {
        return (String) cbRol.getSelectedItem();
    }

    public boolean isActivo() {
        return chkActivo.isSelected();
    }

    /** Devuelve la contraseña (puede ser null o "") */
    public String getPassword() {
        char[] pass = txtPassword.getPassword();
        return (pass == null || pass.length == 0) ? "" : new String(pass);
    }
}
