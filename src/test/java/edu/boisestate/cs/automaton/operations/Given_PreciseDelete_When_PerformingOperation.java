package edu.boisestate.cs.automaton.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseDelete_When_PerformingOperation {

    static Map<String, Automaton> automatonMap;
    @Parameter // first data value (0) is default
    public String automatonString;
    @Parameter(value = 2)
    public int end;
    @Parameter(value = 4)
    public int expectedNumAcceptStates;
    @Parameter(value = 3)
    public int expectedNumStates;
    @Parameter(value = 5)
    public int expectedNumTransitions;
    @Parameter(value = 1)
    public int start;
    private Automaton actual;

    static {
        // initilize automaton map
        automatonMap = new HashMap<>();

        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-B");
        int boundLength = 3;

        // create base automaton
        Automaton base = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                      .repeat();

        // create arg automaton
        Automaton arg = BasicAutomata.makeChar('A');

        // create unbounded automaton
        Automaton unbounded = base.concatenate(arg);
        unbounded.minimize();

        // create bounded automaton
        Automaton boundeding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                            .repeat(0, boundLength);
        Automaton bounded = base.intersection(boundeding);
        bounded = bounded.concatenate(arg);
        bounded.minimize();

        // add automata to map
        automatonMap.put("unbounded", unbounded);
        automatonMap.put("bounded", bounded);
    }

    @Parameters(name = "{index}: delete({0}, {1}, {2}) = [{3}, {4}, {5}]")
    public static Iterable<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {"unbounded", 0, 1, 1, 1, 1},
                {"unbounded", 0, 2, 1, 1, 1},
                {"unbounded", 0, 3, 1, 1, 1},
                {"unbounded", 0, 4, 1, 1, 1},
                {"unbounded", 1, 2, 1, 1, 1},
                {"unbounded", 1, 3, 1, 1, 1},
                {"unbounded", 1, 4, 1, 1, 1},
                {"unbounded", 2, 3, 1, 1, 1},
                {"unbounded", 2, 4, 1, 1, 1},
                {"unbounded", 3, 4, 1, 1, 1},
                {"bounded", 0, 1, 6, 4, 8},
                {"bounded", 0, 2, 4, 3, 4},
                {"bounded", 0, 3, 2, 2, 1},
                {"bounded", 0, 4, 1, 1, 0},
                {"bounded", 1, 2, 5, 3, 5},
                {"bounded", 1, 3, 3, 2, 2},
                {"bounded", 1, 4, 2, 1, 1},
                {"bounded", 2, 3, 4, 2, 3},
                {"bounded", 2, 4, 3, 1, 2},
                {"bounded", 3, 4, 4, 1, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        ImpreciseDelete delete = new ImpreciseDelete(this.start, this.end);
        Automaton automaton = automatonMap.get(this.automatonString);

        // *** act ***
        this.actual = delete.op(automaton);
    }

    @Test
    public void it_should_have_the_correct_number_of_states() {
        // *** assert ***
        int numStates = this.actual.getNumberOfStates();
        assertThat(numStates, is(equalTo(this.expectedNumStates)));
    }

    @Test
    public void it_should_have_the_correct_number_of_accepting_states() {
        // *** assert ***
        int numStates = this.actual.getAcceptStates().size();
        assertThat(numStates, is(equalTo(this.expectedNumAcceptStates)));
    }

    @Test
    public void it_should_have_the_correct_number_of_transitions() {
        // *** assert ***
        int numStates = this.actual.getNumberOfTransitions();
        assertThat(numStates, is(equalTo(this.expectedNumTransitions)));
    }
}
