package ni.edu.uam.evaluacion1_laboratorio_equipos.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import ni.edu.uam.evaluacion1_laboratorio_equipos.database.JPAUtil;
import ni.edu.uam.evaluacion1_laboratorio_equipos.model.Equipo;
import ni.edu.uam.evaluacion1_laboratorio_equipos.model.TipoEquipo;

import java.util.List;

public class EquipoDAO {

    public void guardar(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TipoEquipo tipoEquipo = obtenerTipoEquipo(em, equipo);
            equipo.setTipoEquipo(tipoEquipo);

            em.persist(equipo);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Equipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Equipo e "
                            + "JOIN FETCH e.tipoEquipo "
                            + "ORDER BY e.id",
                    Equipo.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca por un fragmento del nombre, por tipo de equipo o por ambos.
     * Si nombre está vacío y tipoEquipoId es null, devuelve todos los equipos.
     */
    public List<Equipo> buscar(String nombre, Long tipoEquipoId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT e FROM Equipo e "
                            + "JOIN FETCH e.tipoEquipo "
                            + "WHERE 1 = 1"
            );

            boolean filtrarNombre = nombre != null && !nombre.trim().isEmpty();
            boolean filtrarTipo = tipoEquipoId != null;

            if (filtrarNombre) {
                jpql.append(" AND LOWER(e.nombre) LIKE LOWER(:nombre)");
            }

            if (filtrarTipo) {
                jpql.append(" AND e.tipoEquipo.id = :tipoEquipoId");
            }

            jpql.append(" ORDER BY e.id");

            TypedQuery<Equipo> consulta =
                    em.createQuery(jpql.toString(), Equipo.class);

            if (filtrarNombre) {
                consulta.setParameter("nombre", "%" + nombre.trim() + "%");
            }

            if (filtrarTipo) {
                consulta.setParameter("tipoEquipoId", tipoEquipoId);
            }

            return consulta.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean modificar(Equipo equipo) {
        if (equipo == null || equipo.getId() == null) {
            throw new IllegalArgumentException(
                    "El equipo y su identificador son obligatorios para modificarlo."
            );
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipoExistente = em.find(Equipo.class, equipo.getId());

            if (equipoExistente == null) {
                tx.rollback();
                return false;
            }

            TipoEquipo tipoEquipo = obtenerTipoEquipo(em, equipo);

            equipoExistente.setCodigo(equipo.getCodigo());
            equipoExistente.setNombre(equipo.getNombre());
            equipoExistente.setMarca(equipo.getMarca());
            equipoExistente.setEstado(equipo.getEstado());
            equipoExistente.setCantidad(equipo.getCantidad());
            equipoExistente.setTipoEquipo(tipoEquipo);

            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del equipo es obligatorio para eliminarlo."
            );
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipo = em.find(Equipo.class, id);

            if (equipo == null) {
                tx.rollback();
                return false;
            }

            em.remove(equipo);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private TipoEquipo obtenerTipoEquipo(EntityManager em, Equipo equipo) {
        if (equipo.getTipoEquipo() == null
                || equipo.getTipoEquipo().getId() == null) {
            throw new IllegalArgumentException(
                    "El equipo debe tener un tipo válido."
            );
        }

        TipoEquipo tipoEquipo = em.find(
                TipoEquipo.class,
                equipo.getTipoEquipo().getId()
        );

        if (tipoEquipo == null) {
            throw new IllegalArgumentException(
                    "No existe el tipo de equipo seleccionado."
            );
        }

        return tipoEquipo;
    }

}
