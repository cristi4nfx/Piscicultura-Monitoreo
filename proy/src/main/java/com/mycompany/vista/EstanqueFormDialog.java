package com.mycompany.vista;

import com.mycompany.dao.EstanqueDAO;
import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.dao.LotesDAO;
import com.mycompany.dao.AuditoriaDAO;
import com.mycompany.model.Especie;
import com.mycompany.model.Estanque;
import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Lote;
import com.mycompany.model.Usuario;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// =============================
//   DIÁLOGO FORM ESTANQUE
// =============================
class EstanqueFormDialog extends JDialog {

    private final Connection conn;
    private final EstanqueDAO estanqueDAO;
    private final LotesDAO loteDAO;
    private final Granja_PiscicolaDAO fincaDAO;
    private final AuditoriaDAO auditoriaDAO;   // 👈 auditoría
    private final Usuario usuarioActual;       // 👈 quién hace la acción

    private Estanque estanque;            // null = crear, no null = editar
    private final Integer idFincaContext; // si no es null, usamos esa finca
    private final Runnable onSaved;

    // Campos de ESTANQUE
    private JTextField txtTipo;
    private JComboBox<String> cboEstado;
    private JTextField txtCapacidad;

    // Campos de LOTE
    private JTextArea txtDescripcionLote;

    private JComboBox<FincaItem> cboFinca;
    private JComboBox<EspecieItem> cboEspecie;

    // Acciones sobre especies (solo en EDITAR)
    private JPanel panelAccionEspecie;
    private JRadioButton rbNoCambiar;
    private JRadioButton rbAgregar;
    private JRadioButton rbCambiar;

    public EstanqueFormDialog(Window owner,
                              Connection conn,
                              EstanqueDAO estanqueDAO,
                              LotesDAO loteDAO,
                              Granja_PiscicolaDAO fincaDAO,
                              AuditoriaDAO auditoriaDAO,
                              Usuario usuarioActual,
                              Estanque estanqueEditar,
                              Integer idFincaContext,
                              Runnable onSaved) {
        super(owner,
              estanqueEditar == null ? "Nuevo estanque" : "Editar estanque",
              ModalityType.APPLICATION_MODAL);
        this.conn = conn;
        this.estanqueDAO = estanqueDAO;
        this.loteDAO = loteDAO;
        this.fincaDAO = fincaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.usuarioActual = usuarioActual;
        this.estanque = estanqueEditar;
        this.idFincaContext = idFincaContext;
        this.onSaved = onSaved;

        initUI();
        cargarEspecies();

        if (idFincaContext == null) {
            cargarFincas();
        }

        if (estanque == null) {
            // CREAR: no se muestra panel de acciones sobre especie
            panelAccionEspecie.setVisible(false);
            cboEspecie.setEnabled(true);
        } else {
            // EDITAR
            panelAccionEspecie.setVisible(true);
            rbNoCambiar.setSelected(true);   // por defecto, no tocar especies
            cboEspecie.setEnabled(false);    // solo se habilita si elige agregar/cambiar
            precargar();
        }

        pack();
        setMinimumSize(new Dimension(620, 430));
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

        cboFinca = new JComboBox<>();

        // ===== Finca =====
        if (idFincaContext == null) {
            form.add(new JLabel("Finca:"), gbc);
            gbc.gridx = 1;
            form.add(cboFinca, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
        } else {
            JLabel lblFinca = new JLabel("Finca asociada ID: " + idFincaContext);
            gbc.gridwidth = 2;
            form.add(lblFinca, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;
        }

        // ===== Campos de estanque =====
        txtTipo = new JTextField(20);
        cboEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        txtCapacidad = new JTextField(10);

        form.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        form.add(txtTipo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        form.add(cboEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Capacidad (m³):"), gbc);
        gbc.gridx = 1;
        form.add(txtCapacidad, gbc);

        // ===== Especie (para lote / acciones) =====
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Especie:"), gbc);
        gbc.gridx = 1;
        cboEspecie = new JComboBox<>();
        form.add(cboEspecie, gbc);

        // ===== Panel de acciones sobre especie (solo en editar) =====
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Acción sobre especies:"), gbc);
        gbc.gridx = 1;
        panelAccionEspecie = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        rbNoCambiar = new JRadioButton("No modificar especies");
        rbAgregar   = new JRadioButton("Agregar especie");
        rbCambiar   = new JRadioButton("Cambiar especie");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbNoCambiar);
        bg.add(rbAgregar);
        bg.add(rbCambiar);

        // Habilitar/deshabilitar combo de especie según la acción
        rbNoCambiar.addActionListener(e -> cboEspecie.setEnabled(false));
        rbAgregar.addActionListener(e -> cboEspecie.setEnabled(true));
        rbCambiar.addActionListener(e -> cboEspecie.setEnabled(true));

        panelAccionEspecie.add(rbNoCambiar);
        panelAccionEspecie.add(rbAgregar);
        panelAccionEspecie.add(rbCambiar);

        form.add(panelAccionEspecie, gbc);

        // ===== Descripción del lote =====
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Descripción del lote:"), gbc);
        gbc.gridx = 1;
        txtDescripcionLote = new JTextArea(3, 22);
        txtDescripcionLote.setLineWrap(true);
        txtDescripcionLote.setWrapStyleWord(true);
        JScrollPane spDesc = new JScrollPane(txtDescripcionLote);
        spDesc.setPreferredSize(new Dimension(250, 70));
        form.add(spDesc, gbc);

        content.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        content.add(botones, BorderLayout.SOUTH);
    }

    private void cargarFincas() {
        cboFinca.removeAllItems();
        List<GranjaPiscicola> fincas = fincaDAO.listarTodos();
        for (GranjaPiscicola g : fincas) {
            cboFinca.addItem(new FincaItem(g.getIdGranja(), g.getNombre()));
        }
    }

    private void cargarEspecies() {
        cboEspecie.removeAllItems();
        final String sql = """
            SELECT id_especie, nombre_comun, nombre_cientifico
            FROM especie
            ORDER BY nombre_comun
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_especie");
                String nomC = rs.getString("nombre_comun");
                String nomCi = rs.getString("nombre_cientifico");
                String label;
                if (nomC != null && !nomC.isBlank()) {
                    label = nomC + " (" + nomCi + ")";
                } else {
                    label = nomCi;
                }
                cboEspecie.addItem(new EspecieItem(id, label));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error cargando especies: " + e.getMessage(),
                    "Error BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void precargar() {
        txtTipo.setText(estanque.getTipo());
        txtCapacidad.setText(String.valueOf(estanque.getCapacidad()));

        String estado = estanque.getEstado();
        if (estado != null) {
            if (estado.equalsIgnoreCase("Activo")) {
                cboEstado.setSelectedItem("Activo");
            } else if (estado.equalsIgnoreCase("Inactivo")) {
                cboEstado.setSelectedItem("Inactivo");
            }
        }

        if (estanque.getEspecies() != null && !estanque.getEspecies().isEmpty()) {
            int idEsp = estanque.getEspecies().get(0).getIdEspecie();
            for (int i = 0; i < cboEspecie.getItemCount(); i++) {
                if (cboEspecie.getItemAt(i).id == idEsp) {
                    cboEspecie.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void guardar() {
        String tipo = safe(txtTipo.getText());
        String estado = (String) cboEstado.getSelectedItem();
        Float capacidad = parseFloat(txtCapacidad.getText());
        String descLote = safe(txtDescripcionLote.getText());

        if (tipo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el tipo de estanque.");
            txtTipo.requestFocus();
            return;
        }
        if (estado == null || estado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona el estado del estanque.");
            cboEstado.requestFocus();
            return;
        }
        if (capacidad == null) {
            JOptionPane.showMessageDialog(this, "Capacidad inválida.");
            txtCapacidad.requestFocus();
            return;
        }

        // Finca
        Integer idFincaFinal;
        if (idFincaContext != null) {
            idFincaFinal = idFincaContext;
        } else {
            FincaItem sel = (FincaItem) cboFinca.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una finca.");
                return;
            }
            idFincaFinal = sel.id;
        }

        EspecieItem espItem = (EspecieItem) cboEspecie.getSelectedItem();

        try {
            if (estanque == null) {
                // ===== CREAR =====
                if (espItem == null) {
                    JOptionPane.showMessageDialog(this,
                            "Selecciona una especie para el lote inicial.");
                    return;
                }
                int idEsp = espItem.id;

                estanque = new Estanque();
                estanque.setTipo(tipo);
                estanque.setEstado(estado);
                estanque.setCapacidad(capacidad);

                Especie esp = new Especie();
                esp.setIdEspecie(idEsp);
                List<Especie> listaEsp = new ArrayList<>();
                listaEsp.add(esp);
                estanque.setEspecies(listaEsp);

                int idEstanqueGen = estanqueDAO.insertar(estanque, idFincaFinal);
                if (idEstanqueGen <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo insertar el estanque.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                estanque.setIdEstanque(idEstanqueGen);

                // Lote inicial (activo, ph_actual NULL)
                Lote l = new Lote();
                l.setIdEstanque(idEstanqueGen);
                l.setIdEspecie(idEsp);
                l.setDescripcion(descLote.isEmpty() ? null : descLote);
                l.setPhActual(null);
                l.setActivo(true);
                loteDAO.crear(l);

                // === AUDITORÍA: CREACIÓN ===
                if (auditoriaDAO != null) {
                    String despues = resumenEstanque(estanque);
                    auditoriaDAO.registrarCreacion(
                            "ESTANQUE",
                            idEstanqueGen,
                            usuarioActual,
                            "Creación de estanque desde EstanqueFormDialog",
                            despues
                    );
                }

            } else {
                // ===== EDITAR =====
                // Estado ANTES de modificar
                String antes = resumenEstanque(estanque);

                estanque.setTipo(tipo);
                estanque.setEstado(estado);
                estanque.setCapacidad(capacidad);

                boolean ok = estanqueDAO.actualizar(estanque);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo actualizar el estanque.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Luego vemos si hay acción sobre especies
                if (rbAgregar.isSelected() || rbCambiar.isSelected()) {
                    if (espItem == null) {
                        JOptionPane.showMessageDialog(this,
                                "Selecciona una especie para la acción elegida.");
                        return;
                    }
                    int idEsp = espItem.id;

                    // Si ES CAMBIAR ESPECIE:
                    // 1) desactivamos todos los lotes activos actuales del estanque
                    if (rbCambiar.isSelected()) {
                        desactivarLotesActivos(estanque.getIdEstanque());
                    }

                    // 2) creamos el nuevo lote (activo)
                    Lote l = new Lote();
                    l.setIdEstanque(estanque.getIdEstanque());
                    l.setIdEspecie(idEsp);
                    l.setDescripcion(descLote.isEmpty() ? null : descLote);
                    l.setPhActual(null); // lo actualizarán lecturas reales
                    l.setActivo(true);
                    loteDAO.crear(l);

                    // 3) actualizamos especie principal del estanque si es CAMBIAR
                    if (rbCambiar.isSelected()) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE estanques SET id_especie = ? WHERE id_estanque = ?")) {
                            ps.setInt(1, idEsp);
                            ps.setInt(2, estanque.getIdEstanque());
                            ps.executeUpdate();
                        }
                    }
                }

                // Estado DESPUÉS de modificar
                String despues = resumenEstanque(estanque);

                // === AUDITORÍA: EDICIÓN ===
                if (auditoriaDAO != null) {
                    String desc = "Edición de estanque desde EstanqueFormDialog";
                    if (rbAgregar.isSelected()) {
                        desc += " (agregar especie)";
                    } else if (rbCambiar.isSelected()) {
                        desc += " (cambiar especie)";
                    }
                    auditoriaDAO.registrarActualizacion(
                            "ESTANQUE",
                            estanque.getIdEstanque(),
                            usuarioActual,
                            desc,
                            antes,
                            despues
                    );
                }
            }

            if (onSaved != null) onSaved.run();
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error guardando estanque / lote: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Marca como inactivos todos los lotes activos del estanque.
     * Esto se usa cuando el usuario elige "Cambiar especie".
     */
    private void desactivarLotesActivos(int idEstanque) throws SQLException {
        String sql = "UPDATE lotes SET activo = FALSE WHERE id_estanque = ? AND activo = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            ps.executeUpdate();
        }
    }

    private Float parseFloat(String txt) {
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

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    /** Resumen de datos clave del estanque para guardar en la auditoría. */
    private String resumenEstanque(Estanque e) {
        StringBuilder sb = new StringBuilder();
        sb.append("idEstanque=").append(e.getIdEstanque());
        sb.append(", tipo=").append(safe(e.getTipo()));
        sb.append(", estado=").append(safe(e.getEstado()));
        sb.append(", capacidad=").append(e.getCapacidad());

        if (e.getEspecies() != null && !e.getEspecies().isEmpty()) {
            Especie esp = e.getEspecies().get(0);
            if (esp != null) {
                sb.append(", especieId=").append(esp.getIdEspecie());
            }
        }
        if (idFincaContext != null) {
            sb.append(", idFinca=").append(idFincaContext);
        }
        return sb.toString();
    }

    // Helpers para combos
    private static class FincaItem {
        final int id;
        final String nombre;

        FincaItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre + " (ID " + id + ")";
        }
    }

    private static class EspecieItem {
        final int id;
        final String label;

        EspecieItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
