package com.increff.dao;

import com.increff.entity.UserEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
public class UserDao extends AbstractDao {
    private static final String SELECT_BY_EMAIL = "select u from UserEntity u where lower(u.email)=:email";
    @PersistenceContext
    private EntityManager em;

    public UserEntity insert(UserEntity user) {
        em.persist(user);
        return user;
    }

    public UserEntity findByEmail(String email) {
        TypedQuery<UserEntity> query = getQuery(SELECT_BY_EMAIL, UserEntity.class);
        query.setParameter("email", email.toLowerCase());
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity select(Long id) {
        return em.find(UserEntity.class, id);
    }
} 