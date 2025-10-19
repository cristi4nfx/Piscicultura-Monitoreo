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
    public List<Parametro> obtenerParametrosPorEspecie(int idEspecie) {
        List<Parametro> parametros = new ArrayList<>();
        String sql = "SELECT nombre, unidad, rango_min, rango_max FROM parametros_especie WHERE id_especie = ? ORDER BY nombre";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idEspecie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Parametro param = new Parametro();
                    param.setNombre(rs.getString("nombre"));
                    param.setUnidad(rs.getString("unidad"));
                    param.setRangoMin(rs.getFloat("rango_min"));
                    param.setRangoMax(rs.getFloat("rango_max"));
                    parametros.add(param);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo parámetros para especie " + idEspecie + ": " + e.getMessage());
        }
        
        return parametros;
    }
}
