package com.atguigu.gmall.common.exception;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 11:06
 */
public class OrderException extends RuntimeException{
    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
