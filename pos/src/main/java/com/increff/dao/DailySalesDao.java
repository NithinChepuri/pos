package com.increff.dao;

import com.increff.entity.DailySalesEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DailySalesDao extends AbstractDao<DailySalesEntity> {

    private static final String SELECT_BY_DATE = "SELECT p FROM DailySalesEntity p WHERE p.date = :date";
    private static final String SELECT_BY_DATE_RANGE = "SELECT p FROM DailySalesEntity p WHERE p.date >= :startDate AND p.date <= :endDate ORDER BY p.date";
    private static final String SELECT_LATEST = "SELECT p FROM DailySalesEntity p ORDER BY p.date DESC";

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