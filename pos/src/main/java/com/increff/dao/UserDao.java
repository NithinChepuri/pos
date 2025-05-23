package com.increff.dao;

import com.increff.entity.UserEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.TypedQuery;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
public class UserDao extends AbstractDao {
    private static final String SELECT_BY_EMAIL = "SELECT u FROM UserEntity u WHERE LOWER(u.email) = :email";

    public UserEntity insert(UserEntity user) {
        em.persist(user);
        return user;
    }


    public UserEntity findByEmail(String email) {
        TypedQuery<UserEntity> query = getQuery(SELECT_BY_EMAIL, UserEntity.class);
        query.setParameter("email", email.toLowerCase());
        List<UserEntity> resultList = query.getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }


    public UserEntity select(Long id) {
        return em.find(UserEntity.class, id);
    }
}