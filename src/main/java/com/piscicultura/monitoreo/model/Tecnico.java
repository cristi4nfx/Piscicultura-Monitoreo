/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.model;

/**
 *
 * @author Cristian
 */

public class Tecnico extends Usuario {

    public Tecnico() {}

    public Tecnico(int id, String nombre, String email, String contrasena, int telefono, boolean activo) {
        super(id, nombre, email, contrasena, telefono, Rol.TECNICO, activo);
    }

    public void modificarSensor(int idSensor, String nuevoValor) {
        System.out.println("Modificando sensor " + idSensor + " con nuevo valor: " + nuevoValor);
    }

    public void registrarMantenimiento(String fecha, int idEstanque) {
        System.out.println("Mantenimiento registrado en estanque " + idEstanque + " el " + fecha);
    }
}
