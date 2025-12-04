/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String FILE_NAME = "productos.dat";

    public static void guardar(List<Producto> lista) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(new ArrayList<>(lista));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Producto> cargar() throws IOException, ClassNotFoundException {
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object o = ois.readObject();
            if (o instanceof List) {
                return (List<Producto>) o;
            } else {
                return new ArrayList<>();
            }
        }
    }
}
