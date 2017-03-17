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
public class Given_WeightedAutomaton_When_Concatenating {

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
    @Parameters(name = "{index}: <{0} Automaton>.concatenate(<{1} Automaton>) -> Expected MC = {2}")
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
        WeightedAutomaton unbalancedUniform0 = unbalanced_Uniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedUniform1 = unbalanced_Uniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedUniform2 = unbalanced_Uniform_WeightedAutomaton_2();
        WeightedAutomaton unbalancedNonUniform0 = unbalanced_NonUniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedNonUniform1 = unbalanced_NonUniform_WeightedAutomaton_1();

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", -1, empty, empty},
                {"Empty", "Empty String", -1, empty, emptyString},
                {"Empty", "Concrete", -1, empty, concrete},
                {"Empty", "Uniform", -1, empty, uniform},
                {"Empty", "Non-Uniform", -1, empty, nonUniform},
                {"Empty", "Unbalanced Uniform ", -1, empty, unbalancedUniform0},
                {"Empty", "Unbalanced Uniform ", -1, empty, unbalancedUniform1},
                {"Empty", "Unbalanced Uniform ", -1, empty, unbalancedUniform2},
                {"Empty", "Unbalanced Uniform ", -1, empty, unbalancedNonUniform0},
                {"Empty", "Unbalanced Uniform ", -1, empty, unbalancedNonUniform1},
                {"Empty String", "Empty", -1, emptyString, empty},
                {"Empty String", "Empty String", -1, emptyString, emptyString},
                {"Empty String", "Concrete", -1, emptyString, concrete},
                {"Empty String", "Uniform", -1, emptyString, uniform},
                {"Empty String", "Non-Uniform", -1, emptyString, nonUniform},
                {"Empty String", "Unbalanced Uniform ", -1, emptyString, unbalancedUniform0},
                {"Empty String", "Unbalanced Uniform ", -1, emptyString, unbalancedUniform1},
                {"Empty String", "Unbalanced Uniform ", -1, emptyString, unbalancedUniform2},
                {"Empty String", "Unbalanced Uniform ", -1, emptyString, unbalancedNonUniform0},
                {"Empty String", "Unbalanced Uniform ", -1, emptyString, unbalancedNonUniform1},
                {"Concrete", "Empty", -1, concrete, empty},
                {"Concrete", "Empty String", -1, concrete, emptyString},
                {"Concrete", "Concrete", -1, concrete, concrete},
                {"Concrete", "Uniform", -1, concrete, uniform},
                {"Concrete", "Non-Uniform", -1, concrete, nonUniform},
                {"Concrete", "Unbalanced Uniform ", -1, concrete, unbalancedUniform0},
                {"Concrete", "Unbalanced Uniform ", -1, concrete, unbalancedUniform1},
                {"Concrete", "Unbalanced Uniform ", -1, concrete, unbalancedUniform2},
                {"Concrete", "Unbalanced Uniform ", -1, concrete, unbalancedNonUniform0},
                {"Concrete", "Unbalanced Uniform ", -1, concrete, unbalancedNonUniform1},
                {"Uniform", "Empty", -1, uniform, empty},
                {"Uniform", "Empty String", -1, uniform, emptyString},
                {"Uniform", "Concrete", -1, uniform, concrete},
                {"Uniform", "Uniform", -1, uniform, uniform},
                {"Uniform", "Non-Uniform", -1, uniform, nonUniform},
                {"Uniform", "Unbalanced Uniform ", -1, uniform, unbalancedUniform0},
                {"Uniform", "Unbalanced Uniform ", -1, uniform, unbalancedUniform1},
                {"Uniform", "Unbalanced Uniform ", -1, uniform, unbalancedUniform2},
                {"Uniform", "Unbalanced Uniform ", -1, uniform, unbalancedNonUniform0},
                {"Uniform", "Unbalanced Uniform ", -1, uniform, unbalancedNonUniform1},
                {"Non-Uniform", "Empty", -1, nonUniform, empty},
                {"Non-Uniform", "Empty String", -1, nonUniform, emptyString},
                {"Non-Uniform", "Concrete", -1, nonUniform, concrete},
                {"Non-Uniform", "Uniform", -1, nonUniform, uniform},
                {"Non-Uniform", "Non-Uniform", -1, nonUniform, nonUniform},
                {"Non-Uniform", "Unbalanced Uniform ", -1, nonUniform, unbalancedUniform0},
                {"Non-Uniform", "Unbalanced Uniform ", -1, nonUniform, unbalancedUniform1},
                {"Non-Uniform", "Unbalanced Uniform ", -1, nonUniform, unbalancedUniform2},
                {"Non-Uniform", "Unbalanced Uniform ", -1, nonUniform, unbalancedNonUniform0},
                {"Non-Uniform", "Unbalanced Uniform ", -1, nonUniform, unbalancedNonUniform1},
                {"Unbalanced Uniform 0", "Empty", -1, unbalancedUniform0, empty},
                {"Unbalanced Uniform 0", "Empty String", -1, unbalancedUniform0, emptyString},
                {"Unbalanced Uniform 0", "Concrete", -1, unbalancedUniform0, concrete},
                {"Unbalanced Uniform 0", "Uniform", -1, unbalancedUniform0, uniform},
                {"Unbalanced Uniform 0", "Non-Uniform", -1, unbalancedUniform0, nonUniform},
                {"Unbalanced Uniform 0", "Unbalanced Uniform ", -1, unbalancedUniform0, unbalancedUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Uniform ", -1, unbalancedUniform0, unbalancedUniform1},
                {"Unbalanced Uniform 0", "Unbalanced Uniform ", -1, unbalancedUniform0, unbalancedUniform2},
                {"Unbalanced Uniform 0", "Unbalanced Uniform ", -1, unbalancedUniform0, unbalancedNonUniform0},
                {"Unbalanced Uniform 0", "Unbalanced Uniform ", -1, unbalancedUniform0, unbalancedNonUniform1},
                {"Unbalanced Uniform 1", "Empty", -1, unbalancedUniform1, empty},
                {"Unbalanced Uniform 1", "Empty String", -1, unbalancedUniform1, emptyString},
                {"Unbalanced Uniform 1", "Concrete", -1, unbalancedUniform1, concrete},
                {"Unbalanced Uniform 1", "Uniform", -1, unbalancedUniform1, uniform},
                {"Unbalanced Uniform 1", "Non-Uniform", -1, unbalancedUniform1, nonUniform},
                {"Unbalanced Uniform 1", "Unbalanced Uniform ", -1, unbalancedUniform1, unbalancedUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Uniform ", -1, unbalancedUniform1, unbalancedUniform1},
                {"Unbalanced Uniform 1", "Unbalanced Uniform ", -1, unbalancedUniform1, unbalancedUniform2},
                {"Unbalanced Uniform 1", "Unbalanced Uniform ", -1, unbalancedUniform1, unbalancedNonUniform0},
                {"Unbalanced Uniform 1", "Unbalanced Uniform ", -1, unbalancedUniform1, unbalancedNonUniform1},
                {"Unbalanced Uniform 2", "Empty", -1, unbalancedUniform2, empty},
                {"Unbalanced Uniform 2", "Empty String", -1, unbalancedUniform2, emptyString},
                {"Unbalanced Uniform 2", "Concrete", -1, unbalancedUniform2, concrete},
                {"Unbalanced Uniform 2", "Uniform", -1, unbalancedUniform2, uniform},
                {"Unbalanced Uniform 2", "Non-Uniform", -1, unbalancedUniform2, nonUniform},
                {"Unbalanced Uniform 2", "Unbalanced Uniform ", -1, unbalancedUniform2, unbalancedUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Uniform ", -1, unbalancedUniform2, unbalancedUniform1},
                {"Unbalanced Uniform 2", "Unbalanced Uniform ", -1, unbalancedUniform2, unbalancedUniform2},
                {"Unbalanced Uniform 2", "Unbalanced Uniform ", -1, unbalancedUniform2, unbalancedNonUniform0},
                {"Unbalanced Uniform 2", "Unbalanced Uniform ", -1, unbalancedUniform2, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 0", "Empty", -1, unbalancedNonUniform0, empty},
                {"Unbalanced Non-Uniform 0", "Empty String", -1, unbalancedNonUniform0, emptyString},
                {"Unbalanced Non-Uniform 0", "Concrete", -1, unbalancedNonUniform0, concrete},
                {"Unbalanced Non-Uniform 0", "Uniform", -1, unbalancedNonUniform0, uniform},
                {"Unbalanced Non-Uniform 0", "Non-Uniform", -1, unbalancedNonUniform0, nonUniform},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform ", -1, unbalancedNonUniform0, unbalancedUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform ", -1, unbalancedNonUniform0, unbalancedUniform1},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform ", -1, unbalancedNonUniform0, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform ", -1, unbalancedNonUniform0, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 0", "Unbalanced Uniform ", -1, unbalancedNonUniform0, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 1", "Empty", -1, unbalancedNonUniform1, empty},
                {"Unbalanced Non-Uniform 1", "Empty String", -1, unbalancedNonUniform1, emptyString},
                {"Unbalanced Non-Uniform 1", "Concrete", -1, unbalancedNonUniform1, concrete},
                {"Unbalanced Non-Uniform 1", "Uniform", -1, unbalancedNonUniform1, uniform},
                {"Unbalanced Non-Uniform 1", "Non-Uniform", -1, unbalancedNonUniform1, nonUniform},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform ", -1, unbalancedNonUniform1, unbalancedUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform ", -1, unbalancedNonUniform1, unbalancedUniform1},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform ", -1, unbalancedNonUniform1, unbalancedUniform2},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform ", -1, unbalancedNonUniform1, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", "Unbalanced Uniform ", -1, unbalancedNonUniform1, unbalancedNonUniform1}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomaton = baseAutomaton.concatenate(argAutomaton);
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
