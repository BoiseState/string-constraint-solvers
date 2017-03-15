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
public class Given_IgnoreCase_For_BoundedAutomata {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 1)
    public int expectedModelCount;
    private Automaton resultAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.ignoreCase() - Expected" +
                       " MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D,a-d");
        int initialBoundLength = 3;

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton nonUniform = getNonUniformBoundAutomaton(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty},
                {"Empty String", 1, emptyString},
                {"Concrete", 8, concrete},
                {"Uniform", 585, uniform},
                {"Non-uniform", 326, nonUniform}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        IgnoreCase operation = new IgnoreCase();

        // *** act ***
        this.resultAutomaton = operation.op(this.automaton);
        this.resultAutomaton.minimize();

    }

    @Test
    public void it_should_have_the_correct_number_of_accepted_strings() {
        // *** act ***
        int modelCount = StringModelCounter.ModelCount(this.resultAutomaton)
                                           .intValue();

        // *** assert ***
        assertThat(modelCount, is(equalTo(this.expectedModelCount)));
    }
}
