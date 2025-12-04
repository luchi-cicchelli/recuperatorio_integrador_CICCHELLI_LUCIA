/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

public class Medicamento extends Producto {
    private static final long serialVersionUID = 1L;
    private boolean requiereReceta;

    public Medicamento(String nombreComercial, String dosis, LocalDate fechaVencimiento, int stock, boolean requiereReceta) {
        super(nombreComercial, dosis, fechaVencimiento, stock);
        this.requiereReceta = requiereReceta;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    @Override
    public String getTipo() {
        return "Medicamento";
    }
}
