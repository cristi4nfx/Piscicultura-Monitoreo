package com.mycompany.vista;

import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.dao.AuditoriaDAO;
import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// ==============================
//   FORMULARIO DE FINCA
// ==============================
class FincaFormDialog extends JDialog {

    private final Connection conn;
    private final Granja_PiscicolaDAO fincaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final Usuario usuarioActual;

    private GranjaPiscicola finca; // null = nueva, != null = editar
    private final Runnable onSaved;

    private JTextField txtNombre;
    private JTextField txtAltitud;
    private JTextField txtArea;

    private JComboBox<String> cboDepartamento;
    private JComboBox<String> cboMunicipio;

    // Mapas nombre -> id
    private final java.util.Map<String, Integer> deptIdByName = new java.util.HashMap<>();
    private final java.util.Map<String, Long> muniIdByName = new java.util.HashMap<>();

    public FincaFormDialog(Window owner,
                           Connection conn,
                           Granja_PiscicolaDAO fincaDAO,
                           AuditoriaDAO auditoriaDAO,
                           Usuario usuarioActual,
                           GranjaPiscicola fincaEditar,
                           Runnable onSaved) {
        super(owner,
                (fincaEditar == null ? "Nueva finca" : "Editar finca"),
                ModalityType.APPLICATION_MODAL);
        this.conn = conn;
        this.fincaDAO = fincaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.usuarioActual = usuarioActual;
        this.finca = fincaEditar;
        this.onSaved = onSaved;

        initUI();
        cargarDepartamentos();

        if (finca != null) {
            precargarDatos();
        }

        pack();
        setMinimumSize(new Dimension(520, 280));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        txtNombre = new JTextField(25);
        txtAltitud = new JTextField(10);
        txtArea = new JTextField(10);
        cboDepartamento = new JComboBox<>();
        cboMunicipio = new JComboBox<>();
        cboMunicipio.setEnabled(false);

        // Nombre
        form.add(new JLabel("Nombre de la finca:"), gbc);
        gbc.gridx = 1;
        form.add(txtNombre, gbc);

        // Departamento
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Departamento:"), gbc);
        gbc.gridx = 1;
        form.add(cboDepartamento, gbc);

        // Municipio
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Municipio:"), gbc);
        gbc.gridx = 1;
        form.add(cboMunicipio, gbc);

        // Altitud
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Altitud (m):"), gbc);
        gbc.gridx = 1;
        form.add(txtAltitud, gbc);

        // Área total
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Área total (m²):"), gbc);
        gbc.gridx = 1;
        form.add(txtArea, gbc);

        content.add(form, BorderLayout.CENTER);

        // Listener departamento -> carga municipios
        cboDepartamento.addActionListener(e -> onDepartamentoSeleccionado());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(this::onGuardar);
        btnCancelar.addActionListener(e -> dispose());

        botones.add(btnCancelar);
        botones.add(btnGuardar);

        content.add(botones, BorderLayout.SOUTH);
    }

    // ==============================
    //   CARGAR COMBOS DESDE BD
    // ==============================
    private void cargarDepartamentos() {
        deptIdByName.clear();
        cboDepartamento.removeAllItems();

        final String sql = "SELECT id, nombre FROM departamentos ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                deptIdByName.put(nombre, id);
                cboDepartamento.addItem(nombre);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error cargando departamentos: " + e.getMessage(),
                    "Error BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDepartamentoSeleccionado() {
        String nombreDepSel = (String) cboDepartamento.getSelectedItem();
        muniIdByName.clear();
        cboMunicipio.removeAllItems();

        if (nombreDepSel == null) {
            cboMunicipio.setEnabled(false);
            return;
        }

        Integer idDep = deptIdByName.get(nombreDepSel);
        if (idDep == null) {
            cboMunicipio.setEnabled(false);
            return;
        }

        final String sql = "SELECT id, nombre FROM municipios WHERE departamento_id = ? ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDep);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String nombre = rs.getString("nombre");
                    muniIdByName.put(nombre, id);
                    cboMunicipio.addItem(nombre);
                }
            }
            cboMunicipio.setEnabled(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error cargando municipios: " + e.getMessage(),
                    "Error BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==============================
    //   PRECARGAR DATOS EN EDICIÓN
    // ==============================
    private void precargarDatos() {
        txtNombre.setText(finca.getNombre());
        txtAltitud.setText(String.valueOf(finca.getAltitud()));
        txtArea.setText(String.valueOf(finca.getAreaTotal()));

        // finca.getUbicacion() es "Municipio, Departamento"
        String ubic = finca.getUbicacion();
        if (ubic != null && ubic.contains(",")) {
            String[] partes = ubic.split(",");
            String mun = partes[0].trim();
            String dep = partes.length > 1 ? partes[1].trim() : "";

            // seleccionar departamento
            if (!dep.isEmpty()) {
                cboDepartamento.setSelectedItem(dep);
                // esto dispara onDepartamentoSeleccionado() y llena municipios
            }

            // seleccionar municipio después
            SwingUtilities.invokeLater(() -> {
                if (!mun.isEmpty()) {
                    cboMunicipio.setSelectedItem(mun);
                }
            });
        }
    }

    // ==============================
    //   GUARDAR (CREAR / EDITAR)
    // ==============================
    private void onGuardar(ActionEvent evt) {
        String nombreFinca = (txtNombre.getText() == null) ? "" : txtNombre.getText().trim();
        String nombreDepSel = (String) cboDepartamento.getSelectedItem();
        String nombreMunSel = (String) cboMunicipio.getSelectedItem();

        if (nombreFinca.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el nombre de la finca.");
            txtNombre.requestFocus();
            return;
        }
        if (nombreDepSel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un departamento.");
            cboDepartamento.requestFocus();
            return;
        }
        if (nombreMunSel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un municipio.");
            cboMunicipio.requestFocus();
            return;
        }

        Integer departamentoId = deptIdByName.get(nombreDepSel);
        Long municipioId = muniIdByName.get(nombreMunSel);
        if (departamentoId == null) {
            JOptionPane.showMessageDialog(this, "Departamento no reconocido internamente.");
            return;
        }
        if (municipioId == null) {
            JOptionPane.showMessageDialog(this, "Municipio no reconocido internamente.");
            return;
        }

        Float altitud = parseFloatUI(txtAltitud.getText());
        if (altitud == null) {
            JOptionPane.showMessageDialog(this,
                    "Altitud inválida. Usa números (ej: 120.5).");
            txtAltitud.requestFocus();
            return;
        }

        Float areaTotal = parseFloatUI(txtArea.getText());
        if (areaTotal == null) {
            JOptionPane.showMessageDialog(this,
                    "Área total inválida. Usa números (ej: 5000).");
            txtArea.requestFocus();
            return;
        }

        // Igual que en tu JavaFX: "Municipio, Departamento"
        String ubicacion = nombreMunSel + ", " + nombreDepSel;

        try {
            if (finca == null) {
                // ===== CREAR =====
                finca = new GranjaPiscicola();
                finca.setNombre(nombreFinca);
                finca.setUbicacion(ubicacion);
                finca.setAltitud(altitud);
                finca.setAreaTotal(areaTotal);

                boolean ok = fincaDAO.insertar(finca);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo guardar la finca (sin filas afectadas).",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Auditoría: creación
                if (auditoriaDAO != null) {
                    auditoriaDAO.registrarCreacion(
                            "FINCA",
                            finca.getIdGranja(),  // se setea en insertar()
                            usuarioActual,
                            "Creación de finca desde FincaFormDialog",
                            resumenFinca(finca)
                    );
                }

            } else {
                // ===== EDITAR =====
                String antes = resumenFinca(finca);

                finca.setNombre(nombreFinca);
                finca.setUbicacion(ubicacion);
                finca.setAltitud(altitud);
                finca.setAreaTotal(areaTotal);

                boolean ok = fincaDAO.actualizar(finca);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo actualizar la finca.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String despues = resumenFinca(finca);

                if (auditoriaDAO != null) {
                    auditoriaDAO.registrarActualizacion(
                            "FINCA",
                            finca.getIdGranja(),
                            usuarioActual,
                            "Edición de finca desde FincaFormDialog",
                            antes,
                            despues
                    );
                }
            }

            if (onSaved != null) onSaved.run();
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error guardando finca: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Float parseFloatUI(String txt) {
        if (txt == null) return null;
        String s = txt.trim();
        if (s.isEmpty()) return null;
        s = s.replace(',', '.');
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resumenFinca(GranjaPiscicola f) {
        StringBuilder sb = new StringBuilder();
        sb.append("idGranja=").append(f.getIdGranja());
        sb.append(", nombre=").append(f.getNombre());
        sb.append(", ubicacion=").append(f.getUbicacion());
        sb.append(", altitud=").append(f.getAltitud());
        sb.append(", areaTotal=").append(f.getAreaTotal());
        return sb.toString();
    }
}
