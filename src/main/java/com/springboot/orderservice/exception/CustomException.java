package com.springboot.orderservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomException extends RuntimeException{
    private String errorCode;
    private int status;

    public CustomException(String message,String errorCode,int status){
        super(message);
        this.errorCode=errorCode;
        this.status=status;
    }
}
