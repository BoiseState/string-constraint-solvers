package edu.boisestate.cs.automatonModel.operations;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigInteger;
import java.util.Arrays;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .ModelCount;
import static edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_StringModelCounter_When_ModelCounting_For_WeightedAutomata {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 0) // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    private BigInteger modelCount;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: Automaton <{0}>, Bounding Length {2}, Expected MC {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");
        WeightedAutomaton boundUniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton boundNonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton unbalancedUniform0 = unbalanced_Uniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedUniform1 = unbalanced_Uniform_WeightedAutomaton_1();
        WeightedAutomaton unbalancedUniform2 = unbalanced_Uniform_WeightedAutomaton_2();
        WeightedAutomaton unbalancedNonUniform0 = unbalanced_NonUniform_WeightedAutomaton_0();
        WeightedAutomaton unbalancedNonUniform1 = unbalanced_NonUniform_WeightedAutomaton_1();

        // create unbalanced automata


        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 1, concrete},
                {"Uniform", 85, boundUniform},
                {"Non-uniform", 45, boundNonUniform},
                {"Unbalanced Uniform 0", 64, unbalancedUniform0},
                {"Unbalanced Uniform 1", 64, unbalancedUniform1},
                {"Unbalanced Uniform 2", 64, unbalancedUniform2},
                {"Unbalanced Non-Uniform 0", 28, unbalancedNonUniform0},
                {"Unbalanced Non-Uniform 1", 28, unbalancedNonUniform1}
        });
    }

    @Before
    public void setup() {
        /* *** act ****/
        this.modelCount = ModelCount(this.automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** assert ***
        assertThat(this.modelCount.intValue(), is(equalTo(this.expectedModelCount)));
    }
}
