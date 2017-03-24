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
import java.util.HashSet;
import java.util.Set;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_WeightedAutomaton_When_Determinizing {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;

    private static int initialBoundLength;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.determinize() -> Expected MC = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

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
        WeightedAutomaton nonUniformDelete01 = nonUniform_delete_01();
        WeightedAutomaton nonUniformDelete12 = nonUniform_delete_12();
        WeightedAutomaton replaceUnbalancedNonUniform0 = replaceUnbalancedNonUniform0();
        WeightedAutomaton replaceUnbalancedNonUniform1 = replaceUnbalancedNonUniform1();
        WeightedAutomaton replaceUnbalancedNonUniform2 = replaceUnbalancedNonUniform2();

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 1, concrete},
                {"Uniform", 85, uniform},
                {"Non-Uniform", 45, nonUniform},
                {"Unbalanced Uniform 0", 64, unbalancedUniform0},
                {"Unbalanced Uniform 1", 64, unbalancedUniform1},
                {"Unbalanced Uniform 2", 64, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", 37, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", 37, unbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2", 37, unbalancedNonUniform2},
                {"Non-Uniform delete(0,1)", 37, nonUniformDelete01},
                {"Non-Uniform delete(1,2)", 37, nonUniformDelete12},
                {"Unbalanced Non-Uniform 0 replace('B', 'A')", 37, replaceUnbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1 replace('B', 'A')", 37, replaceUnbalancedNonUniform1},
                {"Unbalanced Non-Uniform 2 replace('B', 'A')", 37, replaceUnbalancedNonUniform2}
        });
    }

    @Before
    public void setup() {
        // *** act ***
        automaton.setDeterministic(false);
        BasicWeightedOperations.determinize(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(automaton, initialBoundLength)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.determinize()", description);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
