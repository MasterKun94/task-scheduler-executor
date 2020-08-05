package com.oceanum.exceptions;

public class VersionOutdatedException extends Exception {
    public VersionOutdatedException(String msg) {
        super(msg);
    }
}
