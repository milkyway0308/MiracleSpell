package skywolf46.miraclespell.util;

import java.util.concurrent.Future;

public interface ObservableFuture<V> extends Future<V> {
    V observe();
}
