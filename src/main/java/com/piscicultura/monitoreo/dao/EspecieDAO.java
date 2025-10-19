/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.dao;

/**
 *
 * @author Cristian
 */

import com.piscicultura.monitoreo.model.Parametro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecieDAO {
    private final Connection conexion;

    public EspecieDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /** Obtener todos los parámetros de una especie */
    // EspecieDAO.java
    public List<Parametro> obtenerParametrosPorEspecie(int idEspecie) throws SQLException {
        final String sql = """
            SELECT id, nombre, unidad, rango_min, rango_max
            FROM parametros_especie
            WHERE id_especie = ?
            ORDER BY id
        """;
        List<Parametro> out = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idEspecie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Parametro p = new Parametro();
                    p.setNombre(rs.getString("nombre"));
                    p.setUnidad(rs.getString("unidad"));
                    p.setRangoMin(rs.getFloat("rango_min"));
                    p.setRangoMax(rs.getFloat("rango_max"));
                    out.add(p);
                }
            }
        }
        return out;
    }

}
