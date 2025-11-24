package com.mycompany.vista;

import com.mycompany.dao.EstanqueDAO;
import com.mycompany.dao.Granja_PiscicolaDAO;
import com.mycompany.dao.LotesDAO;
import com.mycompany.model.Estanque;
import com.mycompany.model.GranjaPiscicola;
import com.mycompany.model.Lote;
import com.util.ReporteExcelUtil;
import com.util.ReportePDFUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReporteDialog extends JDialog {

    private final Connection conn;
    private final Granja_PiscicolaDAO fincaDAO;
    private final EstanqueDAO estanqueDAO;
    private final LotesDAO lotesDAO;

    private JComboBox<GranjaPiscicola> cbFincas;
    private JComboBox<Estanque> cbEstanques;
    private JComboBox<Lote> cbLotes;
    private JButton btnGenerarPDF;
    private JButton btnGenerarExcel;

    public ReporteDialog(Frame owner, Connection conn) {
        super(owner, "Generar Reporte", true);
        this.conn = conn;

        this.fincaDAO = new Granja_PiscicolaDAO(conn);
        this.estanqueDAO = new EstanqueDAO(conn);
        this.lotesDAO = new LotesDAO(conn);

        initUI();
        cargarFincasSeguras();

        setSize(480, 330);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new GridLayout(6, 1, 8, 8));
        panelSeleccion.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cbFincas = new JComboBox<>();
        cbEstanques = new JComboBox<>();
        cbLotes = new JComboBox<>();

        cbFincas.addActionListener(e -> {
            GranjaPiscicola f = (GranjaPiscicola) cbFincas.getSelectedItem();
            if (f != null) cargarEstanquesSeguros(f.getIdGranja());
        });

        cbEstanques.addActionListener(e -> {
            Estanque es = (Estanque) cbEstanques.getSelectedItem();
            if (es != null) cargarLotesSeguros(es.getIdEstanque());
        });

        panelSeleccion.add(new JLabel("Finca:"));
        panelSeleccion.add(cbFincas);

        panelSeleccion.add(new JLabel("Estanque:"));
        panelSeleccion.add(cbEstanques);

        panelSeleccion.add(new JLabel("Lote:"));
        panelSeleccion.add(cbLotes);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnGenerarPDF = new JButton("Generar PDF");
        btnGenerarExcel = new JButton("Generar Excel");

        btnGenerarPDF.addActionListener(e -> generarPDF());
        btnGenerarExcel.addActionListener(e -> generarExcel());

        panelBotones.add(btnGenerarPDF);
        panelBotones.add(btnGenerarExcel);

        add(panelSeleccion, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    //  CARGA SEGURA DE FINCAS
    // ------------------------------------------------------------------
    private void cargarFincasSeguras() {
        cbFincas.removeAllItems();

        List<GranjaPiscicola> fincas = fincaDAO.listarTodos();
        for (GranjaPiscicola f : fincas) {
            cbFincas.addItem(f);
        }
    }

    // ------------------------------------------------------------------
    //  CARGA SEGURA DE ESTANQUES
    // ------------------------------------------------------------------
    private void cargarEstanquesSeguros(int idFinca) {
        try {
            cbEstanques.removeAllItems();
            cbLotes.removeAllItems();

            List<Estanque> estanques = estanqueDAO.listarPorFincaConEspecies(idFinca);

            for (Estanque es : estanques) {
                cbEstanques.addItem(es);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error cargando estanques de la finca seleccionada.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------
    //  CARGA SEGURA DE LOTES
    // ------------------------------------------------------------------
    private void cargarLotesSeguros(int idEstanque) {
        try {
            cbLotes.removeAllItems();

            List<Lote> lotes = lotesDAO.listarPorEstanque(idEstanque);

            for (Lote l : lotes) {
                cbLotes.addItem(l);
            }

            // SI NO HAY LOTES → Lote dummy
            if (lotes.isEmpty()) {
                cbLotes.addItem(new Lote(0, "Sin lotes disponibles"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error cargando lotes del estanque seleccionado.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------
    //  BOTONES (AÚN SIN IMPLEMENTAR GENERADOR)
    // ------------------------------------------------------------------
    private void generarPDF() {
        try {
            ReportePDFUtil.generarReporteFincaEstanqueLote(
                    (GranjaPiscicola) cbFincas.getSelectedItem(),
                    (Estanque) cbEstanques.getSelectedItem(),
                    (Lote) cbLotes.getSelectedItem(),
                    conn
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar PDF: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void generarExcel() {
        try {
            ReporteExcelUtil.generarReporteFincaEstanqueLote(
                    (GranjaPiscicola) cbFincas.getSelectedItem(),
                    (Estanque) cbEstanques.getSelectedItem(),
                    (Lote) cbLotes.getSelectedItem(),
                    conn
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar Excel: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
