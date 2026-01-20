package com.dave.Main.State.Observe;

public interface Observer<E extends StateEvent> {

    void onNotify(E event);

}
