package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;

public abstract class AutomatonModelManager {

    static protected AutomatonModelManager instance = null;
    protected Alphabet alphabet;

    public Alphabet getAlphabet() {
        return this.alphabet;
    }

    static public AutomatonModelManager getInstance(Alphabet alphabet,
                                                    int modelVersion,
                                                    int initialBoundLength) {

        if (modelVersion == 1) {
            UnboundedAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        } else if (modelVersion == 2) {
            BoundedAutomatonModelManager.setInstance(alphabet,
                                                     initialBoundLength);
        } else if (modelVersion == 3) {
            AggregateAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        } else if (modelVersion == 4) {
            WeightedAutomatonModelManager.setInstance(alphabet,
                                                      initialBoundLength);
        } else if (modelVersion == 5) {
        	AcyclicWeightedAutomatonModelManager.setInstance(alphabet, 
        											initialBoundLength);
        }

        return instance;
    }

    /**
     * Create a new automaton model from a concrete string
     * @param string
     * @return
     */
    public abstract AutomatonModel createString(String string);

    /**
     * Create a new symbolic string from 0 to up to a certain length
     * @param initialBound the upper bound of the lenght (inlcusive)
     * @return
     */
    public abstract AutomatonModel createAnyString(int initialBound);

    /**
     * A string with no upper bound - for unbounded models
     * @return
     */
    public abstract AutomatonModel createAnyString();

    /**
     * Creates a symbolic string with length from min to max (both inclusive)
     * @param min
     * @param max
     * @return
     */
    public abstract AutomatonModel createAnyString(int min, int max);
}
