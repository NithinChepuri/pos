package com.increff.dao;

import com.increff.entity.DailySalesEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DailySalesDao extends AbstractDao {

    private static final String SELECT_BY_DATE = "select p from DailySalesEntity p where p.date = :date";
    private static final String SELECT_BY_DATE_RANGE = "select p from DailySalesEntity p where p.date >= :startDate and p.date <= :endDate order by p.date";
    private static final String SELECT_LATEST = "select p from DailySalesEntity p order by p.date desc";

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void insert(DailySalesEntity dailySalesEntity) {
        em.persist(dailySalesEntity);
    }

    public Optional<DailySalesEntity> selectByDate(LocalDate date) {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_BY_DATE, DailySalesEntity.class);
        query.setParameter("date", date);

        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    public List<DailySalesEntity> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_BY_DATE_RANGE, DailySalesEntity.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        
        return query.getResultList();
    }
    
    public Optional<DailySalesEntity> selectLatest() {
        TypedQuery<DailySalesEntity> query = getQuery(SELECT_LATEST, DailySalesEntity.class);
        query.setMaxResults(1);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
} 