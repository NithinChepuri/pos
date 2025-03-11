package com.increff.dao;

import com.increff.entity.UserEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class UserDao extends AbstractDao {
    
    @PersistenceContext
    private EntityManager em;

    public UserEntity insert(UserEntity user) {
        em.persist(user);
        return user;
    }

    public UserEntity findByEmail(String email) {
        TypedQuery<UserEntity> query = getQuery(
            "select u from UserEntity u where lower(u.email)=:email", 
            UserEntity.class);
        query.setParameter("email", email.toLowerCase());
        List<UserEntity> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    public UserEntity select(Long id) {
        return em.find(UserEntity.class, id);
    }
} 