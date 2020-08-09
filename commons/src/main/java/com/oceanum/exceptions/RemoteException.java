package com.oceanum.exceptions;

/**
 * @author chenmingkun
 * @date 2020/8/9
 */
public class RemoteException extends Exception {
    public RemoteException() {
        super();
    }

    public RemoteException(String msg) {
        super(msg);
    }

    public RemoteException(String msg, Throwable e) {
        super(msg, e);
    }

    public RemoteException(Throwable e) {
        super(e);
    }
}
