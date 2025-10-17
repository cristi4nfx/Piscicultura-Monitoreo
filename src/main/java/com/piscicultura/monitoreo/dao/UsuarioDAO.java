/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.dao;

import com.piscicultura.monitoreo.model.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private final Connection conexion;

    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // =========================================================
    // ===============        AUTH / LOGIN        ==============
    // =========================================================
    /** Valida credenciales y devuelve Admin/Tecnico/Investigador si son correctas. */
    public Usuario validarCredenciales(String nombreUsuario, String contraseña) {
        final String sql = "SELECT id, nombre_usuario, email, contraseña, telefono, rol, activo " +
                           "FROM usuarios WHERE nombre_usuario = ? AND contraseña = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            String hash = encriptarSHA256(contraseña);
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, hash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // usuario o contraseña incorrectos
    }

    // =========================================================
    // ===============        CREATE / READ       ==============
    // =========================================================
    /** Inserta un usuario (admin/tecnico/investigador). */
    public boolean registrarUsuario(String nombreUsuario, String email, String contraseña,
                                    int telefono, String rol, boolean activo) {
        final String sql = "INSERT INTO usuarios (nombre_usuario, email, contraseña, telefono, rol, activo) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            String hash = encriptarSHA256(contraseña);
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, email);
            stmt.setString(3, hash);
            stmt.setInt(4, telefono);
            stmt.setString(5, rol.toUpperCase());
            stmt.setBoolean(6, activo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Devuelve todos los usuarios ordenados por ID. */
    public List<Usuario> listarTodos() {
        final String sql = "SELECT id, nombre_usuario, email, contraseña, telefono, rol, activo " +
                           "FROM usuarios ORDER BY id";
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRowToUsuario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Busca un usuario por ID, o null si no existe. */
    public Usuario obtenerPorId(int id) {
        final String sql = "SELECT id, nombre_usuario, email, contraseña, telefono, rol, activo " +
                           "FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToUsuario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================
    // ===============        UPDATE / DELETE     ==============
    // =========================================================
    /**
     * Actualiza datos del usuario. Si {@code nuevaContrasena} es null o vacía, NO se modifica la contraseña.
     * @return true si actualizó al menos 1 fila.
     */
    public boolean actualizarUsuario(int id, String nombreUsuario, String email,
                                     int telefono, String rol, boolean activo,
                                     String nuevaContrasena) {

        final boolean cambiaPass = nuevaContrasena != null && !nuevaContrasena.isBlank();

        final String sql = cambiaPass
                ? "UPDATE usuarios SET nombre_usuario=?, email=?, telefono=?, rol=?, activo=?, contraseña=? WHERE id=?"
                : "UPDATE usuarios SET nombre_usuario=?, email=?, telefono=?, rol=?, activo=? WHERE id=?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nombreUsuario);
            ps.setString(i++, email);
            ps.setInt(i++, telefono);
            ps.setString(i++, rol.toUpperCase());
            ps.setBoolean(i++, activo);
            if (cambiaPass) {
                ps.setString(i++, encriptarSHA256(nuevaContrasena));
            }
            ps.setInt(i, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Elimina por ID. Devuelve true si borró alguna fila. */
    public boolean eliminarPorId(int id) {
        final String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // ======================  HELPERS  ========================
    // =========================================================
    /** Construye la subclase correcta (Admin/Tecnico/Investigador) a partir del ResultSet. */
    private Usuario mapRowToUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre_usuario");
        String email = rs.getString("email");
        String passHash = safeGet(rs, "contraseña"); // puede ser null si columna no está seleccionada
        int telefono = rs.getInt("telefono");
        boolean activo = rs.getBoolean("activo");

        String rolStr = rs.getString("rol");
        Rol rolEnum = Rol.valueOf(rolStr.toUpperCase());

        switch (rolEnum) {
            case ADMIN:
                return new Administrador(id, nombre, email, passHash, telefono, activo);
            case TECNICO:
                return new Tecnico(id, nombre, email, passHash, telefono, activo);
            case INVESTIGADOR:
                return new Investigador(id, nombre, email, passHash, telefono, activo);
            default:
                // Por seguridad, aunque el enum ya validó antes
                throw new IllegalArgumentException("Rol desconocido: " + rolStr);
        }
    }

    /** Evita NPE si la columna no fue seleccionada en el SELECT. */
    private String safeGet(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (SQLException ignored) {
            return null;
        }
    }

    /** 🔐 Encripta contraseñas con SHA-256 (misma lógica que ya usabas). */
    private String encriptarSHA256(String contraseña) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(contraseña.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }
}
