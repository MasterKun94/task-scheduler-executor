package com.oceanum.common;

/**
 * @author chenmingkun
 * @date 2020/8/9
 */
public enum  TaskStatus {

    OFFLINE(0),
    PREPARE(1),
    START(2),
    RUNNING(3),
    FAILED(4),
    SUCCESS(5),
    RETRY(6),
    TIMEOUT(7),
    KILL(8);

    int value;

    TaskStatus(int value) {
        this.value = value;
    }
}
