/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.model;

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
    private float longitud;
    private float areaTotal;
    private List<Estanque> estanques;

    public GranjaPiscicola() {
        this.estanques = new ArrayList<>();
    }

    public GranjaPiscicola(int idGranja, String nombre, String ubicacion, float longitud, float areaTotal) {
        this.idGranja = idGranja;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.longitud = longitud;
        this.areaTotal = areaTotal;
        this.estanques = new ArrayList<>();
    }

    public String agregarEstanque(Estanque estanque) {
        if (estanque == null) {
            return "No se puede agregar un estanque nulo.";
        }
        estanques.add(estanque);
        return "Estanque agregado correctamente: " + estanque.getIdEstanque();
    }

    public String eliminarEstanque(int idEstanque) {
        for (Estanque e : estanques) {
            if (e.getIdEstanque() == idEstanque) {
                estanques.remove(e);
                return "Estanque eliminado correctamente (ID: " + idEstanque + ")";
            }
        }
        return "No se encontró un estanque con ID: " + idEstanque;
    }

    public int getIdGranja() { return idGranja; }
    public void setIdGranja(int idGranja) { this.idGranja = idGranja; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public float getLongitud() { return longitud; }
    public void setLongitud(float longitud) { this.longitud = longitud; }

    public float getAreaTotal() { return areaTotal; }
    public void setAreaTotal(float areaTotal) { this.areaTotal = areaTotal; }

    public List<Estanque> getEstanques() { return estanques; }
    public void setEstanques(List<Estanque> estanques) { this.estanques = estanques; }


}