package com.oceanum.pluggable.javadsl;

import java.util.Optional;

public interface TaskComplete {
    Optional<PluggableState> getFinalState();
}
