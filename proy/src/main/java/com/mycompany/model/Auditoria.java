package com.mycompany.model;

import java.sql.Timestamp;

public class Auditoria {
    private int id;
    private Timestamp fechaHora;
    private int idUsuario;
    private String nombreUsuario;
    private String entidad;
    private Integer idRegistro;
    private String accion;
    private String descripcion;
    private String valoresAntes;
    private String valoresDespues;

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getValoresAntes() { return valoresAntes; }
    public void setValoresAntes(String valoresAntes) { this.valoresAntes = valoresAntes; }

    public String getValoresDespues() { return valoresDespues; }
    public void setValoresDespues(String valoresDespues) { this.valoresDespues = valoresDespues; }
}
