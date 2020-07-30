package com.oceanum.pluggable.javadsl;

import java.util.Map;

public class PluggableState {
    private String state;
    private Map<String, Object> desc;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getDesc() {
        return desc;
    }

    public void setDesc(Map<String, Object> desc) {
        this.desc = desc;
    }
}
