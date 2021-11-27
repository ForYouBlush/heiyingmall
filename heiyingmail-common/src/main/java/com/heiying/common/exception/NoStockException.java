package com.heiying.common.exception;

public class NoStockException extends RuntimeException{
    public NoStockException() {
        super("库存不足");
    }
}
