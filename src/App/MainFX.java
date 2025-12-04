/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package App;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import modelo.*;

import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MainFX extends Application {

    private TableView<Producto> table;
    private ObservableList<Producto> productos;

    // form fields
    private TextField tfNombre;
    private TextField tfDosis;
    private DatePicker dpVencimiento;
    private Spinner<Integer> spStock;
    private CheckBox cbReceta;
    private ComboBox<String> cbTipo;
    private ComboBox<Suplemento.Objetivo> cbObjetivo;

    private Button btnAgregar;
    private Button btnModificar;
    private Button btnEliminar;
    private Button btnLimpiar;

    private Producto seleccionado = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventario - Don Alberto");

        productos = FXCollections.observableArrayList();
        loadData();

        table = new TableView<>(productos);
        table.setPlaceholder(new Label("No hay productos cargados"));

        TableColumn<Producto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTipo()));

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreComercial"));

        TableColumn<Producto, String> colDosis = new TableColumn<>("Dosis");
        colDosis.setCellValueFactory(new PropertyValueFactory<>("dosis"));

        TableColumn<Producto, String> colVence = new TableColumn<>("Vencimiento");
        colVence.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getFechaVencimientoFormateada()));

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Producto, String> colExtra = new TableColumn<>("Extra");
        colExtra.setCellValueFactory(p -> {
            Producto prod = p.getValue();
            if (prod instanceof Medicamento) {
                return new ReadOnlyStringWrapper(((Medicamento) prod).isRequiereReceta() ? "Receta" : "Sin receta");
            } else if (prod instanceof Suplemento) {
                return new ReadOnlyStringWrapper(((Suplemento) prod).getObjetivo().name());
            }
            return new ReadOnlyStringWrapper("");
        });

        table.getColumns().addAll(colTipo, colNombre, colDosis, colVence, colStock, colExtra);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            seleccionado = newSel;
            cargarFormularioDesdeSeleccion();
        });

        GridPane form = crearFormulario();

        HBox botones = new HBox(8, btnAgregar, btnModificar, btnEliminar, btnLimpiar);
        VBox root = new VBox(10, table, form, botones);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(evt -> {
            try {
                DataManager.guardar(productos);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        primaryStage.show();
    }

    private GridPane crearFormulario() {
        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);

        tfNombre = new TextField();
        tfDosis = new TextField();
        dpVencimiento = new DatePicker();
        spStock = new Spinner<>(0, 10000, 1);
        spStock.setEditable(true);

        cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll("Medicamento", "Suplemento");
        cbTipo.setValue("Medicamento");

        cbReceta = new CheckBox("Requiere receta");

        cbObjetivo = new ComboBox<>();
        cbObjetivo.getItems().addAll(Suplemento.Objetivo.values());
        cbObjetivo.setValue(Suplemento.Objetivo.Vitaminas);

        btnAgregar = new Button("Agregar");
        btnModificar = new Button("Modificar");
        btnEliminar = new Button("Eliminar");
        btnLimpiar = new Button("Limpiar");

        // colocar en grid
        gp.add(new Label("Tipo:"), 0, 0);
        gp.add(cbTipo, 1, 0);

        gp.add(new Label("Nombre comercial:"), 0, 1);
        gp.add(tfNombre, 1, 1);

        gp.add(new Label("Dosis:"), 0, 2);
        gp.add(tfDosis, 1, 2);

        gp.add(new Label("Fecha vencimiento:"), 0, 3);
        gp.add(dpVencimiento, 1, 3);

        gp.add(new Label("Stock:"), 0, 4);
        gp.add(spStock, 1, 4);

        gp.add(cbReceta, 2, 1);
        gp.add(new Label("Objetivo (suplemento):"), 2, 2);
        gp.add(cbObjetivo, 3, 2);

        // acciones
        btnAgregar.setOnAction(e -> onAgregar());
        btnModificar.setOnAction(e -> onModificar());
        btnEliminar.setOnAction(e -> onEliminar());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        // cambiar visibilidad de campos extra segun tipo
        cbTipo.setOnAction(e -> actualizarVisibilidadCampos());

        actualizarVisibilidadCampos();

        return gp;
    }

    private void actualizarVisibilidadCampos() {
        boolean esMedicamento = "Medicamento".equals(cbTipo.getValue());
        cbReceta.setDisable(!esMedicamento);
        cbReceta.setVisible(esMedicamento);

        cbObjetivo.setDisable(esMedicamento);
        cbObjetivo.setVisible(!esMedicamento);
    }

    private void onAgregar() {
        try {
            Producto p = construirProductoDesdeFormulario();
            productos.add(p);
            limpiarFormulario();
            guardarDatosSilencioso();
            mostrarInfo("Producto agregado correctamente.");
        } catch (ValidationException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al agregar el producto.");
        }
    }

    private void onModificar() {
        if (seleccionado == null) {
            mostrarError("Seleccione un producto para modificar.");
            return;
        }
        try {
            Producto p = construirProductoDesdeFormulario();
            int idx = productos.indexOf(seleccionado);
            if (idx >= 0) {
                productos.set(idx, p);
                seleccionado = p;
                table.getSelectionModel().select(idx);
                guardarDatosSilencioso();
                mostrarInfo("Producto modificado correctamente.");
            }
        } catch (ValidationException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al modificar el producto.");
        }
    }

    private void onEliminar() {
        if (seleccionado == null) {
            mostrarError("Seleccione un producto para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("Eliminar producto");
        confirm.setContentText("¿Está seguro que desea eliminar: " + seleccionado.getNombreComercial() + "?");
        Optional<ButtonType> opt = confirm.showAndWait();
        if (opt.isPresent() && opt.get() == ButtonType.OK) {
            productos.remove(seleccionado);
            limpiarFormulario();
            guardarDatosSilencioso();
            mostrarInfo("Producto eliminado.");
        }
    }

    private Producto construirProductoDesdeFormulario() throws ValidationException {
        String nombre = tfNombre.getText().trim();
        String dosis = tfDosis.getText().trim();
        LocalDate venc = dpVencimiento.getValue();
        int stock = spStock.getValue();

        // validaciones
        if (nombre.isEmpty()) throw new ValidationException("El nombre comercial es obligatorio.");
        if (dosis.isEmpty()) throw new ValidationException("La dosis es obligatoria.");
        if (venc == null) throw new ValidationException("La fecha de vencimiento es obligatoria.");
        if (stock < 0) throw new ValidationException("El stock no puede ser negativo.");
        // opcional: evitar cargar vencido
        if (venc.isBefore(LocalDate.now())) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Producto vencido");
            a.setHeaderText("La fecha de vencimiento ya pasó");
            a.setContentText("¿Desea cargar el producto vencido?");
            Optional<ButtonType> opt = a.showAndWait();
            if (!(opt.isPresent() && opt.get() == ButtonType.OK)) {
                throw new ValidationException("Carga cancelada por vencimiento.");
            }
        }

        if ("Medicamento".equals(cbTipo.getValue())) {
            boolean requiere = cbReceta.isSelected();
            return new Medicamento(nombre, dosis, venc, stock, requiere);
        } else {
            Suplemento.Objetivo objetivo = cbObjetivo.getValue();
            return new Suplemento(nombre, dosis, venc, stock, objetivo);
        }
    }

    private void cargarFormularioDesdeSeleccion() {
        if (seleccionado == null) {
            limpiarFormulario();
            return;
        }
        tfNombre.setText(seleccionado.getNombreComercial());
        tfDosis.setText(seleccionado.getDosis());
        dpVencimiento.setValue(seleccionado.getFechaVencimiento());
        spStock.getValueFactory().setValue(seleccionado.getStock());

        if (seleccionado instanceof Medicamento) {
            cbTipo.setValue("Medicamento");
            cbReceta.setSelected(((Medicamento) seleccionado).isRequiereReceta());
        } else if (seleccionado instanceof Suplemento) {
            cbTipo.setValue("Suplemento");
            cbObjetivo.setValue(((Suplemento) seleccionado).getObjetivo());
        }

        actualizarVisibilidadCampos();
    }

    private void limpiarFormulario() {
        tfNombre.clear();
        tfDosis.clear();
        dpVencimiento.setValue(null);
        spStock.getValueFactory().setValue(1);
        cbReceta.setSelected(false);
        cbTipo.setValue("Medicamento");
        cbObjetivo.setValue(Suplemento.Objetivo.Vitaminas);
        seleccionado = null;
        table.getSelectionModel().clearSelection();
        actualizarVisibilidadCampos();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Atención");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void guardarDatosSilencioso() {
        try {
            DataManager.guardar(productos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadData() {
        try {
            List<Producto> list = DataManager.cargar();
            productos.addAll(list);
        } catch (Exception ex) {
            // si hay problema cargando, avisamos y seguimos con lista vacía
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error de carga");
            a.setHeaderText("No se pudo leer el archivo de productos");
            a.setContentText("Se iniciará con inventario vacío.");
            a.showAndWait();
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
