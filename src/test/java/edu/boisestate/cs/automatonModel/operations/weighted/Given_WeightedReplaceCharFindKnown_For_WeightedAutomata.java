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
public class Given_WeightedReplaceCharFindKnown_For_WeightedAutomata {

    @Parameter(value = 3)
    public char find;
    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String description;

    private WeightedAutomaton resultAutomaton;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.replace('{3}', ?) -> Expected MC = {1}")
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
                {"Empty", 0, empty, 'A'},
                {"Empty", 0, empty, 'B'},
                {"Empty", 0, empty, 'C'},
                {"Empty", 0, empty, 'D'},
                {"Empty String", 1, emptyString, 'A'},
                {"Empty String", 1, emptyString, 'B'},
                {"Empty String", 1, emptyString, 'C'},
                {"Empty String", 1, emptyString, 'D'},
                {"Concrete", 4, concrete, 'A'},
                {"Concrete", 4, concrete, 'B'},
                {"Concrete", 4, concrete, 'C'},
                {"Concrete", 4, concrete, 'D'},
                {"Uniform", 340, uniform, 'A'},
                {"Uniform", 340, uniform, 'B'},
                {"Uniform", 340, uniform, 'C'},
                {"Uniform", 340, uniform, 'D'},
                {"Non-uniform", 180, nonUniform, 'A'},
                {"Non-uniform", 180, nonUniform, 'B'},
                {"Non-uniform", 180, nonUniform, 'C'},
                {"Non-uniform", 180, nonUniform, 'D'}
        });
    }

    @Before
    public void setup() {
        // *** arrange ***
         WeightedReplaceCharFindKnown operation = new WeightedReplaceCharFindKnown(find);

        // *** act ***
        resultAutomaton = operation.op(automaton);
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        String message = String.format("<%s Automaton>.replace('%c', ?)", description, find);
        assertThat(message, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
