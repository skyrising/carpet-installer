package de.skyrising.carpet.installer.utils;

public class ListItemWrapper<T> {
    public final T item;
    public final String label;

    public ListItemWrapper(T item, String label) {
        this.item = item;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
