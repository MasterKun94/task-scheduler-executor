package com.oceanum.pluggable;

public interface Executor {

    void run(String[] args, StateListener listener) throws Throwable;

    boolean isRunning();

    void kill(StateListener stateListener) throws Throwable;

    void close();
}
