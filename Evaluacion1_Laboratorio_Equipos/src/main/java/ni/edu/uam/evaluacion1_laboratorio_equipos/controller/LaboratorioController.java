package ni.edu.uam.evaluacion1_laboratorio_equipos.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.evaluacion1_laboratorio_equipos.dao.EquipoDAO;
import ni.edu.uam.evaluacion1_laboratorio_equipos.dao.TipoEquipoDAO;
import ni.edu.uam.evaluacion1_laboratorio_equipos.database.JPAUtil;
import ni.edu.uam.evaluacion1_laboratorio_equipos.model.Equipo;
import ni.edu.uam.evaluacion1_laboratorio_equipos.model.TipoEquipo;

import java.util.List;
import java.util.Optional;

public class LaboratorioController {


    private static final List<String> ESTADOS_PERMITIDOS = List.of(
            "Disponible",
            "En mantenimiento",
            "Fuera de servicio"
    );

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtMarca;

    @FXML
    private TextField txtCantidad;

    @FXML
    private ComboBox<TipoEquipo> cmbTipoEquipo;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TextField txtBuscarNombre;

    @FXML
    private ComboBox<TipoEquipo> cmbFiltroTipo;

    @FXML
    private TableView<Equipo> tablaEquipos;

    @FXML
    private TableColumn<Equipo, String> colCodigo;

    @FXML
    private TableColumn<Equipo, String> colNombre;

    @FXML
    private TableColumn<Equipo, String> colMarca;

    @FXML
    private TableColumn<Equipo, String> colEstado;

    @FXML
    private TableColumn<Equipo, Integer> colCantidad;

    @FXML
    private TableColumn<Equipo, String> colTipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    private Equipo equipoSeleccionado;

    @FXML
    private void initialize() {
        configurarColumnas();
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS_PERMITIDOS));

        tablaEquipos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        cargarEquipoEnFormulario(seleccionado);
                    }
                });

        try {
            cargarTipos();
            cargarEquipos();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo cargar la información desde PostgreSQL.", ex);
        }
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(celda.getValue().getCodigo())
        );

        colNombre.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(celda.getValue().getNombre())
        );

        colMarca.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(celda.getValue().getMarca())
        );

        colEstado.setCellValueFactory(
                celda -> new ReadOnlyStringWrapper(celda.getValue().getEstado())
        );

        colCantidad.setCellValueFactory(
                celda -> new ReadOnlyObjectWrapper<>(celda.getValue().getCantidad())
        );

        colTipo.setCellValueFactory(celda -> {
            TipoEquipo tipo = celda.getValue().getTipoEquipo();
            String nombreTipo = tipo == null ? "" : tipo.getNombre();
            return new ReadOnlyStringWrapper(nombreTipo);
        });
    }

    private void cargarTipos() {
        List<TipoEquipo> tipos = tipoEquipoDAO.listar();

        cmbTipoEquipo.setItems(FXCollections.observableArrayList(tipos));
        cmbFiltroTipo.setItems(FXCollections.observableArrayList(tipos));
    }

    private void cargarEquipos() {
        tablaEquipos.setItems(
                FXCollections.observableArrayList(equipoDAO.listar())
        );
    }

    @FXML
    private void guardarEquipo() {
        try {
            Equipo equipo = leerFormulario();

            if (codigoYaExiste(equipo.getCodigo(), null)) {
                mostrarAviso("Ya existe un equipo con ese código.");
                return;
            }

            equipoDAO.guardar(equipo);

            mostrarInformacion("El equipo se guardó correctamente.");
            cargarEquipos();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            mostrarAviso(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo guardar el equipo. Comprueba los datos y que el código no esté duplicado.", ex);
        }
    }

    @FXML
    private void actualizarEquipo() {
        if (equipoSeleccionado == null) {
            mostrarAviso("Selecciona un equipo de la tabla antes de actualizarlo.");
            return;
        }

        try {
            Equipo equipo = leerFormulario();
            equipo.setId(equipoSeleccionado.getId());

            if (codigoYaExiste(equipo.getCodigo(), equipo.getId())) {
                mostrarAviso("Ese código ya pertenece a otro equipo.");
                return;
            }

            boolean actualizado = equipoDAO.modificar(equipo);

            if (!actualizado) {
                mostrarAviso("No se encontró el equipo que intentas actualizar.");
                cargarEquipos();
                return;
            }

            mostrarInformacion("El equipo se actualizó correctamente.");
            cargarEquipos();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            mostrarAviso(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo actualizar el equipo.", ex);
        }
    }

    @FXML
    private void eliminarEquipo() {
        if (equipoSeleccionado == null) {
            mostrarAviso("Selecciona un equipo de la tabla antes de eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar el equipo seleccionado?");
        confirmacion.setContentText(
                "Código: " + equipoSeleccionado.getCodigo()
                        + "\nNombre: " + equipoSeleccionado.getNombre()
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean eliminado = equipoDAO.eliminar(equipoSeleccionado.getId());

            if (!eliminado) {
                mostrarAviso("No se encontró el equipo que intentas eliminar.");
                cargarEquipos();
                return;
            }

            mostrarInformacion("El equipo se eliminó correctamente.");
            cargarEquipos();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo eliminar el equipo.", ex);
        }
    }

    @FXML
    private void aplicarFiltros() {
        String nombre = txtBuscarNombre.getText() == null
                ? ""
                : txtBuscarNombre.getText().trim();

        TipoEquipo tipo = cmbFiltroTipo.getValue();

        EntityManager em = JPAUtil.getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo WHERE 1 = 1"
            );

            if (!nombre.isEmpty()) {
                jpql.append(" AND LOWER(e.nombre) LIKE LOWER(:nombre)");
            }

            if (tipo != null) {
                jpql.append(" AND e.tipoEquipo.id = :idTipo");
            }

            jpql.append(" ORDER BY e.id");

            TypedQuery<Equipo> consulta = em.createQuery(jpql.toString(), Equipo.class);

            if (!nombre.isEmpty()) {
                consulta.setParameter("nombre", "%" + nombre + "%");
            }

            if (tipo != null) {
                consulta.setParameter("idTipo", tipo.getId());
            }

            tablaEquipos.setItems(
                    FXCollections.observableArrayList(consulta.getResultList())
            );
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron aplicar los filtros.", ex);
        } finally {
            em.close();
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        txtBuscarNombre.clear();

        cmbTipoEquipo.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();
        cmbFiltroTipo.getSelectionModel().clearSelection();

        tablaEquipos.getSelectionModel().clearSelection();
        equipoSeleccionado = null;
    }

    private Equipo leerFormulario() {
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String marca = txtMarca.getText() == null ? "" : txtMarca.getText().trim();
        String cantidadTexto = txtCantidad.getText() == null
                ? ""
                : txtCantidad.getText().trim();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (marca.isEmpty()) {
            throw new IllegalArgumentException("La marca es obligatoria.");
        }

        if (cantidadTexto.isEmpty()) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        TipoEquipo tipo = cmbTipoEquipo.getValue();

        if (tipo == null) {
            throw new IllegalArgumentException("Selecciona un tipo de equipo.");
        }

        String estado = cmbEstado.getValue();

        if (estado == null || !ESTADOS_PERMITIDOS.contains(estado)) {
            throw new IllegalArgumentException("Selecciona un estado permitido.");
        }

        return new Equipo(codigo, nombre, marca, estado, cantidad, tipo);
    }

    private boolean codigoYaExiste(String codigo, Long idExcluir) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(e) FROM Equipo e "
                            + "WHERE LOWER(e.codigo) = LOWER(:codigo)"
            );

            if (idExcluir != null) {
                jpql.append(" AND e.id <> :idExcluir");
            }

            TypedQuery<Long> consulta = em.createQuery(jpql.toString(), Long.class);
            consulta.setParameter("codigo", codigo);

            if (idExcluir != null) {
                consulta.setParameter("idExcluir", idExcluir);
            }

            return consulta.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    private void cargarEquipoEnFormulario(Equipo equipo) {
        equipoSeleccionado = equipo;

        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca());
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));

        cmbEstado.setValue(equipo.getEstado());
        cmbTipoEquipo.setValue(equipo.getTipoEquipo());
    }

    private void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Validación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Operación completada");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje, Throwable error) {
        String detalle = error.getMessage();

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(detalle == null ? "Ocurrió un error inesperado." : detalle);
        alerta.showAndWait();
    }
}

