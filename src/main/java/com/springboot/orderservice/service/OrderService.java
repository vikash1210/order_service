package com.springboot.orderservice.service;

import com.springboot.orderservice.model.OrderRequest;
import com.springboot.orderservice.model.OrderResponse;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
