package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonTestUtilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseDelete_When_PerformingOperation_On_BoundedAutomaton {

    private static Automaton automaton;
    private static int initialBoundLength;
    @Parameter(value = 1)
    public int end;
    @Parameter(value = 3)
    public int expectedNumAcceptStates;
    @Parameter(value = 5)
    public int expectedNumAcceptedStrings;
    @Parameter(value = 2)
    public int expectedNumStates;
    @Parameter(value = 4)
    public int expectedNumTransitions;
    @Parameter // first data value (0) is default
    public int start;
    private Automaton actual;

    static {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        initialBoundLength = 4;

        // create unbounded automaton
        Automaton unbounded = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                           .repeat();

        // create bounded automaton
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, initialBoundLength);
        Automaton bounded = unbounded.intersection(bounding);
        bounded.minimize();

        // store automaton as static variable
        automaton = bounded;
    }


    @Parameters(name = "{index}: delete({0}, {1}) = [0][{2} states, {3} " +
                       "accepting states, {4} transistions, {5} accepted " +
                       "strings]")
    public static Iterable<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {0, 0, 5, 5, 4, 341},
                {0, 1, 4, 4, 3, 85},
                {0, 2, 3, 3, 2, 21},
                {0, 3, 2, 2, 1, 5},
                {0, 4, 1, 1, 0, 1},
                {1, 1, 5, 5, 4, 341},
                {1, 2, 4, 3, 3, 85},
                {1, 3, 3, 2, 2, 21},
                {1, 4, 2, 1, 1, 5},
                {2, 2, 5, 5, 4, 341},
                {2, 3, 4, 2, 3, 85},
                {2, 4, 3, 1, 2, 21},
                {3, 3, 5, 5, 4, 341},
                {3, 4, 4, 1, 3, 85},
                {4, 4, 5, 5, 4, 341}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseDelete delete = new PreciseDelete(this.start, this.end);

        // *** act ***
        this.actual = delete.op(automaton);
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** assert ***
        int maxLength = initialBoundLength - (this.end - this.start);
        Set<String> strings = AutomatonTestUtilities.getStrings(this.actual,
                                                                0,
                                                                maxLength);
        int numStrings = strings.size();
        assertThat(numStrings, is(equalTo(this.expectedNumAcceptedStrings)));
    }

    @Test
    public void it_should_have_the_correct_number_of_accepting_states() {
        // *** assert ***
        int numStates = this.actual.getAcceptStates().size();
        assertThat(numStates, is(equalTo(this.expectedNumAcceptStates)));
    }

    @Test
    public void it_should_have_the_correct_number_of_states() {
        // *** assert ***
        int numStates = this.actual.getNumberOfStates();
        assertThat(numStates, is(equalTo(this.expectedNumStates)));
    }

    @Test
    public void it_should_have_the_correct_number_of_transitions() {
        // *** assert ***
        int numStates = this.actual.getNumberOfTransitions();
        assertThat(numStates, is(equalTo(this.expectedNumTransitions)));
    }
}
