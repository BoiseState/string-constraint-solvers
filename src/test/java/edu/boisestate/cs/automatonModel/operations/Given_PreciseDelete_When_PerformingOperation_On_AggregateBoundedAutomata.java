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
public class
Given_PreciseDelete_When_PerformingOperation_On_AggregateBoundedAutomata {

    private static Automaton[] automata;
    private static int initialBoundLength;
    @Parameter(value = 1)
    public int end;
    @Parameter(value = 3)
    public int[] expectedNumAcceptStates;
    @Parameter(value = 5)
    public int expectedNumAcceptedStrings;
    @Parameter(value = 2)
    public int[] expectedNumStates;
    @Parameter(value = 4)
    public int[] expectedNumTransitions;
    @Parameter // first data value (0) is default
    public int start;
    private Automaton[] actual;

    static {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        initialBoundLength = 4;

        // create unbounded automaton
        Automaton unbounded = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                           .repeat();

        // create aggregate bounded automata
        automata = new Automaton[initialBoundLength + 1];
        for (int i = 0; i <= initialBoundLength; i++) {

            // create bounded autoamton at length i
            Automaton bounding =
                    BasicAutomata.makeCharSet(alphabet.getCharSet())
                                 .repeat(i, i);
            Automaton automaton = unbounded.intersection(bounding);
            automaton.minimize();

            // set automaton at length
            automata[i] = automaton;
        }
    }

    @Parameters(name = "{index}: delete({0}, {1}) = [0][{2} states, {3} " +
                       "accepting states, {4} transistions, {5} accepted " +
                       "strings]")
    public static Iterable<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {0, 0, new int[]{1,2,3,4,5}, new int[]{1,1,1,1,1}, new int[]{0,1,2,3,4}, 341},
                {0, 1, new int[]{1,1,2,3,4}, new int[]{1,1,1,1,1}, new int[]{0,0,1,2,3}, 86},
                {0, 2, new int[]{1,1,1,2,3}, new int[]{1,1,1,1,1}, new int[]{0,0,0,1,2}, 23},
                {0, 3, new int[]{1,1,1,1,2}, new int[]{1,1,1,1,1}, new int[]{0,0,0,0,1}, 8},
                {0, 4, new int[]{1,1,1,1,1}, new int[]{1,1,1,1,1}, new int[]{0,0,0,0,0}, 5},
                {1, 1, new int[]{1,2,3,4,5}, new int[]{1,1,1,1,1}, new int[]{0,1,2,3,4}, 341},
                {1, 2, new int[]{1,2,2,3,4}, new int[]{0,1,1,1,1}, new int[]{0,1,1,2,3}, 88},
                {1, 3, new int[]{1,2,2,2,3}, new int[]{0,1,1,1,1}, new int[]{0,1,1,1,2}, 28},
                {1, 4, new int[]{1,2,2,2,2}, new int[]{0,1,1,1,1}, new int[]{0,1,1,1,1}, 16},
                {2, 2, new int[]{1,2,3,4,5}, new int[]{1,1,1,1,1}, new int[]{0,1,2,3,4}, 341},
                {2, 3, new int[]{1,1,3,3,4}, new int[]{0,0,1,1,1}, new int[]{0,0,2,2,3}, 96},
                {2, 4, new int[]{1,1,3,3,3}, new int[]{0,0,1,1,1}, new int[]{0,0,2,2,2}, 48},
                {3, 3, new int[]{1,2,3,4,5}, new int[]{1,1,1,1,1}, new int[]{0,1,2,3,4}, 341},
                {3, 4, new int[]{1,1,1,4,4}, new int[]{0,0,0,1,1}, new int[]{0,0,0,3,3}, 128},
                {4, 4, new int[]{1,2,3,4,5}, new int[]{1,1,1,1,1}, new int[]{0,1,2,3,4}, 341}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseDelete delete = new PreciseDelete(this.start, this.end);

        // *** act ***
        this.actual = new Automaton[automata.length];
        for (int i = 0; i < automata.length; i++) {
            actual[i] = delete.op(automata[i]);
        }
    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** assert ***
        int maxLength = initialBoundLength - (this.end - this.start);
        int numStrings = 0;
        for (int i = 0; i < this.actual.length; i++) {
            Set<String> strings =
                    AutomatonTestUtilities.getStrings(this.actual[i],
                                                      0,
                                                      maxLength);
            numStrings += strings.size();
        }

        assertThat(numStrings, is(equalTo(this.expectedNumAcceptedStrings)));
    }

    @Test
    public void it_should_have_the_correct_number_of_accepting_states() {
        // *** assert ***
        for (int i = 0; i < this.actual.length; i++) {
            int numStates = this.actual[i].getAcceptStates().size();
            assertThat(numStates, is(equalTo(this.expectedNumAcceptStates[i])));
        }
    }

    @Test
    public void it_should_have_the_correct_number_of_states() {
        // *** assert ***
        for (int i = 0; i < this.actual.length; i++) {
            int numStates = this.actual[i].getNumberOfStates();
            assertThat(numStates, is(equalTo(this.expectedNumStates[i])));
        }
    }

    @Test
    public void it_should_have_the_correct_number_of_transitions() {
        // *** assert ***
        for (int i = 0; i < this.actual.length; i++) {
            int numStates = this.actual[i].getNumberOfTransitions();
            assertThat(numStates, is(equalTo(this.expectedNumTransitions[i])));
        }
    }
}
