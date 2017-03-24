package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedAutomaton_When_UnioningACollection {

    @Parameter(value = 4)
    public WeightedAutomaton argAutomaton;
    @Parameter(value = 3)
    public WeightedAutomaton baseAutomaton;
    @Parameter(value = 2)
    public int expectedModelCount;
    @Parameter(value = 1)
    public String argDescription;
    @Parameter(value = 0) // first data value (0) is default
    public String baseDescription;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.union(<{1} Automaton>) -> Expected MC = {2}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton uniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton nonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton balancedUniform0 = balanced_Uniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedUniform1 = balanced_Uniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedUniform2 = balanced_Uniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedUniform3 = balanced_Uniform_WeightedAutomaton(alphabet, 3);
        WeightedAutomaton balancedNonUniform0 = balanced_NonUniform_WeightedAutomaton(alphabet, 0);
        WeightedAutomaton balancedNonUniform1 = balanced_NonUniform_WeightedAutomaton(alphabet, 1);
        WeightedAutomaton balancedNonUniform2 = balanced_NonUniform_WeightedAutomaton(alphabet, 2);
        WeightedAutomaton balancedNonUniform3 = balanced_NonUniform_WeightedAutomaton(alphabet, 3);
        WeightedAutomaton unbalancedUniform0 = unbalanced_Uniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedUniform1 = unbalanced_Uniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedUniform2 = unbalanced_Uniform_WeightedAutomaton_2();
        WeightedAutomaton unbalancedNonUniform0 = unbalanced_NonUniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedNonUniform1 = unbalanced_NonUniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedNonUniform2 = unbalanced_NonUniform_WeightedAutomaton_2();

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, empty, empty},
                {"Empty", "Empty String", 1, empty, emptyString},
                {"Empty", "Concrete", 1, empty, concrete},
                {"Empty", "Uniform", 85, empty, uniform},
                {"Empty", "Non-Uniform", 45, empty, nonUniform},
                {"Empty", "Balanced Uniform 0", 1, empty, balancedUniform0},
                {"Empty", "Balanced Uniform 1", 4, empty, balancedUniform1},
                {"Empty", "Balanced Uniform 2", 16, empty, balancedUniform2},
                {"Empty", "Balanced Uniform 3", 64, empty, balancedUniform3},
                {"Empty", "Balanced Non-Uniform 0", 0, empty, balancedNonUniform0},
                {"Empty", "Balanced Non-Uniform 1", 1, empty, balancedNonUniform1},
                {"Empty", "Balanced Non-Uniform 2", 7, empty, balancedNonUniform2},
                {"Empty", "Balanced Non-Uniform 3", 37, empty, balancedNonUniform3},
                {"Empty", "Unbalanced Uniform 0", 64, empty, unbalancedUniform0},
                {"Empty", "Unbalanced Uniform 1", 64, empty, unbalancedUniform1},
                {"Empty", "Unbalanced Uniform 2", 64, empty, unbalancedUniform2},
                {"Empty", "Unbalanced Non-Uniform 0", 37, empty, unbalancedNonUniform0},
                {"Empty", "Unbalanced Non-Uniform 1", 37, empty, unbalancedNonUniform1},
                {"Empty", "Unbalanced Non-Uniform 2", 37, empty, unbalancedNonUniform2},
                {"Empty String", "Empty", 1, emptyString, empty},
                {"Empty String", "Empty String", 2, emptyString, emptyString},
                {"Empty String", "Concrete", 2, emptyString, concrete},
                {"Empty String", "Uniform", 86, emptyString, uniform},
                {"Empty String", "Non-Uniform", 46, emptyString, nonUniform},
                {"Empty String", "Balanced Uniform 0", 2, emptyString, balancedUniform0},
                {"Empty String", "Balanced Uniform 1", 5, emptyString, balancedUniform1},
                {"Empty String", "Balanced Uniform 2", 17, emptyString, balancedUniform2},
                {"Empty String", "Balanced Uniform 3", 65, emptyString, balancedUniform3},
                {"Empty String", "Balanced Non-Uniform 0", 1, emptyString, balancedNonUniform0},
                {"Empty String", "Balanced Non-Uniform 1", 2, emptyString, balancedNonUniform1},
                {"Empty String", "Balanced Non-Uniform 2", 8, emptyString, balancedNonUniform2},
                {"Empty String", "Balanced Non-Uniform 3", 38, emptyString, balancedNonUniform3},
                {"Empty String", "Unbalanced Uniform 0", 65, emptyString, unbalancedUniform0},
                {"Empty String", "Unbalanced Uniform 1", 65, emptyString, unbalancedUniform1},
                {"Empty String", "Unbalanced Uniform 2", 65, emptyString, unbalancedUniform2},
                {"Empty String", "Unbalanced Non-Uniform 0", 38, emptyString, unbalancedNonUniform0},
                {"Empty String", "Unbalanced Non-Uniform 1", 38, emptyString, unbalancedNonUniform1},
                {"Empty String", "Unbalanced Non-Uniform 2", 38, emptyString, unbalancedNonUniform2},
                {"Concrete", "Empty", 1, concrete, empty},
                {"Concrete", "Empty String", 2, concrete, emptyString},
                {"Concrete", "Concrete", 2, concrete, concrete},
                {"Concrete", "Uniform", 86, concrete, uniform},
                {"Concrete", "Non-Uniform", 46, concrete, nonUniform},
                {"Concrete", "Balanced Uniform 0", 2, concrete, balancedUniform0},
                {"Concrete", "Balanced Uniform 1", 5, concrete, balancedUniform1},
                {"Concrete", "Balanced Uniform 2", 17, concrete, balancedUniform2},
                {"Concrete", "Balanced Uniform 3", 65, concrete, balancedUniform3},
                {"Concrete", "Balanced Non-Uniform 0", 1, concrete, balancedNonUniform0},
                {"Concrete", "Balanced Non-Uniform 1", 2, concrete, balancedNonUniform1},
                {"Concrete", "Balanced Non-Uniform 2", 8, concrete, balancedNonUniform2},
                {"Concrete", "Balanced Non-Uniform 3", 38, concrete, balancedNonUniform3},
                {"Concrete", "Unbalanced Uniform 0", 65, concrete, unbalancedUniform0},
                {"Concrete", "Unbalanced Uniform 1", 65, concrete, unbalancedUniform1},
                {"Concrete", "Unbalanced Uniform 2", 65, concrete, unbalancedUniform2},
                {"Concrete", "Unbalanced Non-Uniform 0", 38, concrete, unbalancedNonUniform0},
                {"Concrete", "Unbalanced Non-Uniform 1", 38, concrete, unbalancedNonUniform1},
                {"Concrete", "Unbalanced Non-Uniform 2", 38, concrete, unbalancedNonUniform2},
                {"Uniform", "Empty", 85, uniform, empty},
                {"Uniform", "Empty String", 86, uniform, emptyString},
                {"Uniform", "Concrete", 86, uniform, concrete},
                {"Uniform", "Uniform", 170, uniform, uniform},
                {"Uniform", "Non-Uniform", 130, uniform, nonUniform},
                {"Uniform", "Balanced Uniform 0", 86, uniform, balancedUniform0},
                {"Uniform", "Balanced Uniform 1", 89, uniform, balancedUniform1},
                {"Uniform", "Balanced Uniform 2", 101, uniform, balancedUniform2},
                {"Uniform", "Balanced Uniform 3", 149, uniform, balancedUniform3},
                {"Uniform", "Balanced Non-Uniform 0", 85, uniform, balancedNonUniform0},
                {"Uniform", "Balanced Non-Uniform 1", 86, uniform, balancedNonUniform1},
                {"Uniform", "Balanced Non-Uniform 2", 92, uniform, balancedNonUniform2},
                {"Uniform", "Balanced Non-Uniform 3", 122, uniform, balancedNonUniform3},
                {"Uniform", "Unbalanced Uniform 0", 149, uniform, unbalancedUniform0},
                {"Uniform", "Unbalanced Uniform 1", 149, uniform, unbalancedUniform1},
                {"Uniform", "Unbalanced Uniform 2", 149, uniform, unbalancedUniform2},
                {"Uniform", "Unbalanced Non-Uniform 0", 122, uniform, unbalancedNonUniform0},
                {"Uniform", "Unbalanced Non-Uniform 1", 122, uniform, unbalancedNonUniform1},
                {"Uniform", "Unbalanced Non-Uniform 2", 122, uniform, unbalancedNonUniform2},
                {"Non-Uniform", "Empty", 45, nonUniform, empty},
                {"Non-Uniform", "Empty String", 46, nonUniform, emptyString},
                {"Non-Uniform", "Concrete", 46, nonUniform, concrete},
                {"Non-Uniform", "Uniform", 130, nonUniform, uniform},
                {"Non-Uniform", "Non-Uniform", 90, nonUniform, nonUniform},
                {"Non-Uniform", "Balanced Uniform 0", 46, nonUniform, balancedUniform0},
                {"Non-Uniform", "Balanced Uniform 1", 49, nonUniform, balancedUniform1},
                {"Non-Uniform", "Balanced Uniform 2", 61, nonUniform, balancedUniform2},
                {"Non-Uniform", "Balanced Uniform 3", 109, nonUniform, balancedUniform3},
                {"Non-Uniform", "Balanced Non-Uniform 0", 45, nonUniform, balancedNonUniform0},
                {"Non-Uniform", "Balanced Non-Uniform 1", 46, nonUniform, balancedNonUniform1},
                {"Non-Uniform", "Balanced Non-Uniform 2", 52, nonUniform, balancedNonUniform2},
                {"Non-Uniform", "Balanced Non-Uniform 3", 82, nonUniform, balancedNonUniform3},
                {"Non-Uniform", "Unbalanced Uniform 0", 109, nonUniform, unbalancedUniform0},
                {"Non-Uniform", "Unbalanced Uniform 1", 109, nonUniform, unbalancedUniform1},
                {"Non-Uniform", "Unbalanced Uniform 2", 109, nonUniform, unbalancedUniform2},
                {"Non-Uniform", "Unbalanced Non-Uniform 0", 82, nonUniform, unbalancedNonUniform0},
                {"Non-Uniform", "Unbalanced Non-Uniform 1", 82, nonUniform, unbalancedNonUniform1},
                {"Non-Uniform", "Unbalanced Non-Uniform 2", 82, nonUniform, unbalancedNonUniform2},
                {"Balanced Uniform 0", "Empty", 1, balancedUniform0, empty},
                {"Balanced Uniform 0", "Empty String", 2, balancedUniform0, emptyString},
                {"Balanced Uniform 0", "Concrete", 2, balancedUniform0, concrete},
                {"Balanced Uniform 0", "Uniform", 86, balancedUniform0, uniform},
                {"Balanced Uniform 0", "Non-Uniform", 46, balancedUniform0, nonUniform},
                {"Balanced Uniform 0", "Balanced Uniform 0", 2, balancedUniform0, balancedUniform0},
                {"Balanced Uniform 0", "Balanced Uniform 1", 5, balancedUniform0, balancedUniform1},
                {"Balanced Uniform 0", "Balanced Uniform 2", 17, balancedUniform0, balancedUniform2},
                {"Balanced Uniform 0", "Balanced Uniform 3", 65, balancedUniform0, balancedUniform3},
                {"Balanced Uniform 0", "Balanced Non-Uniform 0", 1, balancedUniform0, balancedNonUniform0},
                {"Balanced Uniform 0", "Balanced Non-Uniform 1", 2, balancedUniform0, balancedNonUniform1},
                {"Balanced Uniform 0", "Balanced Non-Uniform 2", 8, balancedUniform0, balancedNonUniform2},
                {"Balanced Uniform 0", "Balanced Non-Uniform 3", 38, balancedUniform0, balancedNonUniform3},
                {"Balanced Uniform 0", "Unbalanced Uniform 0", 65, balancedUniform0, unbalancedUniform0},
                {"Balanced Uniform 0", "Unbalanced Uniform 1", 65, balancedUniform0, unbalancedUniform1},
                {"Balanced Uniform 0", "Unbalanced Uniform 2", 65, balancedUniform0, unbalancedUniform2},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 0", 38, balancedUniform0, unbalancedNonUniform0},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 1", 38, balancedUniform0, unbalancedNonUniform1},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 2", 38, balancedUniform0, unbalancedNonUniform2},
                {"Balanced Uniform 1", "Empty", 4, balancedUniform1, empty},
                {"Balanced Uniform 1", "Empty String", 5, balancedUniform1, emptyString},
                {"Balanced Uniform 1", "Concrete", 5, balancedUniform1, concrete},
                {"Balanced Uniform 1", "Uniform", 89, balancedUniform1, uniform},
                {"Balanced Uniform 1", "Non-Uniform", 49, balancedUniform1, nonUniform},
                {"Balanced Uniform 1", "Balanced Uniform 0", 5, balancedUniform1, balancedUniform0},
                {"Balanced Uniform 1", "Balanced Uniform 1", 8, balancedUniform1, balancedUniform1},
                {"Balanced Uniform 1", "Balanced Uniform 2", 20, balancedUniform1, balancedUniform2},
                {"Balanced Uniform 1", "Balanced Uniform 3", 68, balancedUniform1, balancedUniform3},
                {"Balanced Uniform 1", "Balanced Non-Uniform 0", 4, balancedUniform1, balancedNonUniform0},
                {"Balanced Uniform 1", "Balanced Non-Uniform 1", 5, balancedUniform1, balancedNonUniform1},
                {"Balanced Uniform 1", "Balanced Non-Uniform 2", 11, balancedUniform1, balancedNonUniform2},
                {"Balanced Uniform 1", "Balanced Non-Uniform 3", 41, balancedUniform1, balancedNonUniform3},
                {"Balanced Uniform 1", "Unbalanced Uniform 0", 68, balancedUniform1, unbalancedUniform0},
                {"Balanced Uniform 1", "Unbalanced Uniform 1", 68, balancedUniform1, unbalancedUniform1},
                {"Balanced Uniform 1", "Unbalanced Uniform 2", 68, balancedUniform1, unbalancedUniform2},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 0", 41, balancedUniform1, unbalancedNonUniform0},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 1", 41, balancedUniform1, unbalancedNonUniform1},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 2", 41, balancedUniform1, unbalancedNonUniform2},
                {"Balanced Uniform 2", "Empty", 16, balancedUniform2, empty},
                {"Balanced Uniform 2", "Empty String", 17, balancedUniform2, emptyString},
                {"Balanced Uniform 2", "Concrete", 17, balancedUniform2, concrete},
                {"Balanced Uniform 2", "Uniform", 101, balancedUniform2, uniform},
                {"Balanced Uniform 2", "Non-Uniform", 61, balancedUniform2, nonUniform},
                {"Balanced Uniform 2", "Balanced Uniform 0", 17, balancedUniform2, balancedUniform0},
                {"Balanced Uniform 2", "Balanced Uniform 1", 20, balancedUniform2, balancedUniform1},
                {"Balanced Uniform 2", "Balanced Uniform 2", 32, balancedUniform2, balancedUniform2},
                {"Balanced Uniform 2", "Balanced Uniform 3", 80, balancedUniform2, balancedUniform3},
                {"Balanced Uniform 2", "Balanced Non-Uniform 0", 16, balancedUniform2, balancedNonUniform0},
                {"Balanced Uniform 2", "Balanced Non-Uniform 1", 17, balancedUniform2, balancedNonUniform1},
                {"Balanced Uniform 2", "Balanced Non-Uniform 2", 23, balancedUniform2, balancedNonUniform2},
                {"Balanced Uniform 2", "Balanced Non-Uniform 3", 53, balancedUniform2, balancedNonUniform3},
                {"Balanced Uniform 2", "Unbalanced Uniform 0", 80, balancedUniform2, unbalancedUniform0},
                {"Balanced Uniform 2", "Unbalanced Uniform 1", 80, balancedUniform2, unbalancedUniform1},
                {"Balanced Uniform 2", "Unbalanced Uniform 2", 80, balancedUniform2, unbalancedUniform2},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 0", 53, balancedUniform2, unbalancedNonUniform0},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 1", 53, balancedUniform2, unbalancedNonUniform1},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 2", 53, balancedUniform2, unbalancedNonUniform2},
                {"Balanced Uniform 3", "Empty", 64, balancedUniform3, empty},
                {"Balanced Uniform 3", "Empty String", 65, balancedUniform3, emptyString},
                {"Balanced Uniform 3", "Concrete", 65, balancedUniform3, concrete},
                {"Balanced Uniform 3", "Uniform", 149, balancedUniform3, uniform},
                {"Balanced Uniform 3", "Non-Uniform", 109, balancedUniform3, nonUniform},
                {"Balanced Uniform 3", "Balanced Uniform 0", 65, balancedUniform3, balancedUniform0},
                {"Balanced Uniform 3", "Balanced Uniform 1", 68, balancedUniform3, balancedUniform1},
                {"Balanced Uniform 3", "Balanced Uniform 2", 80, balancedUniform3, balancedUniform2},
                {"Balanced Uniform 3", "Balanced Uniform 3", 128, balancedUniform3, balancedUniform3},
                {"Balanced Uniform 3", "Balanced Non-Uniform 0", 64, balancedUniform3, balancedNonUniform0},
                {"Balanced Uniform 3", "Balanced Non-Uniform 1", 65, balancedUniform3, balancedNonUniform1},
                {"Balanced Uniform 3", "Balanced Non-Uniform 2", 71, balancedUniform3, balancedNonUniform2},
                {"Balanced Uniform 3", "Balanced Non-Uniform 3", 101, balancedUniform3, balancedNonUniform3},
                {"Balanced Uniform 3", "Unbalanced Uniform 0", 128, balancedUniform3, unbalancedUniform0},
                {"Balanced Uniform 3", "Unbalanced Uniform 1", 128, balancedUniform3, unbalancedUniform1},
                {"Balanced Uniform 3", "Unbalanced Uniform 2", 128, balancedUniform3, unbalancedUniform2},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 0", 101, balancedUniform3, unbalancedNonUniform0},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 1", 101, balancedUniform3, unbalancedNonUniform1},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 2", 101, balancedUniform3, unbalancedNonUniform2},
                {"Balanced Non-Uniform 0", "Empty", 0, balancedNonUniform0, empty},
                {"Balanced Non-Uniform 0", "Empty String", 1, balancedNonUniform0, emptyString},
                {"Balanced Non-Uniform 0", "Concrete", 1, balancedNonUniform0, concrete},
                {"Balanced Non-Uniform 0", "Uniform", 85, balancedNonUniform0, uniform},
                {"Balanced Non-Uniform 0", "Non-Uniform", 45, balancedNonUniform0, nonUniform},
                {"Balanced Non-Uniform 0", "Balanced Uniform 0", 1, balancedNonUniform0, balancedUniform0},
                {"Balanced Non-Uniform 0", "Balanced Uniform 1", 4, balancedNonUniform0, balancedUniform1},
                {"Balanced Non-Uniform 0", "Balanced Uniform 2", 16, balancedNonUniform0, balancedUniform2},
                {"Balanced Non-Uniform 0", "Balanced Uniform 3", 64, balancedNonUniform0, balancedUniform3},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 0", 0, balancedNonUniform0, balancedNonUniform0},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 1", 1, balancedNonUniform0, balancedNonUniform1},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 2", 7, balancedNonUniform0, balancedNonUniform2},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 3", 37, balancedNonUniform0, balancedNonUniform3},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 0", 64, balancedNonUniform0, unbalancedUniform0},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 1", 64, balancedNonUniform0, unbalancedUniform1},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 2", 64, balancedNonUniform0, unbalancedUniform2},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 0", 37, balancedNonUniform0, unbalancedNonUniform0},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 1", 37, balancedNonUniform0, unbalancedNonUniform1},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 2", 37, balancedNonUniform0, unbalancedNonUniform2},
                {"Balanced Non-Uniform 1", "Empty", 1, balancedNonUniform1, empty},
                {"Balanced Non-Uniform 1", "Empty String", 2, balancedNonUniform1, emptyString},
                {"Balanced Non-Uniform 1", "Concrete", 2, balancedNonUniform1, concrete},
                {"Balanced Non-Uniform 1", "Uniform", 86, balancedNonUniform1, uniform},
                {"Balanced Non-Uniform 1", "Non-Uniform", 46, balancedNonUniform1, nonUniform},
                {"Balanced Non-Uniform 1", "Balanced Uniform 0", 2, balancedNonUniform1, balancedUniform0},
                {"Balanced Non-Uniform 1", "Balanced Uniform 1", 5, balancedNonUniform1, balancedUniform1},
                {"Balanced Non-Uniform 1", "Balanced Uniform 2", 17, balancedNonUniform1, balancedUniform2},
                {"Balanced Non-Uniform 1", "Balanced Uniform 3", 65, balancedNonUniform1, balancedUniform3},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 0", 1, balancedNonUniform1, balancedNonUniform0},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 1", 2, balancedNonUniform1, balancedNonUniform1},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 2", 8, balancedNonUniform1, balancedNonUniform2},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 3", 38, balancedNonUniform1, balancedNonUniform3},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 0", 65, balancedNonUniform1, unbalancedUniform0},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 1", 65, balancedNonUniform1, unbalancedUniform1},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 2", 65, balancedNonUniform1, unbalancedUniform2},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 0", 38, balancedNonUniform1, unbalancedNonUniform0},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 1", 38, balancedNonUniform1, unbalancedNonUniform1},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 2", 38, balancedNonUniform1, unbalancedNonUniform2},
                {"Balanced Non-Uniform 2", "Empty", 7, balancedNonUniform2, empty},
                {"Balanced Non-Uniform 2", "Empty String", 8, balancedNonUniform2, emptyString},
                {"Balanced Non-Uniform 2", "Concrete", 8, balancedNonUniform2, concrete},
                {"Balanced Non-Uniform 2", "Uniform", 92, balancedNonUniform2, uniform},
                {"Balanced Non-Uniform 2", "Non-Uniform", 52, balancedNonUniform2, nonUniform},
                {"Balanced Non-Uniform 2", "Balanced Uniform 0", 8, balancedNonUniform2, balancedUniform0},
                {"Balanced Non-Uniform 2", "Balanced Uniform 1", 11, balancedNonUniform2, balancedUniform1},
                {"Balanced Non-Uniform 2", "Balanced Uniform 2", 23, balancedNonUniform2, balancedUniform2},
                {"Balanced Non-Uniform 2", "Balanced Uniform 3", 71, balancedNonUniform2, balancedUniform3},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 0", 7, balancedNonUniform2, balancedNonUniform0},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 1", 8, balancedNonUniform2, balancedNonUniform1},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 2", 14, balancedNonUniform2, balancedNonUniform2},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 3", 44, balancedNonUniform2, balancedNonUniform3},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 0", 71, balancedNonUniform2, unbalancedUniform0},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 1", 71, balancedNonUniform2, unbalancedUniform1},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 2", 71, balancedNonUniform2, unbalancedUniform2},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 0", 44, balancedNonUniform2, unbalancedNonUniform0},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 1", 44, balancedNonUniform2, unbalancedNonUniform1},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 2", 44, balancedNonUniform2, unbalancedNonUniform2},
                {"Balanced Non-Uniform 3", "Empty", 37, balancedNonUniform3, empty},
                {"Balanced Non-Uniform 3", "Empty String", 38, balancedNonUniform3, emptyString},
                {"Balanced Non-Uniform 3", "Concrete", 38, balancedNonUniform3, concrete},
                {"Balanced Non-Uniform 3", "Uniform", 122, balancedNonUniform3, uniform},
                {"Balanced Non-Uniform 3", "Non-Uniform", 82, balancedNonUniform3, nonUniform},
                {"Balanced Non-Uniform 3", "Balanced Uniform 0", 38, balancedNonUniform3, balancedUniform0},
                {"Balanced Non-Uniform 3", "Balanced Uniform 1", 41, balancedNonUniform3, balancedUniform1},
                {"Balanced Non-Uniform 3", "Balanced Uniform 2", 53, balancedNonUniform3, balancedUniform2},
                {"Balanced Non-Uniform 3", "Balanced Uniform 3", 101, balancedNonUniform3, balancedUniform3},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 0", 37, balancedNonUniform3, balancedNonUniform0},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 1", 38, balancedNonUniform3, balancedNonUniform1},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 2", 44, balancedNonUniform3, balancedNonUniform2},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 3", 74, balancedNonUniform3, balancedNonUniform3},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 0", 101, balancedNonUniform3, unbalancedUniform0},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 1", 101, balancedNonUniform3, unbalancedUniform1},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 2", 101, balancedNonUniform3, unbalancedUniform2},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 0", 74, balancedNonUniform3, unbalancedNonUniform0},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 1", 74, balancedNonUniform3, unbalancedNonUniform1},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 2", 74, balancedNonUniform3, unbalancedNonUniform2},
                {"Unbalanced Uniform 0", "Empty", 64, unbalancedUniform0, empty},
                {"Unbalanced Uniform 0", "Empty String", 65, unbalancedUniform0, emptyString},
                {"Unbalanced Uniform 0", "Concrete", 65, unbalancedUniform0, concrete},
                {"Unbalanced Uniform 0", "Uniform", 149, unbalancedUniform0, uniform},
                {"Unbalanced Uniform 0", "Non-Uniform", 109, unbalancedUniform0, nonUniform},
                {"Unbalanced Uniform 0", "Balanced Uniform 0", 65, unbalancedUniform0, balancedUniform0},
                {"Unbalanced Uniform 0", "Balanced Uniform 1", 68, unbalancedUniform0, balancedUniform1},
                {"Unbalanced Uniform 0", "Balanced Uniform 2", 80, unbalancedUniform0, balancedUniform2},
                {"Unbalanced Uniform 0", "Balanced Uniform 3", 128, unbalancedUniform0, balancedUniform3},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 0", 64, unbalancedUniform0, balancedNonUniform0},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 1", 65, unbalancedUniform0, balancedNonUniform1},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 2", 71, unbalancedUniform0, balancedNonUniform2},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 3", 101, unbalancedUniform0, balancedNonUniform3},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 0", 128, unbalancedUniform0, unbalancedUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 1", 128, unbalancedUniform0, unbalancedUniform1},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 2", 128, unbalancedUniform0, unbalancedUniform2},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 0", 101, unbalancedUniform0, unbalancedNonUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 1", 101, unbalancedUniform0, unbalancedNonUniform1},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 2", 101, unbalancedUniform0, unbalancedNonUniform2},
                {"Unbalanced Uniform 1", "Empty", 64, unbalancedUniform1, empty},
                {"Unbalanced Uniform 1", "Empty String", 65, unbalancedUniform1, emptyString},
                {"Unbalanced Uniform 1", "Concrete", 65, unbalancedUniform1, concrete},
                {"Unbalanced Uniform 1", "Uniform", 149, unbalancedUniform1, uniform},
                {"Unbalanced Uniform 1", "Non-Uniform", 109, unbalancedUniform1, nonUniform},
                {"Unbalanced Uniform 1", "Balanced Uniform 0", 65, unbalancedUniform1, balancedUniform0},
                {"Unbalanced Uniform 1", "Balanced Uniform 1", 68, unbalancedUniform1, balancedUniform1},
                {"Unbalanced Uniform 1", "Balanced Uniform 2", 80, unbalancedUniform1, balancedUniform2},
                {"Unbalanced Uniform 1", "Balanced Uniform 3", 128, unbalancedUniform1, balancedUniform3},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 0", 64, unbalancedUniform1, balancedNonUniform0},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 1", 65, unbalancedUniform1, balancedNonUniform1},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 2", 71, unbalancedUniform1, balancedNonUniform2},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 3", 101, unbalancedUniform1, balancedNonUniform3},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 0", 128, unbalancedUniform1, unbalancedUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 1", 128, unbalancedUniform1, unbalancedUniform1},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 2", 128, unbalancedUniform1, unbalancedUniform2},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 0", 101, unbalancedUniform1, unbalancedNonUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 1", 101, unbalancedUniform1, unbalancedNonUniform1},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 2", 101, unbalancedUniform1, unbalancedNonUniform2},
                {"Unbalanced Uniform 2", "Empty", 64, unbalancedUniform2, empty},
                {"Unbalanced Uniform 2", "Empty String", 65, unbalancedUniform2, emptyString},
                {"Unbalanced Uniform 2", "Concrete", 65, unbalancedUniform2, concrete},
                {"Unbalanced Uniform 2", "Uniform", 149, unbalancedUniform2, uniform},
                {"Unbalanced Uniform 2", "Non-Uniform", 109, unbalancedUniform2, nonUniform},
                {"Unbalanced Uniform 2", "Balanced Uniform 0", 65, unbalancedUniform2, balancedUniform0},
                {"Unbalanced Uniform 2", "Balanced Uniform 1", 68, unbalancedUniform2, balancedUniform1},
                {"Unbalanced Uniform 2", "Balanced Uniform 2", 80, unbalancedUniform2, balancedUniform2},
                {"Unbalanced Uniform 2", "Balanced Uniform 3", 128, unbalancedUniform2, balancedUniform3},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 0", 64, unbalancedUniform2, balancedNonUniform0},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 1", 65, unbalancedUniform2, balancedNonUniform1},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 2", 71, unbalancedUniform2, balancedNonUniform2},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 3", 101, unbalancedUniform2, balancedNonUniform3},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 0", 128, unbalancedUniform2, unbalancedUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 1", 128, unbalancedUniform2, unbalancedUniform1},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 2", 128, unbalancedUniform2, unbalancedUniform2},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 0", 101, unbalancedUniform2, unbalancedNonUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 1", 101, unbalancedUniform2, unbalancedNonUniform1},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 2", 101, unbalancedUniform2, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 0", "Empty", 37, unbalancedNonUniform0, empty},
                {"Unbalanced Non-Uniform 0", "Empty String", 38, unbalancedNonUniform0, emptyString},
                {"Unbalanced Non-Uniform 0", "Concrete", 38, unbalancedNonUniform0, concrete},
                {"Unbalanced Non-Uniform 0", "Uniform", 122, unbalancedNonUniform0, uniform},
                {"Unbalanced Non-Uniform 0", "Non-Uniform", 82, unbalancedNonUniform0, nonUniform},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 0", 38, unbalancedNonUniform0, balancedUniform0},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 1", 41, unbalancedNonUniform0, balancedUniform1},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 2", 53, unbalancedNonUniform0, balancedUniform2},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 3", 101, unbalancedNonUniform0, balancedUniform3},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 0", 37, unbalancedNonUniform0, balancedNonUniform0},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 1", 38, unbalancedNonUniform0, balancedNonUniform1},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 2", 44, unbalancedNonUniform0, balancedNonUniform2},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 3", 74, unbalancedNonUniform0, balancedNonUniform3},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 0", 101, unbalancedNonUniform0, unbalancedUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 1", 101, unbalancedNonUniform0, unbalancedUniform1},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 2", 101, unbalancedNonUniform0, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 0", 74, unbalancedNonUniform0, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 1", 74, unbalancedNonUniform0, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 2", 74, unbalancedNonUniform0, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 1", "Empty", 37, unbalancedNonUniform1, empty},
                {"Unbalanced Non-Uniform 1", "Empty String", 38, unbalancedNonUniform1, emptyString},
                {"Unbalanced Non-Uniform 1", "Concrete", 38, unbalancedNonUniform1, concrete},
                {"Unbalanced Non-Uniform 1", "Uniform", 122, unbalancedNonUniform1, uniform},
                {"Unbalanced Non-Uniform 1", "Non-Uniform", 82, unbalancedNonUniform1, nonUniform},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 0", 38, unbalancedNonUniform1, balancedUniform0},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 1", 41, unbalancedNonUniform1, balancedUniform1},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 2", 53, unbalancedNonUniform1, balancedUniform2},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 3", 101, unbalancedNonUniform1, balancedUniform3},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 0", 37, unbalancedNonUniform1, balancedNonUniform0},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 1", 38, unbalancedNonUniform1, balancedNonUniform1},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 2", 44, unbalancedNonUniform1, balancedNonUniform2},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 3", 74, unbalancedNonUniform1, balancedNonUniform3},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 0", 101, unbalancedNonUniform1, unbalancedUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 1", 101, unbalancedNonUniform1, unbalancedUniform1},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 2", 101, unbalancedNonUniform1, unbalancedUniform2},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 0", 74, unbalancedNonUniform1, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 1", 74, unbalancedNonUniform1, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 2", 74, unbalancedNonUniform1, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 2", "Empty", 37, unbalancedNonUniform2, empty},
                {"Unbalanced Non-Uniform 2", "Empty String", 38, unbalancedNonUniform2, emptyString},
                {"Unbalanced Non-Uniform 2", "Concrete", 38, unbalancedNonUniform2, concrete},
                {"Unbalanced Non-Uniform 2", "Uniform", 122, unbalancedNonUniform2, uniform},
                {"Unbalanced Non-Uniform 2", "Non-Uniform", 82, unbalancedNonUniform2, nonUniform},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 0", 38, unbalancedNonUniform2, balancedUniform0},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 1", 41, unbalancedNonUniform2, balancedUniform1},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 2", 53, unbalancedNonUniform2, balancedUniform2},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 3", 101, unbalancedNonUniform2, balancedUniform3},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 0", 37, unbalancedNonUniform2, balancedNonUniform0},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 1", 38, unbalancedNonUniform2, balancedNonUniform1},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 2", 44, unbalancedNonUniform2, balancedNonUniform2},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 3", 74, unbalancedNonUniform2, balancedNonUniform3},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 0", 101, unbalancedNonUniform2, unbalancedUniform0},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 1", 101, unbalancedNonUniform2, unbalancedUniform1},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 2", 101, unbalancedNonUniform2, unbalancedUniform2},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 0", 74, unbalancedNonUniform2, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 1", 74, unbalancedNonUniform2, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 2", 74, unbalancedNonUniform2, unbalancedNonUniform2}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        Collection<WeightedAutomaton> automata = new ArrayList<>();
        automata.add(baseAutomaton);
        automata.add(argAutomaton);
        resultAutomaton = BasicWeightedOperations.union(automata);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton, 3)
                                           .intValue();

        // *** assert ***
        String reason = String.format("union({<%s Automaton>, <%s Automaton>})", baseDescription, argDescription);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
