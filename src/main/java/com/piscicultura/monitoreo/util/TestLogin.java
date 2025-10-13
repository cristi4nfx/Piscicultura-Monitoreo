/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.util;

/**
 *
 * @author Cristian
 */


import com.piscicultura.monitoreo.dao.UsuarioDAO;
import com.piscicultura.monitoreo.model.Usuario;

import java.sql.Connection;

public class TestLogin {
    public static void main(String[] args) {
        try {
            Connection conexion = ConexionDB.getConnection();
            UsuarioDAO usuarioDAO = new UsuarioDAO(conexion);

            // 🔹 Solo la primera vez que lo pruebes, descomenta para registrar un usuario
            // usuarioDAO.registrarUsuario("admin1", "admin@correo.com", "12345", 311555444, "ADMIN", true);

            Usuario usuario = usuarioDAO.validarCredenciales("Admin Principal", "12345");

            if (usuario != null) {
                System.out.println("✅ Bienvenido, " + usuario.getNombre() + " (" + usuario.getRol() + ")");
            } else {
                System.out.println("❌ Credenciales incorrectas.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Muestra el error si algo falla
        }
    }
}

