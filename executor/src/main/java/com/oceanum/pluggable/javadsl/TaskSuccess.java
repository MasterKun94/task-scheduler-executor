package com.oceanum.pluggable.javadsl;

import java.util.Optional;

public class TaskSuccess implements TaskComplete {
    private PluggableState finalState;

    @Override
    public Optional<PluggableState> getFinalState() {
        return Optional.ofNullable(finalState);
    }

    public void setFinalState(PluggableState finalState) {
        this.finalState = finalState;
    }
}
