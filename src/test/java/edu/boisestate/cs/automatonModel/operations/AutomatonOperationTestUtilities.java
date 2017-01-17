package edu.boisestate.cs.automatonModel.operations;

public class AutomatonOperationTestUtilities {

    public static Object[] addAutomatonTestInstance(int numStates,
                                           int numAcceptingStates,
                                           int numTransitions,
                                           int numAcceptedStrings,
                                           Object... args) {
        // initialize return object array
        Object[] returnArray = new Object[args.length + 4];

        // set array values from parameters
        returnArray[0] = numStates;
        returnArray[1] = numAcceptingStates;
        returnArray[2] = numTransitions;
        returnArray[3] = numAcceptedStrings;

        // copy args array to return array with offset of 4
        System.arraycopy(args, 0, returnArray, 4, args.length);

        // return array
        return returnArray;
    }

    public static Object[] addAutomataTestInstance(int[] numStatesArray,
                                           int[] numAcceptingStatesArray,
                                           int[] numTransitionsArray,
                                           int[] numAcceptedStringsArray,
                                           Object... args) {
        // initialize return object array
        Object[] returnArray = new Object[args.length + 4];

        // set array values from parameters
        returnArray[0] = numStatesArray;
        returnArray[1] = numAcceptingStatesArray;
        returnArray[2] = numTransitionsArray;
        returnArray[3] = numAcceptedStringsArray;

        // copy args array to return array with offset of 4
        System.arraycopy(args, 0, returnArray, 4, args.length);

        // return array
        return returnArray;
    }
}
