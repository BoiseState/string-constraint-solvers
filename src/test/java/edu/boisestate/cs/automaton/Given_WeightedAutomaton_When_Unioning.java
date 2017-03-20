package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedAutomaton_When_Unioning {

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
    @Parameters(name = "{index}: <{0} Automaton>.intersect(<{1} Automaton>) ->" +
                       " Expected MC = {2}")
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

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", -1, empty, empty},
                {"Empty", "Empty String", -1, empty, emptyString},
                {"Empty", "Concrete", -1, empty, concrete},
                {"Empty", "Uniform", -1, empty, uniform},
                {"Empty", "Non-Uniform", -1, empty, nonUniform},
                {"Empty", "Balanced Uniform 0", -1, empty, balancedUniform0},
                {"Empty", "Balanced Uniform 1", -1, empty, balancedUniform1},
                {"Empty", "Balanced Uniform 2", -1, empty, balancedUniform2},
                {"Empty", "Balanced Uniform 3", -1, empty, balancedUniform3},
                {"Empty", "Balanced Non-Uniform 0", -1, empty, balancedNonUniform0},
                {"Empty", "Balanced Non-Uniform 1", -1, empty, balancedNonUniform1},
                {"Empty", "Balanced Non-Uniform 2", -1, empty, balancedNonUniform2},
                {"Empty", "Balanced Non-Uniform 3", -1, empty, balancedNonUniform3},
                {"Empty", "Unbalanced Uniform 0", -1, empty, unbalancedUniform0},
                {"Empty", "Unbalanced Uniform 1", -1, empty, unbalancedUniform1},
                {"Empty", "Unbalanced Uniform 2", -1, empty, unbalancedUniform2},
                {"Empty", "Unbalanced Non-Uniform 0", -1, empty, unbalancedNonUniform0},
                {"Empty", "Unbalanced Non-Uniform 1", -1, empty, unbalancedNonUniform1},
                {"Empty", "Unbalanced Non-Uniform 2", -1, empty, unbalancedNonUniform2},
                {"Empty String", "Empty", -1, emptyString, empty},
                {"Empty String", "Empty String", -1, emptyString, emptyString},
                {"Empty String", "Concrete", -1, emptyString, concrete},
                {"Empty String", "Uniform", -1, emptyString, uniform},
                {"Empty String", "Non-Uniform", -1, emptyString, nonUniform},
                {"Empty String", "Balanced Uniform 0", -1, emptyString, balancedUniform0},
                {"Empty String", "Balanced Uniform 1", -1, emptyString, balancedUniform1},
                {"Empty String", "Balanced Uniform 2", -1, emptyString, balancedUniform2},
                {"Empty String", "Balanced Uniform 3", -1, emptyString, balancedUniform3},
                {"Empty String", "Balanced Non-Uniform 0", -1, emptyString, balancedNonUniform0},
                {"Empty String", "Balanced Non-Uniform 1", -1, emptyString, balancedNonUniform1},
                {"Empty String", "Balanced Non-Uniform 2", -1, emptyString, balancedNonUniform2},
                {"Empty String", "Balanced Non-Uniform 3", -1, emptyString, balancedNonUniform3},
                {"Empty String", "Unbalanced Uniform 0", -1, emptyString, unbalancedUniform0},
                {"Empty String", "Unbalanced Uniform 1", -1, emptyString, unbalancedUniform1},
                {"Empty String", "Unbalanced Uniform 2", -1, emptyString, unbalancedUniform2},
                {"Empty String", "Unbalanced Non-Uniform 0", -1, emptyString, unbalancedNonUniform0},
                {"Empty String", "Unbalanced Non-Uniform 1", -1, emptyString, unbalancedNonUniform1},
                {"Empty String", "Unbalanced Non-Uniform 2", -1, emptyString, unbalancedNonUniform2},
                {"Concrete", "Empty", -1, concrete, empty},
                {"Concrete", "Empty String", -1, concrete, emptyString},
                {"Concrete", "Concrete", -1, concrete, concrete},
                {"Concrete", "Uniform", -1, concrete, uniform},
                {"Concrete", "Non-Uniform", -1, concrete, nonUniform},
                {"Concrete", "Balanced Uniform 0", -1, concrete, balancedUniform0},
                {"Concrete", "Balanced Uniform 1", -1, concrete, balancedUniform1},
                {"Concrete", "Balanced Uniform 2", -1, concrete, balancedUniform2},
                {"Concrete", "Balanced Uniform 3", -1, concrete, balancedUniform3},
                {"Concrete", "Balanced Non-Uniform 0", -1, concrete, balancedNonUniform0},
                {"Concrete", "Balanced Non-Uniform 1", -1, concrete, balancedNonUniform1},
                {"Concrete", "Balanced Non-Uniform 2", -1, concrete, balancedNonUniform2},
                {"Concrete", "Balanced Non-Uniform 3", -1, concrete, balancedNonUniform3},
                {"Concrete", "Unbalanced Uniform 0", -1, concrete, unbalancedUniform0},
                {"Concrete", "Unbalanced Uniform 1", -1, concrete, unbalancedUniform1},
                {"Concrete", "Unbalanced Uniform 2", -1, concrete, unbalancedUniform2},
                {"Concrete", "Unbalanced Non-Uniform 0", -1, concrete, unbalancedNonUniform0},
                {"Concrete", "Unbalanced Non-Uniform 1", -1, concrete, unbalancedNonUniform1},
                {"Concrete", "Unbalanced Non-Uniform 2", -1, concrete, unbalancedNonUniform2},
                {"Uniform", "Empty", -1, uniform, empty},
                {"Uniform", "Empty String", -1, uniform, emptyString},
                {"Uniform", "Concrete", -1, uniform, concrete},
                {"Uniform", "Uniform", -1, uniform, uniform},
                {"Uniform", "Non-Uniform", -1, uniform, nonUniform},
                {"Uniform", "Balanced Uniform 0", -1, uniform, balancedUniform0},
                {"Uniform", "Balanced Uniform 1", -1, uniform, balancedUniform1},
                {"Uniform", "Balanced Uniform 2", -1, uniform, balancedUniform2},
                {"Uniform", "Balanced Uniform 3", -1, uniform, balancedUniform3},
                {"Uniform", "Balanced Non-Uniform 0", -1, uniform, balancedNonUniform0},
                {"Uniform", "Balanced Non-Uniform 1", -1, uniform, balancedNonUniform1},
                {"Uniform", "Balanced Non-Uniform 2", -1, uniform, balancedNonUniform2},
                {"Uniform", "Balanced Non-Uniform 3", -1, uniform, balancedNonUniform3},
                {"Uniform", "Unbalanced Uniform 0", -1, uniform, unbalancedUniform0},
                {"Uniform", "Unbalanced Uniform 1", -1, uniform, unbalancedUniform1},
                {"Uniform", "Unbalanced Uniform 2", -1, uniform, unbalancedUniform2},
                {"Uniform", "Unbalanced Non-Uniform 0", -1, uniform, unbalancedNonUniform0},
                {"Uniform", "Unbalanced Non-Uniform 1", -1, uniform, unbalancedNonUniform1},
                {"Uniform", "Unbalanced Non-Uniform 2", -1, uniform, unbalancedNonUniform2},
                {"Non-Uniform", "Empty", -1, nonUniform, empty},
                {"Non-Uniform", "Empty String", -1, nonUniform, emptyString},
                {"Non-Uniform", "Concrete", -1, nonUniform, concrete},
                {"Non-Uniform", "Uniform", -1, nonUniform, uniform},
                {"Non-Uniform", "Non-Uniform", -1, nonUniform, nonUniform},
                {"Non-Uniform", "Balanced Uniform 0", -1, nonUniform, balancedUniform0},
                {"Non-Uniform", "Balanced Uniform 1", -1, nonUniform, balancedUniform1},
                {"Non-Uniform", "Balanced Uniform 2", -1, nonUniform, balancedUniform2},
                {"Non-Uniform", "Balanced Uniform 3", -1, nonUniform, balancedUniform3},
                {"Non-Uniform", "Balanced Non-Uniform 0", -1, nonUniform, balancedNonUniform0},
                {"Non-Uniform", "Balanced Non-Uniform 1", -1, nonUniform, balancedNonUniform1},
                {"Non-Uniform", "Balanced Non-Uniform 2", -1, nonUniform, balancedNonUniform2},
                {"Non-Uniform", "Balanced Non-Uniform 3", -1, nonUniform, balancedNonUniform3},
                {"Non-Uniform", "Unbalanced Uniform 0", -1, nonUniform, unbalancedUniform0},
                {"Non-Uniform", "Unbalanced Uniform 1", -1, nonUniform, unbalancedUniform1},
                {"Non-Uniform", "Unbalanced Uniform 2", -1, nonUniform, unbalancedUniform2},
                {"Non-Uniform", "Unbalanced Non-Uniform 0", -1, nonUniform, unbalancedNonUniform0},
                {"Non-Uniform", "Unbalanced Non-Uniform 1", -1, nonUniform, unbalancedNonUniform1},
                {"Non-Uniform", "Unbalanced Non-Uniform 2", -1, nonUniform, unbalancedNonUniform2},
                {"Balanced Uniform 0", "Empty", -1, balancedUniform0, empty},
                {"Balanced Uniform 0", "Empty String", -1, balancedUniform0, emptyString},
                {"Balanced Uniform 0", "Concrete", -1, balancedUniform0, concrete},
                {"Balanced Uniform 0", "Uniform", -1, balancedUniform0, uniform},
                {"Balanced Uniform 0", "Non-Uniform", -1, balancedUniform0, nonUniform},
                {"Balanced Uniform 0", "Balanced Uniform 0", -1, balancedUniform0, balancedUniform0},
                {"Balanced Uniform 0", "Balanced Uniform 1", -1, balancedUniform0, balancedUniform1},
                {"Balanced Uniform 0", "Balanced Uniform 2", -1, balancedUniform0, balancedUniform2},
                {"Balanced Uniform 0", "Balanced Uniform 3", -1, balancedUniform0, balancedUniform3},
                {"Balanced Uniform 0", "Balanced Non-Uniform 0", -1, balancedUniform0, balancedNonUniform0},
                {"Balanced Uniform 0", "Balanced Non-Uniform 1", -1, balancedUniform0, balancedNonUniform1},
                {"Balanced Uniform 0", "Balanced Non-Uniform 2", -1, balancedUniform0, balancedNonUniform2},
                {"Balanced Uniform 0", "Balanced Non-Uniform 3", -1, balancedUniform0, balancedNonUniform3},
                {"Balanced Uniform 0", "Unbalanced Uniform 0", -1, balancedUniform0, unbalancedUniform0},
                {"Balanced Uniform 0", "Unbalanced Uniform 1", -1, balancedUniform0, unbalancedUniform1},
                {"Balanced Uniform 0", "Unbalanced Uniform 2", -1, balancedUniform0, unbalancedUniform2},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 0", -1, balancedUniform0, unbalancedNonUniform0},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 1", -1, balancedUniform0, unbalancedNonUniform1},
                {"Balanced Uniform 0", "Unbalanced Non-Uniform 2", -1, balancedUniform0, unbalancedNonUniform2},
                {"Balanced Uniform 1", "Empty", -1, balancedUniform1, empty},
                {"Balanced Uniform 1", "Empty String", -1, balancedUniform1, emptyString},
                {"Balanced Uniform 1", "Concrete", -1, balancedUniform1, concrete},
                {"Balanced Uniform 1", "Uniform", -1, balancedUniform1, uniform},
                {"Balanced Uniform 1", "Non-Uniform", -1, balancedUniform1, nonUniform},
                {"Balanced Uniform 1", "Balanced Uniform 0", -1, balancedUniform1, balancedUniform0},
                {"Balanced Uniform 1", "Balanced Uniform 1", -1, balancedUniform1, balancedUniform1},
                {"Balanced Uniform 1", "Balanced Uniform 2", -1, balancedUniform1, balancedUniform2},
                {"Balanced Uniform 1", "Balanced Uniform 3", -1, balancedUniform1, balancedUniform3},
                {"Balanced Uniform 1", "Balanced Non-Uniform 0", -1, balancedUniform1, balancedNonUniform0},
                {"Balanced Uniform 1", "Balanced Non-Uniform 1", -1, balancedUniform1, balancedNonUniform1},
                {"Balanced Uniform 1", "Balanced Non-Uniform 2", -1, balancedUniform1, balancedNonUniform2},
                {"Balanced Uniform 1", "Balanced Non-Uniform 3", -1, balancedUniform1, balancedNonUniform3},
                {"Balanced Uniform 1", "Unbalanced Uniform 0", -1, balancedUniform1, unbalancedUniform0},
                {"Balanced Uniform 1", "Unbalanced Uniform 1", -1, balancedUniform1, unbalancedUniform1},
                {"Balanced Uniform 1", "Unbalanced Uniform 2", -1, balancedUniform1, unbalancedUniform2},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 0", -1, balancedUniform1, unbalancedNonUniform0},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 1", -1, balancedUniform1, unbalancedNonUniform1},
                {"Balanced Uniform 1", "Unbalanced Non-Uniform 2", -1, balancedUniform1, unbalancedNonUniform2},
                {"Balanced Uniform 2", "Empty", -1, balancedUniform2, empty},
                {"Balanced Uniform 2", "Empty String", -1, balancedUniform2, emptyString},
                {"Balanced Uniform 2", "Concrete", -1, balancedUniform2, concrete},
                {"Balanced Uniform 2", "Uniform", -1, balancedUniform2, uniform},
                {"Balanced Uniform 2", "Non-Uniform", -1, balancedUniform2, nonUniform},
                {"Balanced Uniform 2", "Balanced Uniform 0", -1, balancedUniform2, balancedUniform0},
                {"Balanced Uniform 2", "Balanced Uniform 1", -1, balancedUniform2, balancedUniform1},
                {"Balanced Uniform 2", "Balanced Uniform 2", -1, balancedUniform2, balancedUniform2},
                {"Balanced Uniform 2", "Balanced Uniform 3", -1, balancedUniform2, balancedUniform3},
                {"Balanced Uniform 2", "Balanced Non-Uniform 0", -1, balancedUniform2, balancedNonUniform0},
                {"Balanced Uniform 2", "Balanced Non-Uniform 1", -1, balancedUniform2, balancedNonUniform1},
                {"Balanced Uniform 2", "Balanced Non-Uniform 2", -1, balancedUniform2, balancedNonUniform2},
                {"Balanced Uniform 2", "Balanced Non-Uniform 3", -1, balancedUniform2, balancedNonUniform3},
                {"Balanced Uniform 2", "Unbalanced Uniform 0", -1, balancedUniform2, unbalancedUniform0},
                {"Balanced Uniform 2", "Unbalanced Uniform 1", -1, balancedUniform2, unbalancedUniform1},
                {"Balanced Uniform 2", "Unbalanced Uniform 2", -1, balancedUniform2, unbalancedUniform2},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 0", -1, balancedUniform2, unbalancedNonUniform0},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 1", -1, balancedUniform2, unbalancedNonUniform1},
                {"Balanced Uniform 2", "Unbalanced Non-Uniform 2", -1, balancedUniform2, unbalancedNonUniform2},
                {"Balanced Uniform 3", "Empty", -1, balancedUniform3, empty},
                {"Balanced Uniform 3", "Empty String", -1, balancedUniform3, emptyString},
                {"Balanced Uniform 3", "Concrete", -1, balancedUniform3, concrete},
                {"Balanced Uniform 3", "Uniform", -1, balancedUniform3, uniform},
                {"Balanced Uniform 3", "Non-Uniform", -1, balancedUniform3, nonUniform},
                {"Balanced Uniform 3", "Balanced Uniform 0", -1, balancedUniform3, balancedUniform0},
                {"Balanced Uniform 3", "Balanced Uniform 1", -1, balancedUniform3, balancedUniform1},
                {"Balanced Uniform 3", "Balanced Uniform 2", -1, balancedUniform3, balancedUniform2},
                {"Balanced Uniform 3", "Balanced Uniform 3", -1, balancedUniform3, balancedUniform3},
                {"Balanced Uniform 3", "Balanced Non-Uniform 0", -1, balancedUniform3, balancedNonUniform0},
                {"Balanced Uniform 3", "Balanced Non-Uniform 1", -1, balancedUniform3, balancedNonUniform1},
                {"Balanced Uniform 3", "Balanced Non-Uniform 2", -1, balancedUniform3, balancedNonUniform2},
                {"Balanced Uniform 3", "Balanced Non-Uniform 3", -1, balancedUniform3, balancedNonUniform3},
                {"Balanced Uniform 3", "Unbalanced Uniform 0", -1, balancedUniform3, unbalancedUniform0},
                {"Balanced Uniform 3", "Unbalanced Uniform 1", -1, balancedUniform3, unbalancedUniform1},
                {"Balanced Uniform 3", "Unbalanced Uniform 2", -1, balancedUniform3, unbalancedUniform2},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 0", -1, balancedUniform3, unbalancedNonUniform0},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 1", -1, balancedUniform3, unbalancedNonUniform1},
                {"Balanced Uniform 3", "Unbalanced Non-Uniform 2", -1, balancedUniform3, unbalancedNonUniform2},
                {"Balanced Non-Uniform 0", "Empty", -1, balancedNonUniform0, empty},
                {"Balanced Non-Uniform 0", "Empty String", -1, balancedNonUniform0, emptyString},
                {"Balanced Non-Uniform 0", "Concrete", -1, balancedNonUniform0, concrete},
                {"Balanced Non-Uniform 0", "Uniform", -1, balancedNonUniform0, uniform},
                {"Balanced Non-Uniform 0", "Non-Uniform", -1, balancedNonUniform0, nonUniform},
                {"Balanced Non-Uniform 0", "Balanced Uniform 0", -1, balancedNonUniform0, balancedUniform0},
                {"Balanced Non-Uniform 0", "Balanced Uniform 1", -1, balancedNonUniform0, balancedUniform1},
                {"Balanced Non-Uniform 0", "Balanced Uniform 2", -1, balancedNonUniform0, balancedUniform2},
                {"Balanced Non-Uniform 0", "Balanced Uniform 3", -1, balancedNonUniform0, balancedUniform3},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 0", -1, balancedNonUniform0, balancedNonUniform0},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 1", -1, balancedNonUniform0, balancedNonUniform1},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 2", -1, balancedNonUniform0, balancedNonUniform2},
                {"Balanced Non-Uniform 0", "Balanced Non-Uniform 3", -1, balancedNonUniform0, balancedNonUniform3},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 0", -1, balancedNonUniform0, unbalancedUniform0},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 1", -1, balancedNonUniform0, unbalancedUniform1},
                {"Balanced Non-Uniform 0", "Unbalanced Uniform 2", -1, balancedNonUniform0, unbalancedUniform2},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 0", -1, balancedNonUniform0, unbalancedNonUniform0},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 1", -1, balancedNonUniform0, unbalancedNonUniform1},
                {"Balanced Non-Uniform 0", "Unbalanced Non-Uniform 2", -1, balancedNonUniform0, unbalancedNonUniform2},
                {"Balanced Non-Uniform 1", "Empty", -1, balancedNonUniform1, empty},
                {"Balanced Non-Uniform 1", "Empty String", -1, balancedNonUniform1, emptyString},
                {"Balanced Non-Uniform 1", "Concrete", -1, balancedNonUniform1, concrete},
                {"Balanced Non-Uniform 1", "Uniform", -1, balancedNonUniform1, uniform},
                {"Balanced Non-Uniform 1", "Non-Uniform", -1, balancedNonUniform1, nonUniform},
                {"Balanced Non-Uniform 1", "Balanced Uniform 0", -1, balancedNonUniform1, balancedUniform0},
                {"Balanced Non-Uniform 1", "Balanced Uniform 1", -1, balancedNonUniform1, balancedUniform1},
                {"Balanced Non-Uniform 1", "Balanced Uniform 2", -1, balancedNonUniform1, balancedUniform2},
                {"Balanced Non-Uniform 1", "Balanced Uniform 3", -1, balancedNonUniform1, balancedUniform3},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 0", -1, balancedNonUniform1, balancedNonUniform0},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 1", -1, balancedNonUniform1, balancedNonUniform1},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 2", -1, balancedNonUniform1, balancedNonUniform2},
                {"Balanced Non-Uniform 1", "Balanced Non-Uniform 3", -1, balancedNonUniform1, balancedNonUniform3},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 0", -1, balancedNonUniform1, unbalancedUniform0},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 1", -1, balancedNonUniform1, unbalancedUniform1},
                {"Balanced Non-Uniform 1", "Unbalanced Uniform 2", -1, balancedNonUniform1, unbalancedUniform2},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 0", -1, balancedNonUniform1, unbalancedNonUniform0},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 1", -1, balancedNonUniform1, unbalancedNonUniform1},
                {"Balanced Non-Uniform 1", "Unbalanced Non-Uniform 2", -1, balancedNonUniform1, unbalancedNonUniform2},
                {"Balanced Non-Uniform 2", "Empty", -1, balancedNonUniform2, empty},
                {"Balanced Non-Uniform 2", "Empty String", -1, balancedNonUniform2, emptyString},
                {"Balanced Non-Uniform 2", "Concrete", -1, balancedNonUniform2, concrete},
                {"Balanced Non-Uniform 2", "Uniform", -1, balancedNonUniform2, uniform},
                {"Balanced Non-Uniform 2", "Non-Uniform", -1, balancedNonUniform2, nonUniform},
                {"Balanced Non-Uniform 2", "Balanced Uniform 0", -1, balancedNonUniform2, balancedUniform0},
                {"Balanced Non-Uniform 2", "Balanced Uniform 1", -1, balancedNonUniform2, balancedUniform1},
                {"Balanced Non-Uniform 2", "Balanced Uniform 2", -1, balancedNonUniform2, balancedUniform2},
                {"Balanced Non-Uniform 2", "Balanced Uniform 3", -1, balancedNonUniform2, balancedUniform3},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 0", -1, balancedNonUniform2, balancedNonUniform0},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 1", -1, balancedNonUniform2, balancedNonUniform1},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 2", -1, balancedNonUniform2, balancedNonUniform2},
                {"Balanced Non-Uniform 2", "Balanced Non-Uniform 3", -1, balancedNonUniform2, balancedNonUniform3},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 0", -1, balancedNonUniform2, unbalancedUniform0},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 1", -1, balancedNonUniform2, unbalancedUniform1},
                {"Balanced Non-Uniform 2", "Unbalanced Uniform 2", -1, balancedNonUniform2, unbalancedUniform2},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 0", -1, balancedNonUniform2, unbalancedNonUniform0},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 1", -1, balancedNonUniform2, unbalancedNonUniform1},
                {"Balanced Non-Uniform 2", "Unbalanced Non-Uniform 2", -1, balancedNonUniform2, unbalancedNonUniform2},
                {"Balanced Non-Uniform 3", "Empty", -1, balancedNonUniform3, empty},
                {"Balanced Non-Uniform 3", "Empty String", -1, balancedNonUniform3, emptyString},
                {"Balanced Non-Uniform 3", "Concrete", -1, balancedNonUniform3, concrete},
                {"Balanced Non-Uniform 3", "Uniform", -1, balancedNonUniform3, uniform},
                {"Balanced Non-Uniform 3", "Non-Uniform", -1, balancedNonUniform3, nonUniform},
                {"Balanced Non-Uniform 3", "Balanced Uniform 0", -1, balancedNonUniform3, balancedUniform0},
                {"Balanced Non-Uniform 3", "Balanced Uniform 1", -1, balancedNonUniform3, balancedUniform1},
                {"Balanced Non-Uniform 3", "Balanced Uniform 2", -1, balancedNonUniform3, balancedUniform2},
                {"Balanced Non-Uniform 3", "Balanced Uniform 3", -1, balancedNonUniform3, balancedUniform3},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 0", -1, balancedNonUniform3, balancedNonUniform0},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 1", -1, balancedNonUniform3, balancedNonUniform1},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 2", -1, balancedNonUniform3, balancedNonUniform2},
                {"Balanced Non-Uniform 3", "Balanced Non-Uniform 3", -1, balancedNonUniform3, balancedNonUniform3},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 0", -1, balancedNonUniform3, unbalancedUniform0},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 1", -1, balancedNonUniform3, unbalancedUniform1},
                {"Balanced Non-Uniform 3", "Unbalanced Uniform 2", -1, balancedNonUniform3, unbalancedUniform2},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 0", -1, balancedNonUniform3, unbalancedNonUniform0},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 1", -1, balancedNonUniform3, unbalancedNonUniform1},
                {"Balanced Non-Uniform 3", "Unbalanced Non-Uniform 2", -1, balancedNonUniform3, unbalancedNonUniform2},
                {"Unbalanced Uniform 0", "Empty", -1, unbalancedUniform0, empty},
                {"Unbalanced Uniform 0", "Empty String", -1, unbalancedUniform0, emptyString},
                {"Unbalanced Uniform 0", "Concrete", -1, unbalancedUniform0, concrete},
                {"Unbalanced Uniform 0", "Uniform", -1, unbalancedUniform0, uniform},
                {"Unbalanced Uniform 0", "Non-Uniform", -1, unbalancedUniform0, nonUniform},
                {"Unbalanced Uniform 0", "Balanced Uniform 0", -1, unbalancedUniform0, balancedUniform0},
                {"Unbalanced Uniform 0", "Balanced Uniform 1", -1, unbalancedUniform0, balancedUniform1},
                {"Unbalanced Uniform 0", "Balanced Uniform 2", -1, unbalancedUniform0, balancedUniform2},
                {"Unbalanced Uniform 0", "Balanced Uniform 3", -1, unbalancedUniform0, balancedUniform3},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 0", -1, unbalancedUniform0, balancedNonUniform0},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 1", -1, unbalancedUniform0, balancedNonUniform1},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 2", -1, unbalancedUniform0, balancedNonUniform2},
                {"Unbalanced Uniform 0", "Balanced Non-Uniform 3", -1, unbalancedUniform0, balancedNonUniform3},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 0", -1, unbalancedUniform0, unbalancedUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 1", -1, unbalancedUniform0, unbalancedUniform1},
                {"Unbalanced Uniform 0", "Unbalanced Uniform 2", -1, unbalancedUniform0, unbalancedUniform2},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 0", -1, unbalancedUniform0, unbalancedNonUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 1", -1, unbalancedUniform0, unbalancedNonUniform1},
                {"Unbalanced Uniform 0", "Unbalanced Non-Uniform 2", -1, unbalancedUniform0, unbalancedNonUniform2},
                {"Unbalanced Uniform 1", "Empty", -1, unbalancedUniform1, empty},
                {"Unbalanced Uniform 1", "Empty String", -1, unbalancedUniform1, emptyString},
                {"Unbalanced Uniform 1", "Concrete", -1, unbalancedUniform1, concrete},
                {"Unbalanced Uniform 1", "Uniform", -1, unbalancedUniform1, uniform},
                {"Unbalanced Uniform 1", "Non-Uniform", -1, unbalancedUniform1, nonUniform},
                {"Unbalanced Uniform 1", "Balanced Uniform 0", -1, unbalancedUniform1, balancedUniform0},
                {"Unbalanced Uniform 1", "Balanced Uniform 1", -1, unbalancedUniform1, balancedUniform1},
                {"Unbalanced Uniform 1", "Balanced Uniform 2", -1, unbalancedUniform1, balancedUniform2},
                {"Unbalanced Uniform 1", "Balanced Uniform 3", -1, unbalancedUniform1, balancedUniform3},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 0", -1, unbalancedUniform1, balancedNonUniform0},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 1", -1, unbalancedUniform1, balancedNonUniform1},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 2", -1, unbalancedUniform1, balancedNonUniform2},
                {"Unbalanced Uniform 1", "Balanced Non-Uniform 3", -1, unbalancedUniform1, balancedNonUniform3},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 0", -1, unbalancedUniform1, unbalancedUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 1", -1, unbalancedUniform1, unbalancedUniform1},
                {"Unbalanced Uniform 1", "Unbalanced Uniform 2", -1, unbalancedUniform1, unbalancedUniform2},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 0", -1, unbalancedUniform1, unbalancedNonUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 1", -1, unbalancedUniform1, unbalancedNonUniform1},
                {"Unbalanced Uniform 1", "Unbalanced Non-Uniform 2", -1, unbalancedUniform1, unbalancedNonUniform2},
                {"Unbalanced Uniform 2", "Empty", -1, unbalancedUniform2, empty},
                {"Unbalanced Uniform 2", "Empty String", -1, unbalancedUniform2, emptyString},
                {"Unbalanced Uniform 2", "Concrete", -1, unbalancedUniform2, concrete},
                {"Unbalanced Uniform 2", "Uniform", -1, unbalancedUniform2, uniform},
                {"Unbalanced Uniform 2", "Non-Uniform", -1, unbalancedUniform2, nonUniform},
                {"Unbalanced Uniform 2", "Balanced Uniform 0", -1, unbalancedUniform2, balancedUniform0},
                {"Unbalanced Uniform 2", "Balanced Uniform 1", -1, unbalancedUniform2, balancedUniform1},
                {"Unbalanced Uniform 2", "Balanced Uniform 2", -1, unbalancedUniform2, balancedUniform2},
                {"Unbalanced Uniform 2", "Balanced Uniform 3", -1, unbalancedUniform2, balancedUniform3},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 0", -1, unbalancedUniform2, balancedNonUniform0},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 1", -1, unbalancedUniform2, balancedNonUniform1},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 2", -1, unbalancedUniform2, balancedNonUniform2},
                {"Unbalanced Uniform 2", "Balanced Non-Uniform 3", -1, unbalancedUniform2, balancedNonUniform3},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 0", -1, unbalancedUniform2, unbalancedUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 1", -1, unbalancedUniform2, unbalancedUniform1},
                {"Unbalanced Uniform 2", "Unbalanced Uniform 2", -1, unbalancedUniform2, unbalancedUniform2},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 0", -1, unbalancedUniform2, unbalancedNonUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 1", -1, unbalancedUniform2, unbalancedNonUniform1},
                {"Unbalanced Uniform 2", "Unbalanced Non-Uniform 2", -1, unbalancedUniform2, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 0", "Empty", -1, unbalancedNonUniform0, empty},
                {"Unbalanced Non-Uniform 0", "Empty String", -1, unbalancedNonUniform0, emptyString},
                {"Unbalanced Non-Uniform 0", "Concrete", -1, unbalancedNonUniform0, concrete},
                {"Unbalanced Non-Uniform 0", "Uniform", -1, unbalancedNonUniform0, uniform},
                {"Unbalanced Non-Uniform 0", "Non-Uniform", -1, unbalancedNonUniform0, nonUniform},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 0", -1, unbalancedNonUniform0, balancedUniform0},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 1", -1, unbalancedNonUniform0, balancedUniform1},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 2", -1, unbalancedNonUniform0, balancedUniform2},
                {"Unbalanced Non-Uniform 0", "Balanced Uniform 3", -1, unbalancedNonUniform0, balancedUniform3},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 0", -1, unbalancedNonUniform0, balancedNonUniform0},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 1", -1, unbalancedNonUniform0, balancedNonUniform1},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 2", -1, unbalancedNonUniform0, balancedNonUniform2},
                {"Unbalanced Non-Uniform 0", "Balanced Non-Uniform 3", -1, unbalancedNonUniform0, balancedNonUniform3},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 0", -1, unbalancedNonUniform0, unbalancedUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 1", -1, unbalancedNonUniform0, unbalancedUniform1},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform 2", -1, unbalancedNonUniform0, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 0", -1, unbalancedNonUniform0, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 1", -1, unbalancedNonUniform0, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 0", "Unbalanced Non-Uniform 2", -1, unbalancedNonUniform0, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 1", "Empty", -1, unbalancedNonUniform1, empty},
                {"Unbalanced Non-Uniform 1", "Empty String", -1, unbalancedNonUniform1, emptyString},
                {"Unbalanced Non-Uniform 1", "Concrete", -1, unbalancedNonUniform1, concrete},
                {"Unbalanced Non-Uniform 1", "Uniform", -1, unbalancedNonUniform1, uniform},
                {"Unbalanced Non-Uniform 1", "Non-Uniform", -1, unbalancedNonUniform1, nonUniform},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 0", -1, unbalancedNonUniform1, balancedUniform0},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 1", -1, unbalancedNonUniform1, balancedUniform1},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 2", -1, unbalancedNonUniform1, balancedUniform2},
                {"Unbalanced Non-Uniform 1", "Balanced Uniform 3", -1, unbalancedNonUniform1, balancedUniform3},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 0", -1, unbalancedNonUniform1, balancedNonUniform0},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 1", -1, unbalancedNonUniform1, balancedNonUniform1},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 2", -1, unbalancedNonUniform1, balancedNonUniform2},
                {"Unbalanced Non-Uniform 1", "Balanced Non-Uniform 3", -1, unbalancedNonUniform1, balancedNonUniform3},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 0", -1, unbalancedNonUniform1, unbalancedUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 1", -1, unbalancedNonUniform1, unbalancedUniform1},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform 2", -1, unbalancedNonUniform1, unbalancedUniform2},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 0", -1, unbalancedNonUniform1, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 1", -1, unbalancedNonUniform1, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 1", "Unbalanced Non-Uniform 2", -1, unbalancedNonUniform1, unbalancedNonUniform2},
                {"Unbalanced Non-Uniform 2", "Empty", -1, unbalancedNonUniform2, empty},
                {"Unbalanced Non-Uniform 2", "Empty String", -1, unbalancedNonUniform2, emptyString},
                {"Unbalanced Non-Uniform 2", "Concrete", -1, unbalancedNonUniform2, concrete},
                {"Unbalanced Non-Uniform 2", "Uniform", -1, unbalancedNonUniform2, uniform},
                {"Unbalanced Non-Uniform 2", "Non-Uniform", -1, unbalancedNonUniform2, nonUniform},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 0", -1, unbalancedNonUniform2, balancedUniform0},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 1", -1, unbalancedNonUniform2, balancedUniform1},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 2", -1, unbalancedNonUniform2, balancedUniform2},
                {"Unbalanced Non-Uniform 2", "Balanced Uniform 3", -1, unbalancedNonUniform2, balancedUniform3},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 0", -1, unbalancedNonUniform2, balancedNonUniform0},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 1", -1, unbalancedNonUniform2, balancedNonUniform1},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 2", -1, unbalancedNonUniform2, balancedNonUniform2},
                {"Unbalanced Non-Uniform 2", "Balanced Non-Uniform 3", -1, unbalancedNonUniform2, balancedNonUniform3},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 0", -1, unbalancedNonUniform2, unbalancedUniform0},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 1", -1, unbalancedNonUniform2, unbalancedUniform1},
                {"Unbalanced Non-Uniform 2", "Unbalanced Uniform 2", -1, unbalancedNonUniform2, unbalancedUniform2},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 0", -1, unbalancedNonUniform2, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 1", -1, unbalancedNonUniform2, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2", "Unbalanced Non-Uniform 2", -1, unbalancedNonUniform2, unbalancedNonUniform2}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomaton = baseAutomaton.intersection(argAutomaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton, 3)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
