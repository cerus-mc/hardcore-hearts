package dev.cerus.hardcorehearts;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ITextFilter;

/**
 * Simple ITextFilter implementation that routes everything to the original
 * filter and calls a callback when our entry method gets called
 */
public class SpyingTextFilter implements ITextFilter {

    private final ITextFilter backing;
    private Runnable callback;

    public SpyingTextFilter(final ITextFilter backing, final Runnable callback) {
        this.backing = backing;
        this.callback = callback;
    }

    @Override
    public void a() {
        this.backing.a();

        // Prevent multiple injections
        if (this.callback != null) {
            this.callback.run();
            this.callback = null;
        }
    }

    @Override
    public void b() {
        this.backing.b();
    }

    @Override
    public CompletableFuture<FilteredText<String>> a(final String s) {
        return this.backing.a(s);
    }

    @Override
    public CompletableFuture<List<FilteredText<String>>> a(final List<String> list) {
        return this.backing.a(list);
    }

}
