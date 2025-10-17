/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.dao;

import com.piscicultura.monitoreo.model.GranjaPiscicola;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Granja_PiscicolaDAO {

    private final Connection conexion;

    public Granja_PiscicolaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // =========================================================
    // ===================   CREATE / READ   ===================
    // =========================================================

    /** Inserta una finca (granja) y setea el id generado en el objeto. */
    public boolean insertar(GranjaPiscicola finca) {
        final String sql = "INSERT INTO fincas (nombre, ubicacion, longitud, area_total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, finca.getNombre());
            ps.setString(2, finca.getUbicacion());
            ps.setFloat(3, finca.getLongitud());
            ps.setFloat(4, finca.getAreaTotal());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        finca.setIdGranja(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Devuelve todas las fincas ordenadas por ID ascendente. */
    public List<GranjaPiscicola> listarTodos() {
        final String sql = "SELECT id, nombre, ubicacion, longitud, area_total FROM fincas ORDER BY id";
        List<GranjaPiscicola> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRowToFinca(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Busca una finca por su ID, o null si no existe. */
    public GranjaPiscicola obtenerPorId(int id) {
        final String sql = "SELECT id, nombre, ubicacion, longitud, area_total FROM fincas WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToFinca(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================
    // ===================   UPDATE / DELETE  ==================
    // =========================================================

    /**
     * Actualiza una finca completa por ID.
     * @return true si actualizó al menos una fila.
     */
    public boolean actualizar(GranjaPiscicola finca) {
        final String sql = "UPDATE fincas SET nombre=?, ubicacion=?, longitud=?, area_total=? WHERE id=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, finca.getNombre());
            ps.setString(2, finca.getUbicacion());
            ps.setFloat(3, finca.getLongitud());
            ps.setFloat(4, finca.getAreaTotal());
            ps.setInt(5, finca.getIdGranja());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Elimina una finca por ID. Retorna true si se borró alguna fila. */
    public boolean eliminarPorId(int id) {
        final String sql = "DELETE FROM fincas WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // ======================   HELPERS   ======================
    // =========================================================

    private GranjaPiscicola mapRowToFinca(ResultSet rs) throws SQLException {
        GranjaPiscicola g = new GranjaPiscicola();
        g.setIdGranja(rs.getInt("id"));
        g.setNombre(rs.getString("nombre"));
        g.setUbicacion(rs.getString("ubicacion"));
        g.setLongitud(rs.getFloat("longitud"));
        g.setAreaTotal(rs.getFloat("area_total"));
        return g;
    }
}
