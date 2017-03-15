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
public class Given_WeightedPreciseDelete_For_WeightedAutomata {

    @Parameter(value = 3)
    public int start;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.delete({3}, {4}) ->" +
                       " Expected MC = {1}")
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
                {"Empty", 0, empty, 0, 0},
                {"Empty", 0, empty, 0, 1},
                {"Empty", 0, empty, 0, 2},
                {"Empty", 0, empty, 0, 3},
                {"Empty", 0, empty, 1, 1},
                {"Empty", 0, empty, 1, 2},
                {"Empty", 0, empty, 1, 3},
                {"Empty", 0, empty, 2, 2},
                {"Empty", 0, empty, 2, 3},
                {"Empty", 0, empty, 3, 3},
                {"Empty String", 1, emptyString, 0, 0},
                {"Empty String", 1, emptyString, 0, 1},
                {"Empty String", 1, emptyString, 0, 2},
                {"Empty String", 1, emptyString, 0, 3},
                {"Empty String", 0, emptyString, 1, 1},
                {"Empty String", 0, emptyString, 1, 2},
                {"Empty String", 0, emptyString, 1, 3},
                {"Empty String", 0, emptyString, 2, 2},
                {"Empty String", 0, emptyString, 2, 3},
                {"Empty String", 0, emptyString, 3, 3},
                {"Concrete", 1, concrete, 0, 0},
                {"Concrete", 1, concrete, 0, 1},
                {"Concrete", 1, concrete, 0, 2},
                {"Concrete", 1, concrete, 0, 3},
                {"Concrete", 1, concrete, 1, 1},
                {"Concrete", 1, concrete, 1, 2},
                {"Concrete", 1, concrete, 1, 3},
                {"Concrete", 1, concrete, 2, 2},
                {"Concrete", 1, concrete, 2, 3},
                {"Concrete", 1, concrete, 3, 3},
                {"Uniform", 85, uniform, 0, 0},
                {"Uniform", 85, uniform, 0, 1},
                {"Uniform", 85, uniform, 0, 2},
                {"Uniform", 85, uniform, 0, 3},
                {"Uniform", 84, uniform, 1, 1},
                {"Uniform", 84, uniform, 1, 2},
                {"Uniform", 84, uniform, 1, 3},
                {"Uniform", 80, uniform, 2, 2},
                {"Uniform", 80, uniform, 2, 3},
                {"Uniform", 64, uniform, 3, 3},
                {"Non-uniform", 45, nonUniform, 0, 0},
                {"Non-uniform", 45, nonUniform, 0, 1},
                {"Non-uniform", 45, nonUniform, 0, 2},
                {"Non-uniform", 45, nonUniform, 0, 3},
                {"Non-uniform", 45, nonUniform, 1, 1},
                {"Non-uniform", 45, nonUniform, 1, 2},
                {"Non-uniform", 45, nonUniform, 1, 3},
                {"Non-uniform", 44, nonUniform, 2, 2},
                {"Non-uniform", 44, nonUniform, 2, 3},
                {"Non-uniform", 37, nonUniform, 3, 3}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedPreciseDelete operation = new WeightedPreciseDelete(start, end);

        // *** act ***
        resultAutomaton = operation.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String message = String.format("<%s Automaton>.delete(%d, %d)", description, start, end);
        assertThat(message, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
