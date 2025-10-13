/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.dao;

/**
 *
 * @author Cristian
 */


import com.piscicultura.monitoreo.model.*;
import com.piscicultura.monitoreo.util.ConexionDB;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UsuarioDAO {
    private Connection conexion;

    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Valida las credenciales del usuario.
     * Devuelve un objeto Admin, Tecnico o Investigador si las credenciales son correctas.
     */
    public Usuario validarCredenciales(String nombreUsuario, String contraseña) {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contraseña = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            String hash = encriptarSHA256(contraseña); // 🔒 Encriptar antes de comparar
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, hash);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                int telefono = rs.getInt("telefono");
                boolean activo = rs.getBoolean("activo");
                String rol = rs.getString("rol");

                switch (rol.toUpperCase()) {
                    case "ADMIN":
                        return new Administrador(id, nombreUsuario, email, hash, telefono, activo);
                    case "TECNICO":
                        return new Tecnico(id, nombreUsuario, email, hash, telefono, activo);
                    case "INVESTIGADOR":
                        return new Investigador(id, nombreUsuario, email, hash, telefono, activo);
                    default:
                        System.out.println("⚠️ Rol desconocido: " + rol);
                        return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // ❌ Usuario o contraseña incorrectos
    }

    /**
     * Registra un nuevo usuario (usado por el administrador).
     */
    public boolean registrarUsuario(String nombreUsuario, String email, String contraseña,
                                    int telefono, String rol, boolean activo) {
        String sql = "INSERT INTO usuarios (nombre_usuario, email, contraseña, telefono, rol, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            String hash = encriptarSHA256(contraseña);
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, email);
            stmt.setString(3, hash);
            stmt.setInt(4, telefono);
            stmt.setString(5, rol.toUpperCase());
            stmt.setBoolean(6, activo);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 🔐 Encripta contraseñas con SHA-256
     */
    private String encriptarSHA256(String contraseña) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(contraseña.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }
}
