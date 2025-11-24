package com.mycompany.model;

public class Sensor {
    private int idSensor;
    private String codigo;
    private String tipo;
    private String estado;
    private String descripcion;
    private Integer idEstanque; // puede ser null si está libre

    public int getIdSensor() { return idSensor; }
    public void setIdSensor(int idSensor) { this.idSensor = idSensor; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIdEstanque() { return idEstanque; }
    public void setIdEstanque(Integer idEstanque) { this.idEstanque = idEstanque; }

    @Override
    public String toString() {
        return codigo + " (" + tipo + ")"; // útil para combos
    }
}
