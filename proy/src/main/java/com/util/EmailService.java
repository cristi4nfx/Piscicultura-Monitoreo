/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.util;

/**
 *
 * @author Cristian
 */

import java.io.InputStream;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    private final String username;
    private final String password;
    private final Properties props;

    public EmailService() {
        try {
            Properties config = new Properties();
            try (InputStream in = getClass().getResourceAsStream("/com/piscicultura/monitoreo/db.properties")) {
                config.load(in);
            }
            
            this.username = config.getProperty("email.sender");
            this.password = config.getProperty("email.password");
            
            props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            
        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración de email", e);
        }
    }

    public boolean enviarCodigoRecuperacion(String emailDestino, String codigo) {
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Código de Recuperación - Sistema Piscicultura");
            
            String contenido = "<html>"
                    + "<body style='font-family: Arial, sans-serif;'>"
                    + "<h2 style='color: #2c3e50;'>Recuperación de Contraseña</h2>"
                    + "<p>Has solicitado restablecer tu contraseña en el Sistema de Monitoreo de Piscicultura.</p>"
                    + "<div style='background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;'>"
                    + "<h3 style='color: #2c3e50; margin: 0;'>Tu código de verificación:</h3>"
                    + "<p style='font-size: 24px; font-weight: bold; color: #e74c3c; margin: 10px 0;'>" + codigo + "</p>"
                    + "</div>"
                    + "<p>Este código expirará en 1 hora.</p>"
                    + "<p>Si no solicitaste este cambio, por favor ignora este mensaje.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee;'>"
                    + "<p style='color: #7f8c8d; font-size: 12px;'>Sistema de Monitoreo de Piscicultura</p>"
                    + "</body>"
                    + "</html>";
            
            message.setContent(contenido, "text/html; charset=utf-8");
            
            Transport.send(message);
            System.out.println("✅ Email enviado exitosamente a: " + emailDestino);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Generar código de 6 dígitos
    public static String generarCodigo() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
}
