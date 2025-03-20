package com.increff.dao;

import com.increff.entity.DailySalesEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class DailySalesDao extends AbstractDao {

    private static final String SELECT_BY_DATE = "select p from DailySalesEntity p where p.date = :date";

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
} 