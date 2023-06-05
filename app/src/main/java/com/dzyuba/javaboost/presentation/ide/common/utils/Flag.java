package com.dzyuba.javaboost.presentation.ide.common.utils;

public class Flag {
    private boolean state = false;

    synchronized public final void set() {
        state = true;
    }

    synchronized public final void clear() {
        state = false;
    }

    synchronized public final boolean isSet() {
        return state;
    }
}
