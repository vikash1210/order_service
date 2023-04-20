package com.springboot.orderservice.service;
import com.springboot.orderservice.entity.Order;
import com.springboot.orderservice.exception.CustomException;
import com.springboot.orderservice.external.client.PaymentService;
import com.springboot.orderservice.external.client.ProductService;
import com.springboot.orderservice.external.request.PaymentRequest;
import com.springboot.orderservice.external.response.PaymentResponse;
import com.springboot.orderservice.model.OrderRequest;
import com.springboot.orderservice.model.OrderResponse;
import com.springboot.orderservice.model.ProductResponse;
import com.springboot.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Log4j2

public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Order is placing:{} ",orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());

        log.info("Creating order with Status CREATED");
        Order order = Order.builder()
               .amount(orderRequest.getTotalAmount())
               .orderStatus("Created")
               .productId(orderRequest.getProductId())
               .orderDate(Instant.now())
               .quantity(orderRequest.getQuantity())
               .build();

         order = orderRepository.save(order);
         log.info("Calling payment service to complete the payment");
        PaymentRequest paymentRequest =PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus= null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully...");
            orderStatus="success";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus="PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order placed successfully with order id:{}",order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for order id: {}",orderId);
        Order order =orderRepository
                .findById(orderId)
                .orElseThrow(()->new CustomException("Order not found for the order id:"+orderId,"NOT_FOUND",404));

        log.info("Invoking the product service to fetch the product response for productId:{}",order.getProductId());

        ProductResponse productResponse =restTemplate
                .getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class);

        log.info("Getting details for the payment information from the payment service");
        PaymentResponse paymentResponse=
        restTemplate.getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),PaymentResponse.class);


        assert productResponse != null;
        OrderResponse.ProductDetails productDetails=
                 OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                         .price(productResponse.getPrice())
                         .quantity(productResponse.getQuantity())
                .build();

        assert paymentResponse != null;
        OrderResponse.PaymentDetails paymentDetails=
                OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .status(paymentResponse.getStatus())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .amount(paymentResponse.getAmount())
                        .orderId(paymentResponse.getOrderId())
                        .build();

        return OrderResponse.builder()
                .amount(order.getAmount())
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .productDetails(productDetails)
                .paymentdetails(paymentDetails)
                .build();
    }
}
