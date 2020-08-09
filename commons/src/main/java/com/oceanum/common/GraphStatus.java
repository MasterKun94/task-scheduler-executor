package com.oceanum.common;

/**
 * @author chenmingkun
 * @date 2020/8/9
 */
public enum GraphStatus {
    OFFLINE(0),
    RUNNING(1),
    SUCCESS(2),
    EXCEPTION(3),
    FAILED(4),
    KILLED(5);

    int value;

    GraphStatus(int value) {
        this.value = value;
    }
}
