package com.mycompany.dao;

import com.mycompany.model.Auditoria;
import com.mycompany.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuditoriaDAO {

    private final Connection conn;

    public AuditoriaDAO(Connection conn) {
        this.conn = conn;
    }

    // ======================================================
    // MÉTODO GENÉRICO
    // ======================================================
    public void registrar(String entidad,
                          Integer idRegistro,
                          String accion,
                          Usuario usuarioActual,
                          String descripcion,
                          String valoresAntes,
                          String valoresDespues) {

        final String sql = "INSERT INTO auditoria " +
                "(fecha_hora, id_usuario, nombre_usuario, entidad, id_registro, accion, " +
                " descripcion, valores_antes, valores_despues) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));

            if (usuarioActual != null) {
                ps.setInt(2, usuarioActual.getId()); // asumiendo getId()
                ps.setString(3, usuarioActual.getNombre()); // o getNombre()
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setString(3, null);
            }

            ps.setString(4, entidad);
            if (idRegistro != null) {
                ps.setInt(5, idRegistro);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.setString(6, accion);
            ps.setString(7, descripcion);
            ps.setString(8, valoresAntes);
            ps.setString(9, valoresDespues);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            // aquí podrías loguear a archivo si no quieres romper el flujo
        }
    }

    // ======================================================
    // ATALAJOS CÓMODOS
    // ======================================================

    public void registrarCreacion(String entidad,
                                  Integer idRegistro,
                                  Usuario usuario,
                                  String descripcion,
                                  String valoresDespues) {
        registrar(entidad, idRegistro, "CREAR", usuario, descripcion, null, valoresDespues);
    }

    public void registrarActualizacion(String entidad,
                                       Integer idRegistro,
                                       Usuario usuario,
                                       String descripcion,
                                       String valoresAntes,
                                       String valoresDespues) {
        registrar(entidad, idRegistro, "EDITAR", usuario, descripcion, valoresAntes, valoresDespues);
    }

    public void registrarEliminacion(String entidad,
                                     Integer idRegistro,
                                     Usuario usuario,
                                     String descripcion,
                                     String valoresAntes) {
        registrar(entidad, idRegistro, "ELIMINAR", usuario, descripcion, valoresAntes, null);
    }
    
    public java.util.List<Auditoria> listar() {
    java.util.List<Auditoria> lista = new java.util.ArrayList<>();

    final String sql = "SELECT * FROM auditoria ORDER BY fecha_hora DESC";

    try (PreparedStatement ps = conn.prepareStatement(sql);
         java.sql.ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            Auditoria a = new Auditoria();
            // 👇 nombre real de la columna según tu tabla
            a.setId(rs.getInt("id_auditoria"));  
            a.setFechaHora(rs.getTimestamp("fecha_hora"));
            a.setIdUsuario(rs.getInt("id_usuario"));
            a.setNombreUsuario(rs.getString("nombre_usuario"));
            a.setEntidad(rs.getString("entidad"));
            a.setIdRegistro(rs.getInt("id_registro"));
            a.setAccion(rs.getString("accion"));
            a.setDescripcion(rs.getString("descripcion"));
            a.setValoresAntes(rs.getString("valores_antes"));
            a.setValoresDespues(rs.getString("valores_despues"));

            lista.add(a);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

        return lista;
    }
    // Si quieres, puedes luego añadir métodos para listar auditoría, filtrar por entidad, etc.
}
