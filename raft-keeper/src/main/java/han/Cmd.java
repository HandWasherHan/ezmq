package han;

import java.util.function.Function;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@FunctionalInterface
public interface Cmd<T, V> extends Function<T, V> {
}
