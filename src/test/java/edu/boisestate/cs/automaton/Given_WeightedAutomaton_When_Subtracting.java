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
public class Given_WeightedAutomaton_When_Subtracting {

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
    @Parameters(name = "{index}: <{0} Automaton>.minus(<{1} Automaton>) ->" +
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
        WeightedAutomaton unbalancedUniform0 = unbalanced_Uniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedUniform1 = unbalanced_Uniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedUniform2 = unbalanced_Uniform_WeightedAutomaton_2();
        WeightedAutomaton unbalancedNonUniform0 = unbalanced_NonUniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedNonUniform1 = unbalanced_NonUniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedNonUniform2 = unbalanced_NonUniform_WeightedAutomaton_2();

        return Arrays.asList(new Object[][]{
                {"Empty", "Empty", 0, empty, empty},
                {"Empty", "Empty String", 0, empty, emptyString},
                {"Empty", "Concrete", 0, empty, concrete},
                {"Empty", "Uniform", 0, empty, uniform},
                {"Empty", "Non-Uniform", 0, empty, nonUniform},
                {"Empty String", "Empty", 1, emptyString, empty},
                {"Empty String", "Empty String", 0, emptyString, emptyString},
                {"Empty String", "Concrete", 1, emptyString, concrete},
                {"Empty String", "Uniform", 0, emptyString, uniform},
                {"Empty String", "Non-Uniform", 1, emptyString, nonUniform},
                {"Concrete", "Empty", 1, concrete, empty},
                {"Concrete", "Empty String", 1, concrete, emptyString},
                {"Concrete", "Concrete", 0, concrete, concrete},
                {"Concrete", "Uniform", 0, concrete, uniform},
                {"Concrete", "Non-Uniform", 0, concrete, nonUniform},
                {"Uniform", "Empty", 85, uniform, empty},
                {"Uniform", "Empty String", 84, uniform, emptyString},
                {"Uniform", "Concrete", 84, uniform, concrete},
                {"Uniform", "Uniform", 0, uniform, uniform},
                {"Uniform", "Non-Uniform", 40, uniform, nonUniform},
                {"Non-Uniform", "Empty", 45, nonUniform, empty},
                {"Non-Uniform", "Empty String", 45, nonUniform, emptyString},
                {"Non-Uniform", "Concrete", 44, nonUniform, concrete},
                {"Non-Uniform", "Uniform", 0, nonUniform, uniform},
                {"Non-Uniform", "Non-Uniform", 0, nonUniform, nonUniform},
                {"Unbalanced Uniform 0", "Empty", 64, unbalancedUniform0, empty},
                {"Unbalanced Uniform 0", "Empty String", 64, unbalancedUniform0, emptyString},
                {"Unbalanced Uniform 0", "Concrete", 64, unbalancedUniform0, concrete},
                {"Unbalanced Uniform 0", "Uniform", 0, unbalancedUniform0, uniform},
                {"Unbalanced Uniform 0", "Non-Uniform", 36, unbalancedUniform0, nonUniform},
                {"Unbalanced Uniform 1", "Empty", 64, unbalancedUniform1, empty},
                {"Unbalanced Uniform 1", "Empty String", 64, unbalancedUniform1, emptyString},
                {"Unbalanced Uniform 1", "Concrete", 64, unbalancedUniform1, concrete},
                {"Unbalanced Uniform 1", "Uniform", 0, unbalancedUniform1, uniform},
                {"Unbalanced Uniform 1", "Non-Uniform", 36, unbalancedUniform1, nonUniform},
                {"Unbalanced Uniform 2", "Empty", 64, unbalancedUniform2, empty},
                {"Unbalanced Uniform 2", "Empty String", 64, unbalancedUniform2, emptyString},
                {"Unbalanced Uniform 2", "Concrete", 64, unbalancedUniform2, concrete},
                {"Unbalanced Uniform 2", "Uniform", 0, unbalancedUniform2, uniform},
                {"Unbalanced Uniform 2", "Non-Uniform", 36, unbalancedUniform2, nonUniform},
                {"Unbalanced Non-Uniform 0", "Empty", 37, unbalancedNonUniform0, empty},
                {"Unbalanced Non-Uniform 0", "Empty String", 37, unbalancedNonUniform0, emptyString},
                {"Unbalanced Non-Uniform 0", "Concrete", 37, unbalancedNonUniform0, concrete},
                {"Unbalanced Non-Uniform 0", "Uniform", 0, unbalancedNonUniform0, uniform},
                {"Unbalanced Non-Uniform 0", "Non-Uniform", 9, unbalancedNonUniform0, nonUniform},
                {"Unbalanced Non-Uniform 1", "Empty", 37, unbalancedNonUniform1, empty},
                {"Unbalanced Non-Uniform 1", "Empty String", 37, unbalancedNonUniform1, emptyString},
                {"Unbalanced Non-Uniform 1", "Concrete", 37, unbalancedNonUniform1, concrete},
                {"Unbalanced Non-Uniform 1", "Uniform", 0, unbalancedNonUniform1, uniform},
                {"Unbalanced Non-Uniform 1", "Non-Uniform", 9, unbalancedNonUniform1, nonUniform},
                {"Unbalanced Non-Uniform 2", "Empty", 37, unbalancedNonUniform2, empty},
                {"Unbalanced Non-Uniform 2", "Empty String", 37, unbalancedNonUniform2, emptyString},
                {"Unbalanced Non-Uniform 2", "Concrete", 37, unbalancedNonUniform2, concrete},
                {"Unbalanced Non-Uniform 2", "Uniform", 0, unbalancedNonUniform2, uniform},
                {"Unbalanced Non-Uniform 2", "Non-Uniform", 9, unbalancedNonUniform2, nonUniform}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        resultAutomaton = baseAutomaton.minus(argAutomaton);
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
