/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.model;

/**
 *
 * @author Cristian
 */

import java.util.Date;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Alarma implements Notificable {

    // ----- Atributos -----
    private int id_alarma;
    private LocalDateTime fecha_creacion;
    private String mensaje;
    private String parametro_afectado;
    private String severidad;
    private boolean atendida;
    
    private final List<AccionCorrectiva> acciones = new ArrayList<>();
    
    private final Estanque estanque;
    
    Alarma(Estanque estanque, int id_alarma, LocalDateTime fecha_creacion,
           String mensaje, String parametro_afectado, String severidad, boolean atendida) {

        this.estanque = estanque;
        this.id_alarma = id_alarma;
        this.fecha_creacion = fecha_creacion;
        this.mensaje = mensaje;
        this.parametro_afectado = parametro_afectado;
        this.severidad = severidad;
        this.atendida = atendida;
    }
    
    public void alerta() {
        System.out.println("⚠️  [ALARMA] Estanque #" + (estanque != null ? estanque.getIdEstanque() : "?"));
        System.out.println("Mensaje: " + mensaje);
        System.out.println("Parámetro afectado: " + parametro_afectado);
        System.out.println("Severidad: " + severidad);
        System.out.println("Fecha: " + fecha_creacion);
    }
    
    public void asignar_accion(AccionCorrectiva accion) {
        if (accion != null) {
            acciones.add(accion);
            System.out.println("✅ Acción '" + accion.getNombre() + "' asignada a la alarma.");
        }
    }
    
    public void marcar_atendida() {
        this.atendida = true;
        System.out.println("✔️ Alarma marcada como atendida.");
    }
    
    public void determinar_prioridad() {
        int prioridad;
        switch (severidad.toUpperCase()) {
            case "CRITICA" -> prioridad = 1;
            case "ALTA" -> prioridad = 2;
            case "MEDIA" -> prioridad = 3;
            case "BAJA" -> prioridad = 4;
            default -> prioridad = 5;
        }
        System.out.println("Prioridad de la alarma (" + severidad + "): " + prioridad);
    }
    
    @Override
    public void enviar_notificacion(String destino, String cuerpo) {
        System.out.printf("📩 Notificando a %s: %s%n", destino, cuerpo);
    }
    
    public int getId_alarma() { return id_alarma; }
    public void setId_alarma(int id_alarma) { this.id_alarma = id_alarma; }

    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getParametro_afectado() { return parametro_afectado; }
    public void setParametro_afectado(String parametro_afectado) { this.parametro_afectado = parametro_afectado; }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }

    public boolean isAtendida() { return atendida; }
    public void setAtendida(boolean atendida) { this.atendida = atendida; }

    public List<AccionCorrectiva> getAcciones() {
        return Collections.unmodifiableList(acciones);
    }

    public Estanque getEstanque() { return estanque; }
    
    @Override
    public String toString() {
        return "Alarma{" +
                "id_alarma=" + id_alarma +
                ", mensaje='" + mensaje + '\'' +
                ", parametro_afectado='" + parametro_afectado + '\'' +
                ", severidad='" + severidad + '\'' +
                ", atendida=" + atendida +
                ", fecha=" + fecha_creacion +
                '}';
    }
}
