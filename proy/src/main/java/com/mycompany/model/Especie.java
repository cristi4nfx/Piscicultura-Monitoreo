/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Cristian
 */

import com.mycompany.dao.EspecieDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.util.ConexionDB;

public class Especie {
    private Integer idEspecie;
    private String nombreCientifico;
    private String nombreComun;
    private int edadDias;
    private float pesoPromedioG;
    private Date fechaSiembra;
    private int cantidad;
    private List<Parametro> parametros;

    // Constructor vacío
    public Especie() {
        this.parametros = new ArrayList<>();
    }

    // Constructor completo
    public Especie(int idEspecie, String nombreCientifico, String nombreComun,
                   int edadDias, float pesoPromedioG, Date fechaSiembra, int cantidad) {
        this.idEspecie = idEspecie;
        this.nombreCientifico = nombreCientifico;
        this.nombreComun = nombreComun;
        this.edadDias = edadDias;
        this.pesoPromedioG = pesoPromedioG;
        this.fechaSiembra = fechaSiembra;
        this.cantidad = cantidad;
        this.parametros = new ArrayList<>();
    }

    // Métodos
    public void agregarParametro(Parametro parametro) {
        parametros.add(parametro);
    }

    public void eliminarParametro(Parametro parametro) {
        parametros.remove(parametro);
    }

    public void modificarParametros(List<Parametro> nuevosParametros) {
        this.parametros = nuevosParametros;
    }

    // Getters y Setters
    public int getIdEspecie() { return idEspecie; }
    public void setIdEspecie(int idEspecie) { this.idEspecie = idEspecie; }

    public String getNombreCientifico() { return nombreCientifico; }
    public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }

    public int getEdadDias() { return edadDias; }
    public void setEdadDias(int edadDias) { this.edadDias = edadDias; }

    public float getPesoPromedioG() { return pesoPromedioG; }
    public void setPesoPromedioG(float pesoPromedioG) { this.pesoPromedioG = pesoPromedioG; }

    public Date getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(Date fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public void setParametros(List<Parametro> nuevos) {
        // Reemplaza por una nueva lista deduplicada
        this.parametros = (nuevos != null) ? new ArrayList<>(nuevos) : new ArrayList<>();
    }

    public List<Parametro> getParametros() throws Exception {
        // Si ya están cargados, regresa
        if (parametros != null && !parametros.isEmpty()) return parametros;

        // Si no hay id, no hay qué consultar
        if (idEspecie == null || idEspecie <= 0) return parametros;

        // CARGA SIMPLE DESDE LA BD (usa tu helper de conexión)
        try (Connection c = ConexionDB.getConnection()) {
            EspecieDAO dao = new EspecieDAO(c);
            parametros = dao.obtenerParametrosPorEspecie(idEspecie);
        } catch (SQLException e) {
            System.err.println("Error cargando parámetros de especie " + idEspecie + ": " + e.getMessage());
            // deja 'parametros' como lista vacía para evitar NPE
            parametros = new ArrayList<>();
        }
        return parametros;
    }
}
