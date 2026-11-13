package ni.edu.uam.evaluacion1_laboratorio_equipos.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.evaluacion1_laboratorio_equipos.database.JPAUtil;
import ni.edu.uam.evaluacion1_laboratorio_equipos.model.TipoEquipo;

import java.util.List;

public class TipoEquipoDAO {

    public List<TipoEquipo> listar(){
        EntityManager em = JPAUtil.getEntityManager();
        try{
            return em.createQuery("select t from TipoEquipo t", TipoEquipo.class).getResultList();

        }finally{
            em.close();
        }
    }

}
