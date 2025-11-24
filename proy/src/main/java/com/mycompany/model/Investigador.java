/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Cristian
 */

public class Investigador extends Usuario {

    public Investigador() {}

    public Investigador(int id, String nombre, String email, String contrasena, int telefono, boolean activo) {
        super(id, nombre, email, contrasena, telefono, Rol.INVESTIGADOR, activo);
    }

    public void consultarDatosEstanque(int idEstanque) {
        System.out.println("Consultando datos del estanque " + idEstanque);
    }

    public void historico(String fechaInicio, String fechaFin) {
        System.out.println("Consultando histórico desde " + fechaInicio + " hasta " + fechaFin);
    }
}
