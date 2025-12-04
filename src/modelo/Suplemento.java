/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

public class Suplemento extends Producto {
    private static final long serialVersionUID = 1L;

    public static enum Objetivo {
        Vitaminas, Deportivo, Proteinas, Acidos_grasos
    }

    private Objetivo objetivo;

    public Suplemento(String nombreComercial, String dosis, LocalDate fechaVencimiento, int stock, Objetivo objetivo) {
        super(nombreComercial, dosis, fechaVencimiento, stock);
        this.objetivo = objetivo;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    @Override
    public String getTipo() {
        return "Suplemento";
    }
}
