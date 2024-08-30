package com.example.techtask.service.impl;

import com.example.techtask.model.Order;
import com.example.techtask.model.User;
import com.example.techtask.model.enumiration.UserStatus;
import com.example.techtask.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Order findOrder() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> orderQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> orderRoot = orderQuery.from(Order.class);

        Subquery<LocalDateTime> maxDateQuery = orderQuery.subquery(LocalDateTime.class);
        Root<Order> maxDateRoot = maxDateQuery.from(Order.class);

        Predicate moreThenOne = criteriaBuilder.gt(maxDateRoot.get("quantity"), 1);
        maxDateQuery
                .select(criteriaBuilder.greatest(maxDateRoot.get("createdAt").as(LocalDateTime.class)))
                .where(moreThenOne);
        Predicate newestOrder = criteriaBuilder.equal(orderRoot.get("createdAt"), maxDateQuery);
        orderQuery
                .select(orderRoot)
                .where(newestOrder);

        return entityManager.createQuery(orderQuery).getSingleResult();
    }

    @Override
    public List<Order> findOrders() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> orderQuery = cb.createQuery(Order.class);

        Root<User> userRoot = orderQuery.from(User.class);
        Join<User, Order> orderJoin = userRoot.join("orders");

        Predicate activeUsers = cb.equal(userRoot.get("userStatus").as(String.class), UserStatus.ACTIVE.name());
        orderQuery.select(orderJoin)
                .where(activeUsers)
                .orderBy(cb.asc(orderJoin.get("createdAt")));

        return entityManager.createQuery(orderQuery).getResultList();
    }
}
