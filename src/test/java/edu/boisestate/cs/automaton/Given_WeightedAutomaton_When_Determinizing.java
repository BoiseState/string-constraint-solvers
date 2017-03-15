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
public class Given_WeightedAutomaton_When_Determinizing {

    @Parameter(value = 2)
    public WeightedAutomaton automaton;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 0) // first data value (0) is default
    public String baseDescription;

    private WeightedAutomaton resultAutomaton;

    private static int initialBoundLength;


    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.determinize() ->" +
                       " Expected MC = {1}")
    public static Iterable<Object[]> data() {

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        initialBoundLength = 3;

        // create automata
        WeightedAutomaton empty = makeEmpty();
        WeightedAutomaton emptyString = makeEmptyString();
        WeightedAutomaton concrete = getConcreteWeightedAutomaton(alphabet, "ABC");

        // index 1 is the bounding length (-1) for none
        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 1, concrete},
        });
    }

    @Before
    public void setup() {
        // *** act ***
        automaton.determinize();
    }

    @Test
    public void it_should_return_the_correct_model_count() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(automaton, initialBoundLength)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
