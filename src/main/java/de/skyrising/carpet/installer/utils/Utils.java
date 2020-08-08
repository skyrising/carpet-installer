package de.skyrising.carpet.installer.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class Utils {
    private Utils(){}

    public static <T> CompletableFuture<T> wrap(Consumer<Consumer<T>> async) {
        CompletableFuture<T> future = new CompletableFuture<>();
        async.accept(future::complete);
        return future;
    }
}
