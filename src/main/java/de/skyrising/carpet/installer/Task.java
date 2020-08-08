package de.skyrising.carpet.installer;

@FunctionalInterface
public interface Task {
    boolean run();

    default String getName() {
        return toString();
    }

    default boolean hasProgress() {
        return false;
    }

    default double getProgress() {
        return -1;
    }

    default boolean cancel() {
        return false;
    }
}
