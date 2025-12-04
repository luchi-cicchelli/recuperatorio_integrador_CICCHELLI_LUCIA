/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreComercial;
    private String dosis;
    private LocalDate fechaVencimiento;
    private int stock;

    public Producto(String nombreComercial, String dosis, LocalDate fechaVencimiento, int stock) {
        this.nombreComercial = nombreComercial;
        this.dosis = dosis;
        this.fechaVencimiento = fechaVencimiento;
        this.stock = stock;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean estaVencido() {
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    public String getFechaVencimientoFormateada() {
        if (fechaVencimiento == null) return "";
        return fechaVencimiento.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return nombreComercial + " - " + dosis;
    }
}
