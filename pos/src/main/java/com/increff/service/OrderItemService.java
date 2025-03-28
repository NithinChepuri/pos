package com.increff.service;

import com.increff.dao.OrderItemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemService {
    
    @Autowired
    private OrderItemDao dao;
    

    @Transactional(readOnly = true)
    public boolean existsByProductId(Long productId) {
        return dao.countByProductId(productId) > 0;
    }
} 