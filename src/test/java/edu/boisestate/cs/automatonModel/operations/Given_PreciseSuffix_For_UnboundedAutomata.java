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
public class Given_PreciseSuffix_For_UnboundedAutomata {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public int start;
    private Automaton resultAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.prefix({3}) - Expected" +
                       " MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");

        // get automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformUnboundedAutomaton(alphabet);
        Automaton nonUniform = getNonUniformUnboundedAutomaton(alphabet);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty, 0},
                {"Empty", 0, empty, 1},
                {"Empty", 0, empty, 2},
                {"Empty", 0, empty, 3},
                {"Empty String", 1, emptyString, 0},
                {"Empty String", 0, emptyString, 1},
                {"Empty String", 0, emptyString, 2},
                {"Empty String", 0, emptyString, 3},
                {"Concrete", 1, concrete, 0},
                {"Concrete", 1, concrete, 1},
                {"Concrete", 1, concrete, 2},
                {"Concrete", 1, concrete, 3},
                {"Uniform", 85, uniform, 0},
                {"Uniform", 21, uniform, 1},
                {"Uniform", 5, uniform, 2},
                {"Uniform", 1, uniform, 3},
                {"Non-uniform", 45, nonUniform, 0},
                {"Non-uniform", 21, nonUniform, 1},
                {"Non-uniform", 5, nonUniform, 2},
                {"Non-uniform", 1, nonUniform, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseSuffix operation = new PreciseSuffix(this.start);

        // *** act ***
        this.resultAutomaton = operation.op(this.automaton);
        this.resultAutomaton.minimize();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int length = 3 - start;
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton, length)
                                           .intValue();

        // *** assert ***
        String reason = String.format("<%s Automaton>.suffix(%d)", description, start);
        assertThat(reason, modelCount, is(equalTo(this.expectedModelCount)));
    }
}
