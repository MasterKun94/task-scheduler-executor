package com.oceanum.exceptions;

public class VersionOutdatedException extends Exception {
    public VersionOutdatedException() {
        super();
    }

    public VersionOutdatedException(String msg) {
        super(msg);
    }

    public VersionOutdatedException(String msg, Throwable e) {
        super(msg, e);
    }

    public VersionOutdatedException(Throwable e) {
        super(e);
    }
}
