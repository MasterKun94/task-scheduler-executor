package com.oceanum.pluggable.javadsl;

import java.util.Optional;

public class TaskFailed implements TaskComplete {
    private PluggableState finalState;
    private Throwable e;

    @Override
    public Optional<PluggableState> getFinalState() {
        return Optional.ofNullable(finalState);
    }

    public void setFinalState(PluggableState finalState) {
        this.finalState = finalState;
    }

    public Throwable getE() {
        return e;
    }

    public void setE(Throwable e) {
        this.e = e;
    }
}
