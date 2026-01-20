package com.dave.Main.State.Observe;

public interface Observable<T extends StateEvent> {

    void addObserver(Observer<T> o);

    void removeObserver(Observer<T> o);

    void notifyObservers(T event);

}
