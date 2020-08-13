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
        for (int i = 0; i < 20; i++) {
            System.out.println(i + " hello, args: " + Arrays.asList(args));
            map.put("num", String.valueOf(i));
            stateListener.updateState(map);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void kill(StateListener stateListener) {

    }

    @Override
    public void close() {

    }
}
