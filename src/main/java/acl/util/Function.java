package acl.util;

/**
 * Determines an output value based on an input value.
 * 
 * @param <T1> the type of the input 
 * @param <T2> the type of the output 
 * @author Petr Giecek
 */
public interface Function<T1, T2> {

    /**
     * Returns the result of applying this function to input.
     * 
     * @param input the function input
     * @return the result of applying this function to input
     */
    public T2 apply(T1 input);
}
