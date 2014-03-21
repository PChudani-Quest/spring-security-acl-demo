package acl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility methods pertaining to collections.
 * 
 * @author Petr Giecek
 */
public final class CollectionUtils {

    /**
     * Returns a list that applies {@code function} to each element of {@code inputs}.
     * 
     * @param inputs the list of input elements
     * @param function the function to be applied
     * @return the list of transformed elements
     */
    public static <E1, E2> List<E2> transform(List<E1> inputs, Function<? super E1, ? extends E2> function) {
        final List<E2> transformed = new ArrayList<>(inputs.size());
        for (E1 input : inputs) {
            final E2 output = function.apply(input);
            transformed.add(output);
        }
        return transformed;
    }

 
}
