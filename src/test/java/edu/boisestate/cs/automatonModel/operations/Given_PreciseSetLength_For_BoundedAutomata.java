package edu.boisestate.cs.automatonModel.operations;

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

import static edu.boisestate.cs.automatonModel.operations
        .AutomatonOperationTestUtilities.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("WeakerAccess")
@RunWith(Parameterized.class)
public class Given_PreciseSetLength_For_BoundedAutomata {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public int start;
    private Automaton lengthAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.setLength({3}) - Expected" +
                       " MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet,
                                                       initialBoundLength);
        Automaton nonUniform = getNonUniformUnboundAutomaton(alphabet,
                                                             initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty, 0},
                {"Empty", 0, empty, 1},
                {"Empty", 0, empty, 2},
                {"Empty", 0, empty, 3},
                {"Empty String", 0, emptyString, 0},
                {"Empty String", 0, emptyString, 1},
                {"Empty String", 0, emptyString, 2},
                {"Empty String", 0, emptyString, 3},
                {"Concrete", 1, concrete, 0},
                {"Concrete", 1, concrete, 1},
                {"Concrete", 1, concrete, 2},
                {"Concrete", 1, concrete, 3},
                {"Uniform", 21, uniform, 0},
                {"Uniform", 20, uniform, 1},
                {"Uniform", 16, uniform, 2},
                {"Uniform", 0, uniform, 3},
                {"Non-uniform", 21, nonUniform, 0},
                {"Non-uniform", 20, nonUniform, 1},
                {"Non-uniform", 16, nonUniform, 2},
                {"Non-uniform", 0, nonUniform, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseSetLength setLength = new PreciseSetLength(this.start);

        // *** act ***
        this.lengthAutomaton = setLength.op(this.automaton);
        this.lengthAutomaton.minimize();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.lengthAutomaton)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
