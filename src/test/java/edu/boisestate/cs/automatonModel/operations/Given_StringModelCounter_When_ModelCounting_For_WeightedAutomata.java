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

    @Parameter(value = 3)
    public WeightedAutomaton automaton;
    @Parameter(value = 2)
    public int boundingLength;
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
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton nonUniform = getNonUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton boundUniform = getUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);
        WeightedAutomaton boundNonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, initialBoundLength);

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, initialBoundLength, empty},
                {"Empty", 0, -1, empty},
                {"Empty String", 1, initialBoundLength, emptyString},
                {"Empty String", 1, -1, emptyString},
                {"Concrete", 1, initialBoundLength, concrete},
                {"Concrete", 1, -1, concrete},
                {"Uniform", 85, initialBoundLength, uniform},
                {"Uniform", 85, -1, boundUniform},
                {"Non-uniform", 45, initialBoundLength, nonUniform},
                {"Non-uniform", 45, -1, boundNonUniform},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        if (this.boundingLength < 0) {
            this.modelCount = ModelCount(this.automaton);
        } else {
            this.modelCount = ModelCount(this.automaton,
                                         this.boundingLength);
        }
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** assert ***
        assertThat(this.modelCount.intValue(),
                   is(equalTo(this.expectedModelCount)));
    }
}
