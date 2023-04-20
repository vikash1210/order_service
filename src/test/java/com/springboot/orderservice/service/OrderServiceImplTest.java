package com.springboot.orderservice.service;
import com.springboot.orderservice.entity.Order;
import com.springboot.orderservice.external.client.PaymentService;
import com.springboot.orderservice.external.client.ProductService;
import com.springboot.orderservice.external.response.PaymentResponse;
import com.springboot.orderservice.model.OrderResponse;
import com.springboot.orderservice.model.PaymentMode;
import com.springboot.orderservice.model.ProductResponse;
import com.springboot.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest

public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService=new OrderServiceImpl();

    @DisplayName("Get Order success scenario")
    @Test
    void testWhenOrderSuccess(){
        //Mocking

         Order order= getMockOrder();
         Mockito.when(orderRepository.findById(anyLong()))
                 .thenReturn(Optional.of(order));

         Mockito.when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class))
                 .thenReturn(getMockProductResponse());

         Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(), PaymentResponse.class))
                 .thenReturn(getMockPaymentResponse());

         //Actual
        OrderResponse orderResponse=orderService.getOrderDetails(1);

        //Verification
        Mockito.verify(orderRepository,Mockito.times(1)).findById(anyLong());

        Mockito.verify(restTemplate,Mockito.times(1))
                .getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class);

        Mockito.verify(restTemplate,Mockito.times(1))
                .getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),PaymentResponse.class);
        //Assert
        Assertions.assertNotNull(orderResponse);
        Assertions.assertEquals(order.getId(),orderResponse.getOrderId());
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .orderId(2)
                .paymentId(101)
                .paymentMode(PaymentMode.CASH)
                .paymentDate(Instant.now())
                .amount(1000)
                .status("Accepted")
                .build();

    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("Nokia")
                .price(1000)
                .quantity(20)
                .build();
    }

    private Order getMockOrder() {
       return Order.builder()
                .amount(1000)
                .orderDate(Instant.now())
                .orderStatus("Success")
                .productId(2)
                .id(2)
                .quantity(10)
                .build();
    }

}