package com.mycompany.vista;

import com.mycompany.dao.UsuarioDAO;
import com.mycompany.model.Usuario;
import com.mycompany.proy.MainFrame;

import com.util.ConexionDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class UsuariosPanel extends JPanel {

    private final MainFrame mainFrame;
    private final Usuario usuarioSesion;
    private final boolean soloLectura;
    Connection conn;

    private final UsuarioDAO usuarioDAO;

    private JTable tabla;
    private DefaultTableModel modelo;

    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;

    public UsuariosPanel(MainFrame mainFrame, Usuario usuarioSesion, boolean soloLectura) throws Exception {
        this.mainFrame = mainFrame;
        this.usuarioSesion = usuarioSesion;
        this.soloLectura = soloLectura;

        // 🔴 ANTES (mal para tu caso)
        // this.usuarioDAO = new UsuarioDAO(mainFrame.getConexion());

        // ✅ AHORA: igual que haces en LoginView o donde sea que ya te conectas:
        conn = ConexionDB.getConnection();   // <-- usa tu helper real
        this.usuarioDAO = new UsuarioDAO(conn);

        initUI();
        cargarUsuarios();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xECEFF1));

        // ====== ENCABEZADO ======
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        header.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Usuarios del sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");

        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        if (soloLectura) {
            btnNuevo.setEnabled(false);
            btnEditar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ====== TABLA ======
        String[] columnas = {"ID", "Usuario", "Email", "Teléfono", "Rol", "Activo"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo edición vía diálogos
            }
        };

        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // ====== EVENTOS ======
        btnNuevo.addActionListener(e -> onNuevo());
        btnEditar.addActionListener(e -> onEditar());
        btnEliminar.addActionListener(e -> onEliminar());
    }

    // Carga / recarga datos en la tabla
    private void cargarUsuarios() {
        modelo.setRowCount(0);
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        for (Usuario u : usuarios) {
            modelo.addRow(new Object[]{
                    u.getId(),                           // ajusta si tu getter tiene otro nombre
                    u.getNombre(),               // idem
                    u.getEmail(),
                    u.getTelefono(),
                    u.getRol().name(),                  // Rol enum: ADMIN / TECNICO / INVESTIGADOR
                    u.isActivo() ? "Sí" : "No"
            });
        }
    }

    // ==== ACCIONES ====

    private int obtenerIdSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona primero un usuario.",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) modelo.getValueAt(fila, 0); // ID está en la col 0
    }

    private void onNuevo() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        UsuarioFormDialog dlg = new UsuarioFormDialog(parent, null);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!dlg.isConfirmado()) return;

        String nombre = dlg.getNombreUsuario();
        String email = dlg.getEmail();
        int telefono = dlg.getTelefono();
        String rol = dlg.getRol();
        String password = dlg.getPassword();   // obligatorio en "nuevo"
        boolean activo = dlg.isActivo();

        boolean ok = usuarioDAO.registrarUsuario(nombre, email, password,
                telefono, rol, activo);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Usuario creado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEditar() {
        int id = obtenerIdSeleccionado();
        if (id == -1) return;

        Usuario u = usuarioDAO.obtenerPorId(id);
        if (u == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró el usuario seleccionado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        UsuarioFormDialog dlg = new UsuarioFormDialog(parent, u);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!dlg.isConfirmado()) return;

        String nombre = dlg.getNombreUsuario();
        String email = dlg.getEmail();
        int telefono = dlg.getTelefono();
        String rol = dlg.getRol();
        String nuevaPass = dlg.getPassword(); // puede venir vacío → no cambiar
        boolean activo = dlg.isActivo();

        boolean ok = usuarioDAO.actualizarUsuario(
                id, nombre, email, telefono, rol, activo,
                nuevaPass == null || nuevaPass.isBlank() ? null : nuevaPass
        );

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Usuario actualizado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEliminar() {
        int id = obtenerIdSeleccionado();
        if (id == -1) return;

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas eliminar este usuario?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) return;

        boolean ok = usuarioDAO.eliminarPorId(id);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Usuario eliminado.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
