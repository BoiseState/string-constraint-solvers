package extendedSolvers;

import java.math.BigInteger;

/**
 * This interface defines methods for obtaining a model count of the symbolic
 * representation of a string.
 */
public interface IModelCounter<TSymbolicString> {

    /**
     * Gets the model count of the symbolic representation of a string
     * specified by the id.
     * @param symbolicString The symbolic string for which the model count is
     *                       computed.
     * @return The model count of the symbolic string model.
     */
    BigInteger getModelCount(TSymbolicString symbolicString);

    /**
     * Gets the model count of the symbolic representation of a string
     * specified by the id. The model is restricted for strings up to the
     * specified length before counting.
     * @param symbolicString The symbolic string for which the model count is
     *                       computed
     * @param length The maximum length of strings represented by the symbolic
     *               string model which will be counted.
     * @return The model count of the restricted symbolic string model.
     */
    BigInteger getModelCount(TSymbolicString symbolicString, int length);
}
