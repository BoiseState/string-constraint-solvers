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
import java.util.Set;

import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.addAutomatonTestInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseDelete_When_PerformingOperation_On_UnbounedAutomaton {

    private static Alphabet alphabet;
    private static Automaton automaton;
    private static int initialBoundLength;
    @Parameter(value = 5)
    public int end;
    @Parameter(value = 1)
    public int expectedNumAcceptStates;
    @Parameter(value = 3)
    public int expectedNumAcceptedStrings;
    @Parameter // first data value (0) is default
    public int expectedNumStates;
    @Parameter(value = 2)
    public int expectedNumTransitions;
    @Parameter(value = 4)
    public int start;
    private Automaton actual;

    static {
        // initialize alphabet and initial bound length
        alphabet = new Alphabet("A-D");
        initialBoundLength = 4;

        // create unbounded automaton
        automaton = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                 .repeat();
        automaton.minimize();
    }

    @Parameters(name = "{index}: delete({4}, {5}) = [{0} states, {1} " +
                       "accepting states, {2} transistions, {3} accepted " +
                       "strings]")
    public static Iterable<Object[]> data() {

        return Arrays.asList(addAutomatonTestInstance(1, 1, 1, 341, 0, 0),

                             addAutomatonTestInstance(1, 1, 1, 85, 0, 1),
                             addAutomatonTestInstance(1, 1, 1, 21, 0, 2),
                             addAutomatonTestInstance(1, 1, 1, 5, 0, 3),
                             addAutomatonTestInstance(1, 1, 1, 1, 0, 4),
                             addAutomatonTestInstance(1, 1, 1, 341, 1, 1),
                             addAutomatonTestInstance(2, 1, 2, 84, 1, 2),
                             addAutomatonTestInstance(2, 1, 2, 20, 1, 3),
                             addAutomatonTestInstance(2, 1, 2, 4, 1, 4),
                             addAutomatonTestInstance(1, 1, 1, 341, 2, 2),
                             addAutomatonTestInstance(3, 1, 3, 80, 2, 3),
                             addAutomatonTestInstance(3, 1, 3, 16, 2, 4),
                             addAutomatonTestInstance(1, 1, 1, 341, 3, 3),

                             addAutomatonTestInstance(4, 1, 4, 64, 3, 4),
                             addAutomatonTestInstance(1, 1, 1, 341, 4, 4));
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
