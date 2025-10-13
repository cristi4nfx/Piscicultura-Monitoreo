/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.util;

/**
 *
 * @author Cristian
 */

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionDB {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = ConexionDB.class.getResourceAsStream("/com/piscicultura/monitoreo/db.properties")) {
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("db.url");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo cargar db.properties", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }
}
