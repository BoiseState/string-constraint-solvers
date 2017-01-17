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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
abstract public class UnboundedAutomatonOperationTest {

    protected static Automaton automaton;
    protected static int initialBoundLength;
    protected static Alphabet alphabet;
    protected static Iterable<Object[]> testParameters;
    @Parameter(value = 1)
    public int expectedNumAcceptStates;
    @Parameter(value = 3)
    public int expectedNumAcceptedStrings;
    @Parameter // first data value (0) is default
    public int expectedNumStates;
    @Parameter(value = 2)
    public int expectedNumTransitions;
    protected Automaton actual;

    static {
        // initialize default alphabet
        alphabet = new Alphabet("A-D");

        // initialize default initial bound length if not set
        initialBoundLength = 4;

        // initialize default unbounded automaton
        automaton = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        automaton.minimize();
    }

    @Parameters(name = "{index}: {0} states, {1} accepting states, {2} transistions, {3} accepted strings")
    public static Iterable<Object[]> data() {

        if (testParameters == null) {
            return new ArrayList<Object[]>();
        }

        return testParameters;
    }

    @Before
    public void setup() {

        // *** act ***
        this.actual = this.produceAutomaton();
    }

    protected abstract Automaton produceAutomaton();

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** assert ***
        int numStrings = this.getActualNumStrings();
        assertThat(numStrings, is(equalTo(this.expectedNumAcceptedStrings)));
    }

    protected abstract int getActualNumStrings();

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
