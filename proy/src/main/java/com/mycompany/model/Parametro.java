/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Cristian
 */

public class Parametro {
    private String nombre;
    private String unidad;
    private float rangoMin;
    private float rangoMax;

    public Parametro() {}

    public Parametro(String nombre, String unidad, float rangoMin, float rangoMax) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public float getRangoMin() { return rangoMin; }
    public void setRangoMin(float rangoMin) { this.rangoMin = rangoMin; }

    public float getRangoMax() { return rangoMax; }
    public void setRangoMax(float rangoMax) { this.rangoMax = rangoMax; }
}