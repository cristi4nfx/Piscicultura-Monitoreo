package com.mycompany.dao;

import com.mycompany.model.Sensor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SensorDAO {

    private final Connection conn;

    public SensorDAO(Connection conn) {
        this.conn = conn;
    }

    private Sensor mapRow(ResultSet rs) throws SQLException {
        Sensor s = new Sensor();
        s.setIdSensor(rs.getInt("id_sensor"));
        s.setCodigo(rs.getString("codigo"));
        s.setTipo(rs.getString("tipo"));
        s.setEstado(rs.getString("estado"));
        s.setDescripcion(rs.getString("descripcion"));
        int idEst = rs.getInt("id_estanque");
        s.setIdEstanque(rs.wasNull() ? null : idEst);
        return s;
    }

    // Sensores libres (sin estanque)
    public List<Sensor> listarDisponibles() throws SQLException {
        String sql = "SELECT * FROM sensores WHERE id_estanque IS NULL AND estado = 'ACTIVO'";
        List<Sensor> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // Sensores asignados a un estanque
    public List<Sensor> listarPorEstanque(int idEstanque) throws SQLException {
        String sql = "SELECT * FROM sensores WHERE id_estanque = ?";
        List<Sensor> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    // Asignar sensor a un estanque
    public boolean asignarASestanque(int idSensor, int idEstanque) throws SQLException {
        String sql = "UPDATE sensores SET id_estanque = ? WHERE id_sensor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            ps.setInt(2, idSensor);
            return ps.executeUpdate() > 0;
        }
    }

    // Desasignar de estanque (dejarlo libre)
    public boolean desasignarDeEstanque(int idSensor) throws SQLException {
        String sql = "UPDATE sensores SET id_estanque = NULL WHERE id_sensor = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSensor);
            return ps.executeUpdate() > 0;
        }
    }

    // Saber si un estanque tiene al menos un sensor activo
    public boolean tieneSensorActivo(int idEstanque) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sensores " +
                     "WHERE id_estanque = ? AND estado = 'ACTIVO'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    // Desasignar TODOS los sensores de un estanque y marcarlos como ACTIVO (disponibles)
    public boolean desasignarSensoresDeEstanque(int idEstanque) throws SQLException {
        String sql = "UPDATE sensores " +
                     "SET id_estanque = NULL, estado = 'ACTIVO' " +
                     "WHERE id_estanque = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstanque);
            int filas = ps.executeUpdate();
            return filas > 0;  // true si al menos un sensor fue liberado
        }
    }

}
