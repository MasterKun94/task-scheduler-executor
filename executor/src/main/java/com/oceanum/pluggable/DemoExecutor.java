package com.oceanum.pluggable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DemoExecutor implements Executor {
    private Map<String, String> map = new HashMap<>();
    {
        map.put("name", "demo");
    }

    @Override
    public void run(String[] args, StateListener stateListener) {
        stateListener.updateState(map);
        System.out.println("hello, args: " + Arrays.asList(args));
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void kill(StateListener stateListener) {

    }

    @Override
    public void close() {

    }
}
