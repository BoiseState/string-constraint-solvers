package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedAutomaton;
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
public class Given_WeightedPreciseSetLength_For_WeightedAutomata {

    @Parameter(value = 3)
    public int length;
    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.setLength({3}) -> Expected MC = {1}")
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

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty, 0},
                {"Empty", 0, empty, 1},
                {"Empty", 0, empty, 2},
                {"Empty", 0, empty, 3},
                {"Empty String", 1, emptyString, 0},
                {"Empty String", 1, emptyString, 1},
                {"Empty String", 1, emptyString, 2},
                {"Empty String", 1, emptyString, 3},
                {"Concrete", 2, concrete, 0},
                {"Concrete", 2, concrete, 1},
                {"Concrete", 2, concrete, 2},
                {"Concrete", 2, concrete, 3},
                {"Uniform", 85, uniform, 0},
                {"Uniform", 85, uniform, 1},
                {"Uniform", 85, uniform, 2},
                {"Uniform", 85, uniform, 3},
                {"Non-uniform", 45, nonUniform, 0},
                {"Non-uniform", 45, nonUniform, 1},
                {"Non-uniform", 45, nonUniform, 2},
                {"Non-uniform", 45, nonUniform, 3}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedPreciseSetLength operation = new WeightedPreciseSetLength(length);

        // *** act ***
        resultAutomaton = operation.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String message = String.format("<%s Automaton>.setLength(%d)", description, length);
        assertThat(message, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
