package com.oceanum.pluggable;

public interface Executor {

    void run(String[] args, StateListener listener);

    boolean isRunning();

    void kill(StateListener stateListener);

    void close();
}
