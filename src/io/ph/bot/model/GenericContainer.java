package io.ph.bot.model;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericContainer<T>
implements Consumer<T>, Supplier<T> {

    private T t;

    public GenericContainer() {}

    public GenericContainer(T t) {
        this.t = t;
    }

    public T getVal() {
        return t;
    }

    public void setVal(T t) {
        this.t = t;
    }

    @Override
    public void accept(T t) {
        this.t = t;
    }

    @Override
    public T get() {
        return t;
    }
}
