package com.piscicultura.monitoreo.dao;

import com.piscicultura.monitoreo.model.Especie;
import com.piscicultura.monitoreo.model.Estanque;
import com.piscicultura.monitoreo.model.Parametro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date; // <-- ¡Este es el correcto!
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;



public class EstanqueDAO {

    private final Connection conn;

    public EstanqueDAO(Connection conn) {
        this.conn = conn;
    }

    // ============================
    // CREATE estanque
    // ============================
    public int insertar(Estanque e, int idFinca) throws SQLException {
        final String sql = """
            INSERT INTO estanques (id_finca, tipo, estado, capacidad, temperatura_agua, ph_agua, oxigeno, amoniaco)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idFinca);
            ps.setString(2, e.getTipo());
            ps.setString(3, e.getEstado());
            ps.setFloat(4, e.getCapacidad());
            ps.setFloat(5, e.getTemperaturaAgua());
            ps.setFloat(6, e.getPhAgua());
            ps.setFloat(7, e.getOxigeno());
            ps.setFloat(8, e.getAmoniaco());

            int affected = ps.executeUpdate();
            if (affected == 0) return 0;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int idGen = keys.getInt(1);
                    e.setIdEstanque(idGen);
                    return idGen;
                }
            }
        }
        return 0;
    }

    // ============================
    // CREATE relación estanque-especie
    // ============================
    public boolean insertarRelacionEstanqueEspecie(int idEstanque, int idEspecie, Integer cantidad, Date fechaSiembra) throws SQLException {
        final String sql = """
            INSERT INTO estanque_especie (id_estanque, id_especie, cantidad, fecha_siembra)
            VALUES (?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            ps.setInt(2, idEspecie);
            if (cantidad == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, cantidad);
            if (fechaSiembra == null) ps.setNull(4, Types.DATE); else ps.setDate(4, fechaSiembra);
            return ps.executeUpdate() > 0;
        }
    }

    // ============================
    // READ: listar por finca (con especies)
    // ============================
    public List<Estanque> listarPorFincaConEspecies(int idFinca) throws SQLException {
        String sql = """
            SELECT e.id_estanque, e.id_finca, e.tipo, e.estado, e.capacidad,
                   e.temperatura_agua, e.ph_agua, e.oxigeno, e.amoniaco,
                   s.id_especie, s.nombre_cientifico, s.nombre_comun, s.edad_dias,
                   s.peso_promedio_g, s.fecha_siembra, s.cantidad
            FROM estanques e
            LEFT JOIN especie s ON s.id_especie = e.id_especie
            WHERE e.id_finca = ?
            ORDER BY e.id_estanque
        """;

        List<Estanque> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFinca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Estanque est = new Estanque();
                    est.setIdEstanque(rs.getInt("id_estanque"));
                    est.setTipo(nullSafe(rs.getString("tipo")));
                    est.setEstado(nullSafe(rs.getString("estado")));
                    est.setCapacidad(rs.getFloat("capacidad"));
                    est.setTemperaturaAgua(rs.getFloat("temperatura_agua"));
                    est.setPhAgua(rs.getFloat("ph_agua"));
                    est.setOxigeno(rs.getFloat("oxigeno"));
                    est.setAmoniaco(rs.getFloat("amoniaco"));

                    // Especie (si existe)
                    Integer idEsp = (Integer) rs.getObject("id_especie");
                    if (idEsp != null) {
                        Especie esp = new Especie();
                        esp.setIdEspecie(idEsp);
                        esp.setNombreCientifico(nullSafe(rs.getString("nombre_cientifico")));
                        esp.setNombreComun(nullSafe(rs.getString("nombre_comun")));
                        esp.setEdadDias(rs.getInt("edad_dias"));
                        esp.setPesoPromedioG(rs.getFloat("peso_promedio_g"));
                        esp.setFechaSiembra(rs.getDate("fecha_siembra"));
                        esp.setCantidad(rs.getInt("cantidad"));

                        // ✅ NUEVO: CARGAR PARÁMETROS DE LA ESPECIE
                        try {
                            EspecieDAO especieDAO = new EspecieDAO(conn);
                            List<Parametro> parametros = especieDAO.obtenerParametrosPorEspecie(idEsp);
                            esp.getParametros().addAll(parametros);

                            System.out.println("✅ Cargados " + parametros.size() + " parámetros para " + esp.getNombreComun());
                        } catch (Exception e) {
                            System.err.println("❌ Error cargando parámetros para especie " + idEsp + ": " + e.getMessage());
                        }

                        est.setEspecies(new ArrayList<>(List.of(esp)));
                    } else {
                        est.setEspecies(new ArrayList<>());
                    }

                    out.add(est);
                }
            }
        }
        return out;
    }

    // ============================
    // UPDATE (estanque)
    // ============================
    public boolean actualizar(Estanque e) throws SQLException {
        final String sql = """
            UPDATE estanques
            SET tipo = ?, estado = ?, capacidad = ?, temperatura_agua = ?, ph_agua = ?, oxigeno = ?, amoniaco = ?
            WHERE id_estanque = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTipo());
            ps.setString(2, e.getEstado());
            ps.setFloat(3, e.getCapacidad());
            ps.setFloat(4, e.getTemperaturaAgua());
            ps.setFloat(5, e.getPhAgua());
            ps.setFloat(6, e.getOxigeno());
            ps.setFloat(7, e.getAmoniaco());
            ps.setInt(8, e.getIdEstanque());
            return ps.executeUpdate() > 0;
        }
    }

    // ============================
    // DELETE (estanque)
    // ============================
    public boolean eliminar(int idEstanque) throws SQLException {
        final String sql = "DELETE FROM estanques WHERE id_estanque = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            return ps.executeUpdate() > 0;
        }
    }

    private String nullSafe(String s) { return (s == null) ? "" : s; }
}
