package com.increff.flow;

import com.increff.entity.OrderEntity;
import com.increff.entity.OrderItemEntity;
import com.increff.entity.ProductEntity;
import com.increff.model.invoice.InvoiceData;
import com.increff.model.invoice.InvoiceItemData;
import com.increff.service.ApiException;
import com.increff.service.OrderService;
import com.increff.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.increff.model.enums.OrderStatus;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InvoiceFlow {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;
} 