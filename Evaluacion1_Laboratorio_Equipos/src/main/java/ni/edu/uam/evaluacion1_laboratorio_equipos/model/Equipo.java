package ni.edu.uam.evaluacion1_laboratorio_equipos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo", schema = "catalogos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, length = 250)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;

    @Column(name = "marca", nullable = false, length = 250)
    private String marca;

    @Column(name = "estado", nullable = false, length = 250)
    private String estado;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false) //Para establecer la relación con la tabla "tipos_equipos"
    private TipoEquipo tipoEquipo;

    public Equipo() {
    }

    public Equipo(Long id) {
        this.id = id;
    }

    public Equipo(String codigo, String nombre, String marca, String estado, Integer cantidad, TipoEquipo tipoEquipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.estado = estado;
        this.cantidad = cantidad;
        this.tipoEquipo = tipoEquipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public TipoEquipo getTipoEquipo() {
        return tipoEquipo;
    }

    public void setTipoEquipo(TipoEquipo tipoEquipo) {
        this.tipoEquipo = tipoEquipo;
    }
}
