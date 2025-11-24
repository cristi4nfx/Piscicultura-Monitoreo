package com.mycompany.dao;


import com.mycompany.model.Especie;
import com.mycompany.model.Estanque;
import com.mycompany.model.Parametro;
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
import java.util.Locale;
import java.util.Map;



public class EstanqueDAO {

    private final Connection conn;

    public EstanqueDAO(Connection conn) {
        this.conn = conn;
    }

// ============================
// CREATE estanque (SIN temp/ph/ox/amon)
// ============================
public int insertar(Estanque e, int idFinca) throws SQLException {
    final String sql = """
        INSERT INTO estanques (
            id_finca, tipo, estado, capacidad, id_especie
        )
        VALUES (?, ?, ?, ?, ?)
        RETURNING id_estanque
    """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, idFinca);
        ps.setString(2, e.getTipo());
        ps.setString(3, e.getEstado());
        ps.setFloat(4, e.getCapacidad());

        // id_especie (puede ser null si no eligieron especie)
        Integer idEsp = null;
        if (e.getEspecies() != null && !e.getEspecies().isEmpty()
                && e.getEspecies().get(0) != null) {
            idEsp = e.getEspecies().get(0).getIdEspecie();
        }
        if (idEsp != null) {
            ps.setInt(5, idEsp);
        } else {
            ps.setNull(5, Types.INTEGER);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int idGen = rs.getInt(1);
                e.setIdEstanque(idGen);
                return idGen;
            }
        }
    }
    return 0;
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
                            esp.setParametros(dedupParametros(parametros));

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
    
    private static List<Parametro> dedupParametros(List<Parametro> in) {
        if (in == null) return new ArrayList<>();
        Map<String, Parametro> map = new LinkedHashMap<>();
        for (Parametro p : in) {
            String key = (p.getNombre() + "|" + p.getUnidad()).toLowerCase(Locale.ROOT);
            map.putIfAbsent(key, p); // conserva el primero, descarta repetidos lógicos
        }
        return new ArrayList<>(map.values());
    }

// ============================
// UPDATE (solo tipo / estado / capacidad)
// ============================
public boolean actualizar(Estanque e) throws SQLException {
    final String sql = """
        UPDATE estanques
           SET tipo = ?, estado = ?, capacidad = ?
         WHERE id_estanque = ?
    """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, e.getTipo());
        ps.setString(2, e.getEstado());
        ps.setFloat(3, e.getCapacidad());
        ps.setInt(4, e.getIdEstanque());
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
    
    public List<Estanque> listarTodosConEspecies() throws SQLException {
    String sql = """
        SELECT 
            e.id_estanque, e.id_finca, e.tipo, e.estado, e.capacidad,
            e.temperatura_agua, e.ph_agua, e.oxigeno, e.amoniaco,
            s.id_especie, s.nombre_cientifico, s.nombre_comun, 
            s.edad_dias, s.peso_promedio_g, s.fecha_siembra, s.cantidad
        FROM estanques e
        LEFT JOIN especie s ON s.id_especie = e.id_especie
        ORDER BY e.id_estanque
    """;

    List<Estanque> out = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            Estanque est = new Estanque();
            est.setIdEstanque(rs.getInt("id_estanque"));
            est.setTipo(nullSafe(rs.getString("tipo")));
            est.setEstado(nullSafe(rs.getString("estado")));
            est.setCapacidad(rs.getFloat("capacidad"));
            est.setTemperaturaAgua(rs.getFloat("temperatura_agua"));


            // ===== Especie asociada (si existe) =====
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

                est.setEspecies(new ArrayList<>(List.of(esp)));

            } else {
                est.setEspecies(new ArrayList<>()); // sin especie
            }

            out.add(est);
        }
    }

    return out;
}


/** Devuelve el nombre de la finca a la que pertenece el estanque, o null si no existe. */
public String obtenerNombreFincaPorEstanque(int idEstanque) throws SQLException {
    final String sql = """
        SELECT f.nombre
        FROM estanques e
        JOIN fincas f ON f.id = e.id_finca
        WHERE e.id_estanque = ?
    """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, idEstanque);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("nombre");
            }
        }
    }
    return null;
}
// Cuenta el número de estanques monitoreados en el sistema
public int contarMonitoreados() throws Exception {
    java.util.List<com.mycompany.model.Estanque> estanques = listarTodosConEspecies();
    if (estanques == null) {
        return 0;
    }
    return estanques.size();
}

/**
 * Calcula un porcentaje de "salud promedio" de los lotes
 * usando las mediciones de pH de las últimas 24 horas.
 *
 * - Ideal pH = 7.0
 * - Si el promedio está a 0 unidades de 7  -> 100%
 * - Si el promedio está a 2 o más unidades -> 0%
 * - Se interpola linealmente entre 0 y 2 unidades.
 *
 * Requiere la tabla:
 *   lotes_ph_historial(id_lote, ph, medido_at TIMESTAMP)
 */
public double calculoSaludPromedio() throws java.sql.SQLException {
    String sql = """
            SELECT AVG(ph) AS avg_ph
            FROM lotes_ph_historial
            WHERE medido_at >= NOW() - INTERVAL '24 hours'
            """;

    double avgPh = 7.0; // valor neutro por defecto

    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
         java.sql.ResultSet rs = ps.executeQuery()) {

        if (rs.next() && rs.getBigDecimal("avg_ph") != null) {
            avgPh = rs.getBigDecimal("avg_ph").doubleValue();
        }
    }

    // Heurística simple: distancia al pH ideal 7.0
    double diff = Math.abs(avgPh - 7.0); // distancia al ideal
    // diff >= 2 -> 0% ; diff = 0 -> 100%
    double salud = 100.0 * Math.max(0.0, 2.0 - diff) / 2.0;

    // Redondeamos a entero (0–100) pero devolvemos double por si luego
    // quieres más precisión.
    return Math.round(salud);
}



    private String nullSafe(String s) { return (s == null) ? "" : s; }
}


