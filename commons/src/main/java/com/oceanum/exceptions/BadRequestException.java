package com.oceanum.exceptions;

/**
 * @author chenmingkun
 * @date 2020/8/9
 */
public class BadRequestException extends Exception {
    public BadRequestException() {
        super();
    }

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(String msg, Throwable e) {
        super(msg, e);
    }

    public BadRequestException(Throwable e) {
        super(e);
    }
}
