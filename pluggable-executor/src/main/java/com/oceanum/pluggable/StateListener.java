package com.oceanum.pluggable;

import java.util.Map;

public interface StateListener {
    void updateState(Map<String, String> info);
}
