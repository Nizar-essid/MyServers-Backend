package com.myservers.backend.exceptions;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

public class ApiRequestException extends RuntimeException  {
public HttpStatus httpStatus;
    public ApiRequestException(String message,HttpStatus httpStatus) {
        super(message);
        this.httpStatus=httpStatus;


    }

    public ApiRequestException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus=httpStatus;

    }
}




