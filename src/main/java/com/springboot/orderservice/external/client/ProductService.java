package com.springboot.orderservice.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="PRODUCT-SERVICE/product")
public interface ProductService {
    //http://localhost:8080/product/reduceQuantity/1?quantity=10
    @PutMapping("/reduceQuantity/{id}")
    public ResponseEntity<String> reduceQuantity(@PathVariable("id") long productId,
                                                 @RequestParam long quantity);
}
