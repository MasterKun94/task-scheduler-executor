package com.oceanum.pluggable.javadsl;

import com.oceanum.pluggable.ExecutionProxy;
import com.oceanum.pluggable.TaskComplete;

import java.util.Map;
import java.util.function.Function;

public abstract class PluggableExecutor {
    private ExecutionProxy proxy;

    public void setProxy(Function<PluggableExecutor, ExecutionProxy> proxyFunc) {
        this.proxy = proxyFunc.apply(this);
    }

    public void updateInfo(PluggableInfo info) {
        proxy.updateInfo(info);
    }

    public void updateState(PluggableState state) {
        proxy.updateState(state);
    }

    public abstract TaskComplete startRun(Map<String, Object> args);

    public abstract boolean kill();

    public abstract boolean isKilled();
}
