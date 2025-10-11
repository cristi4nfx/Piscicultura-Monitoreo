/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.piscicultura.monitoreo.model;

/**
 *
 * @author Cristian
 */

public class Administrador extends Usuario {

    public Administrador() {}

    public Administrador(int id, String nombre, String email, String contrasena, int telefono, boolean activo) {
        super(id, nombre, email, contrasena, telefono, Rol.ADMIN, activo);
    }

    public void crearUsuario(int id, String nombre, String email, String contrasena, int telefono, boolean activo) {
        System.out.println("Usuario creado: " + nombre);
    }

    public void modificarNombre(int id, String nuevoNombre) {
        System.out.println("Modificando nombre del usuario con ID " + id + " a " + nuevoNombre);
    }

    public void modificarEmail(int id, String nuevoEmail) {
        System.out.println("Modificando email del usuario con ID " + id + " a " + nuevoEmail);
    }

    public void modificarContrasena(int id, String nuevaContrasena) {
        System.out.println("Contraseña modificada para usuario con ID " + id);
    }

    public void eliminarUsuario(int id) {
        System.out.println("Usuario eliminado con ID " + id);
    }
}
