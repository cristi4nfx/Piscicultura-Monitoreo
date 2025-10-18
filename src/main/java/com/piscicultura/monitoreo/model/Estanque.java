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

public class Estanque {
    private int idEstanque;
    private String tipo;
    private String estado;
    private float capacidad;
    private float temperaturaAgua;
    private float phAgua;
    private float oxigeno;
    private float amoniaco;
    private List<Especie> especies;
    private List<Sensor> sensores;

    public Estanque() {
        this.especies = new ArrayList<>();
        this.sensores = new ArrayList<>();
    }

    public Estanque(int idEstanque, String tipo, String estado, float capacidad, String observacion) {
        this();
        this.idEstanque = idEstanque;
        this.tipo = tipo;
        this.estado = estado;
        this.capacidad = capacidad;
    }

    public void agregarEspecie(Especie especie) {
        especies.add(especie);
    }

    public void removerEspecie(Especie especie) {
        especies.remove(especie);
    }

    public boolean validarCondiciones(Especie especie) {
        for (Parametro p : especie.getParametros()) {
        }
        return true;
    }

    public void generarAlarma() {
        System.out.println("⚠️ Alarma generada por condiciones fuera del rango.");
    }

    public int getIdEstanque() { return idEstanque; }
    public void setIdEstanque(int idEstanque) { this.idEstanque = idEstanque; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public float getCapacidad() { return capacidad; }
    public void setCapacidad(float capacidad) { this.capacidad = capacidad; }
    
    public float getTemperaturaAgua() { return temperaturaAgua; }
    public void setTemperaturaAgua(float temperaturaAgua) { this.temperaturaAgua = temperaturaAgua; }
    
    public float getPhAgua() { return phAgua; }
    public void setPhAgua(float phAgua) { this.phAgua = phAgua; }

    public float getOxigeno() { return oxigeno; }
    public void setOxigeno(float oxigeno) { this.oxigeno = oxigeno; }

    public float getAmoniaco() { return amoniaco; }
    public void setAmoniaco(float amoniaco) { this.amoniaco = amoniaco; }

    public List<Especie> getEspecies() { return especies; }
 
    public void setEspecies(List<Especie> especies) {
        this.especies = especies != null ? especies : new ArrayList<>();
    }
    
    public List<Sensor> getSensores() { return sensores; }
}
