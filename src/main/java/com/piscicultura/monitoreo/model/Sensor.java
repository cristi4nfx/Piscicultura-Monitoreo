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

public class Sensor {
    private int idSensor;
    private String fabricante;
    private String modelo;
    private String funcion;
    private int intervaloSeg;
    private boolean enLinea;
    private String estado;
    private Date calibracion;

    public Sensor() {}

    public Sensor(int idSensor, String fabricante, String modelo, String funcion,
                  int intervaloSeg, boolean enLinea) {
        this.idSensor = idSensor;
        this.fabricante = fabricante;
        this.modelo = modelo;
        this.funcion = funcion;
        this.intervaloSeg = intervaloSeg;
        this.enLinea = enLinea;
        this.estado = "Activo";
        this.calibracion = new Date(); // fecha actual por defecto
    }

    public boolean requiereCalibracion() {
        long diasDesdeCalibracion = (new Date().getTime() - calibracion.getTime()) / (1000 * 60 * 60 * 24);
        return diasDesdeCalibracion > 30; // requiere calibración cada 30 días
    }

    public float leerValor() {
        return 0;
    }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public int getIdSensor() { return idSensor; }
    public void setIdSensor(int idSensor) { this.idSensor = idSensor; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getFuncion() { return funcion; }
    public void setFuncion(String funcion) { this.funcion = funcion; }

    public int getIntervaloSeg() { return intervaloSeg; }
    public void setIntervaloSeg(int intervaloSeg) { this.intervaloSeg = intervaloSeg; }

    public boolean isEnLinea() { return enLinea; }
    public void setEnLinea(boolean enLinea) { this.enLinea = enLinea; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getCalibracion() { return calibracion; }
    public void setCalibracion(Date calibracion) { this.calibracion = calibracion; }
}
