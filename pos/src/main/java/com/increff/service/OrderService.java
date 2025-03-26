package com.increff.service;

import com.increff.dao.OrderDao;
import com.increff.dao.OrderItemDao;
import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.model.enums.OrderStatus;
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
    public OrderEntity createOrder(OrderEntity orderEntity) {
        // Ensure required fields are set
        if (orderEntity.getStatus() == null) {
            orderEntity.setStatus(OrderStatus.CREATED);
        }
        if (orderEntity.getCreatedAt() == null) {
            orderEntity.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        }
        dao.insert(orderEntity);
        return orderEntity;
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
    public List<OrderEntity> getAll(int page, int size) {
        return dao.selectAll(page, size);
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
    public List<OrderEntity> getByDateRange(ZonedDateTime startDate, ZonedDateTime endDate, int page, int size) {
        return dao.selectByDateRange(startDate, endDate, page, size);
    }


} 