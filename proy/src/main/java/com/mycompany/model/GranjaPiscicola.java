/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Cristian
 */

import java.util.ArrayList;
import java.util.List;

public class GranjaPiscicola {
    private int idGranja;
    private String nombre;
    private String ubicacion;
    private float altitud;
    private float areaTotal;
    private List<Estanque> estanques;

    public GranjaPiscicola() {
        this.estanques = new ArrayList<>();
    }

    public GranjaPiscicola(int idGranja, String nombre, String ubicacion, float longitud, float areaTotal) {
        this.idGranja = idGranja;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.altitud = longitud;
        this.areaTotal = areaTotal;
        this.estanques = new ArrayList<>();
    }

    public int getIdGranja() { return idGranja; }
    public void setIdGranja(int idGranja) { this.idGranja = idGranja; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public float getAltitud() { return altitud; }
    public void setAltitud(float longitud) { this.altitud = longitud; }

    public float getAreaTotal() { return areaTotal; }
    public void setAreaTotal(float areaTotal) { this.areaTotal = areaTotal; }

    public List<Estanque> getEstanques() { return estanques; }
    public void setEstanques(List<Estanque> estanques) { this.estanques = estanques; }
    
    @Override
    public String toString() {
        // Ajusta para mostrar lo que prefieras
        String nombre = (this.getNombre() != null && !this.getNombre().isBlank())
                        ? this.getNombre() : ("Finca #" + this.getIdGranja());
        return nombre + " — ID:" + this.getIdGranja();
    }
    

}