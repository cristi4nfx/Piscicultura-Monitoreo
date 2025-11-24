// com.mycompany.dao.LotesDAO
package com.mycompany.dao;

import com.mycompany.model.Lote;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LotesDAO {

    private final Connection conn;

    public LotesDAO(Connection conn) {
        this.conn = conn;
    }

    private OffsetDateTime toOffset(Timestamp ts) {
        return (ts == null) ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
    }

    public List<Lote> listarPorEstanque(int idEstanque) throws SQLException {
        String sql = """
            SELECT id_lote,
                   id_estanque,
                   id_especie,
                   descripcion,
                   ph_actual,
                   created_at,
                   updated_at,
                   activo
            FROM lotes
            WHERE id_estanque = ?
            ORDER BY id_lote
        """;

        List<Lote> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lote l = new Lote();
                    l.setIdLote(rs.getInt("id_lote"));
                    l.setIdEstanque(rs.getInt("id_estanque"));
                    l.setIdEspecie(rs.getInt("id_especie"));
                    l.setDescripcion(rs.getString("descripcion"));

                    Object ph = rs.getObject("ph_actual");
                    l.setPhActual(ph == null ? null : ((Number) ph).doubleValue());

                    l.setCreatedAt(toOffset(rs.getTimestamp("created_at")));
                    l.setUpdatedAt(toOffset(rs.getTimestamp("updated_at")));
                    l.setActivo(rs.getBoolean("activo"));

                    out.add(l);
                }
            }
        }
        return out;
    }

    public int crear(Lote l) throws SQLException {
        String sql = """
            INSERT INTO lotes (
                id_estanque,
                id_especie,
                descripcion,
                ph_actual,
                activo
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_lote
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, l.getIdEstanque());
            ps.setInt(2, l.getIdEspecie());
            ps.setString(3, l.getDescripcion());

            if (l.getPhActual() == null) {
                ps.setNull(4, Types.NUMERIC);
            } else {
                ps.setDouble(4, l.getPhActual());
            }

            // si viene null lo tratamos como true por defecto
            boolean activo = (l.getActivo() == null) ? true : l.getActivo();
            ps.setBoolean(5, activo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo crear el lote");
    }

    /** Actualiza el pH actual del lote. Dispara trigger que registra histórico en la otra BD. */
    public boolean actualizarPh(int idLote, double nuevoPh) throws SQLException {
        String sql = "UPDATE lotes SET ph_actual = ? WHERE id_lote = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(Double.toString(nuevoPh)));
            ps.setInt(2, idLote);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarDatosBasicos(Lote l) throws SQLException {
        String sql = """
            UPDATE lotes
               SET id_especie = ?, descripcion = ?
             WHERE id_lote = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, l.getIdEspecie());
            ps.setString(3, l.getDescripcion());
            ps.setInt(4, l.getIdLote());
            return ps.executeUpdate() > 0;
        }
    }

    /** “Eliminar” físico del lote. (Si luego quieres soft-delete, aquí podrías usar activo=false.) */
    public boolean eliminar(int idLote) throws SQLException {
        String sql = "DELETE FROM lotes WHERE id_lote = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            return ps.executeUpdate() > 0;
        }
    }
}
