// com.mycompany.model.Lote
package com.mycompany.model;

import java.time.OffsetDateTime; // o LocalDateTime si prefieres

public class Lote {
    private int idLote;
    private int idEstanque;
    private int idEspecie;

    private String descripcion;

    private Double phActual;                 // NUEVO: mapea lotes.ph_actual
    private OffsetDateTime createdAt;        // opcional
    private OffsetDateTime updatedAt;        // opcional
    
    private Boolean activo;   // puede ser null si quieres, pero mejor boolean/Boolean

    public Boolean getActivo() {
        return activo;
    }
    
    public Lote() {
    }

    /** Constructor básico para mostrar en ComboBox cuando no hay lotes */
    public Lote(int idLote, String codigo) {
        this.idLote = idLote;

    }

    /** Constructor completo (opcional, por si lo necesitas más adelante) */
    public Lote(int idLote, int idEstanque, int idEspecie,
                String descripcion, Double phActual, OffsetDateTime createdAt,
                OffsetDateTime updatedAt, Boolean activo) {
        this.idLote = idLote;
        this.idEstanque = idEstanque;
        this.idEspecie = idEspecie;

        this.descripcion = descripcion;
        this.phActual = phActual;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.activo = activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public int getIdEstanque() { return idEstanque; }
    public void setIdEstanque(int idEstanque) { this.idEstanque = idEstanque; }

    public int getIdEspecie() { return idEspecie; }
    public void setIdEspecie(int idEspecie) { this.idEspecie = idEspecie; }



    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPhActual() { return phActual; }
    public void setPhActual(Double phActual) { this.phActual = phActual; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Lote #" + idLote;
    }
}
