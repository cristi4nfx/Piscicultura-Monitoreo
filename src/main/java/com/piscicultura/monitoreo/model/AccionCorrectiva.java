/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.model;

/**
 *
 * @author Cristian
 */

public class AccionCorrectiva {
    private int id_accion;
    private String nombre;
    private Usuario responsable;
    private String pasos_ejecucion;
    private float efectividad;

    public AccionCorrectiva() {}

    public AccionCorrectiva(int id_accion, String nombre, Usuario responsable, String pasos_ejecucion, float efectividad) {
        this.id_accion = id_accion;
        this.nombre = nombre;
        this.responsable = responsable;
        this.pasos_ejecucion = pasos_ejecucion;
        this.efectividad = efectividad;
    }

    public void ejecutar() {
        System.out.println("Ejecutando acción correctiva: " + nombre);
        if (responsable != null)
            System.out.println("Responsable: " + responsable.getNombre());
        System.out.println("Pasos: " + pasos_ejecucion);
    }

    public void modificar_pasos(String nuevosPasos) {
        this.pasos_ejecucion = nuevosPasos;
        System.out.println("Pasos actualizados.");
    }
    
    public int getId_accion() { return id_accion; }
    public void setId_accion(int id_accion) { this.id_accion = id_accion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public String getPasos_ejecucion() { return pasos_ejecucion; }
    public void setPasos_ejecucion(String pasos_ejecucion) { this.pasos_ejecucion = pasos_ejecucion; }

    public float getEfectividad() { return efectividad; }
    public void setEfectividad(float efectividad) { this.efectividad = efectividad; }
}