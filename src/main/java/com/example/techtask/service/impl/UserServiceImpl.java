package com.example.techtask.service.impl;

import com.example.techtask.model.Order;
import com.example.techtask.model.User;
import com.example.techtask.model.enumiration.OrderStatus;
import com.example.techtask.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User findUser() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = userQuery.from(User.class);

        Subquery<Long> countQuery = userQuery.subquery(Long.class);
        Root<Order> orderRoot = countQuery.from(Order.class);

        Predicate deliveredPredicate = criteriaBuilder.equal(orderRoot.get("orderStatus").as(String.class), OrderStatus.DELIVERED.name());
        Predicate yearPredicate = criteriaBuilder.between(orderRoot.get("createdAt"),
                LocalDateTime.of(2003, 1, 1, 0, 0),
                LocalDateTime.of(2004, 1, 1, 0, 0));

        countQuery.select(criteriaBuilder.count(orderRoot))
                .where(criteriaBuilder.and(deliveredPredicate, yearPredicate,
                        criteriaBuilder.equal(orderRoot.get("userId"), userRoot.get("id"))))
                .groupBy(orderRoot.get("userId"));

        userQuery.select(userRoot)
                .where(criteriaBuilder.greaterThan(countQuery, 0L))
                .orderBy(criteriaBuilder.desc(countQuery));

        return entityManager.createQuery(userQuery).setMaxResults(1).getSingleResult();
    }

    @Override
    public List<User> findUsers() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> userQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = userQuery.from(User.class);
        Join<User, Order> orderJoin = userRoot.join("orders");
        Predicate orderPaid = criteriaBuilder.equal(orderJoin.get("orderStatus").as(String.class), OrderStatus.PAID.name());
        Predicate in2010 = criteriaBuilder.between(orderJoin.get("createdAt"),
                LocalDateTime.of(2010, 1, 1, 0, 0),
                LocalDateTime.of(2011, 1, 1, 0, 0));
        userQuery
                .select(userRoot)
                .where(criteriaBuilder.and(orderPaid, in2010));

        return entityManager.createQuery(userQuery).getResultList();
    }
}