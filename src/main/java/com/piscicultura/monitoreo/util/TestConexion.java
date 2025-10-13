/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.util;

/**
 *
 * @author Cristian
 */

import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        try {
            Connection conn = ConexionDB.getConnection();
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a la base de datos.");
            } else {
                System.out.println("❌ Error al conectar a la base de datos.");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Ocurrió un error al probar la conexión:");
            e.printStackTrace();
        }
    }
}
