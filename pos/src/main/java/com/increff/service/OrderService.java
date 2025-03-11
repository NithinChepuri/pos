package com.increff.service;

import com.increff.dao.OrderDao;
import com.increff.dao.OrderItemDao;
import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderDao dao;
    
    @Autowired
    private OrderItemDao itemDao;

    @Transactional
    public OrderEntity createOrder() {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        dao.insert(order);
        return order;
    }

    @Transactional
    public void addOrderItem(OrderItemEntity orderItem) {
        itemDao.insert(orderItem);
    }

    @Transactional(readOnly = true)
    public OrderEntity get(Long id) {
        return dao.select(id);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> getAll() {
        return dao.selectAll();
    }

    @Transactional(readOnly = true)
    public List<OrderItemEntity> getOrderItems(Long orderId) {
        return itemDao.selectByOrderId(orderId);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        OrderEntity order = get(orderId);
        order.setStatus(status);
        dao.update(order);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return dao.selectByDateRange(startDate, endDate);
    }
} 