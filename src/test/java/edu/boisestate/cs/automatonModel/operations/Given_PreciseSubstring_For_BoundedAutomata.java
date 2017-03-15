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
public class Given_PreciseSubstring_For_BoundedAutomata {

    @Parameter(value = 2)
    public Automaton automaton;
    @Parameter // first data value (0) is default
    public String description;
    @Parameter(value = 4)
    public int end;
    @Parameter(value = 1)
    public int expectedModelCount;
    @Parameter(value = 3)
    public int start;
    private Automaton resultAutomaton;

    @SuppressWarnings("Duplicates")
    @Parameters(name = "{index}: <{0} Automaton>.substring({3}, {4}) - Expected" +
                       " MC = {1}")
    public static Iterable<Object[]> data() {
        // initialize alphabet and initial bound length
        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        // get Automata
        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = getConcreteAutomaton(alphabet, "ABC");
        Automaton uniform = getUniformBoundedAutomaton(alphabet, initialBoundLength);
        Automaton nonUniform = getNonUniformBoundAutomaton(alphabet, initialBoundLength);

        return Arrays.asList(new Object[][]{
                {"Empty", 0, empty, 0, 0},
                {"Empty", 0, empty, 0, 1},
                {"Empty", 0, empty, 0, 2},
                {"Empty", 0, empty, 0, 3},
                {"Empty", 0, empty, 1, 1},
                {"Empty", 0, empty, 1, 2},
                {"Empty", 0, empty, 1, 3},
                {"Empty", 0, empty, 2, 2},
                {"Empty", 0, empty, 2, 3},
                {"Empty", 0, empty, 3, 3},
                {"Empty String", 1, emptyString, 0, 0},
                {"Empty String", 0, emptyString, 0, 1},
                {"Empty String", 0, emptyString, 0, 2},
                {"Empty String", 0, emptyString, 0, 3},
                {"Empty String", 0, emptyString, 1, 1},
                {"Empty String", 0, emptyString, 1, 2},
                {"Empty String", 0, emptyString, 1, 3},
                {"Empty String", 0, emptyString, 2, 2},
                {"Empty String", 0, emptyString, 2, 3},
                {"Empty String", 0, emptyString, 3, 3},
                {"Concrete", 1, concrete, 0, 0},
                {"Concrete", 1, concrete, 0, 1},
                {"Concrete", 1, concrete, 0, 2},
                {"Concrete", 1, concrete, 0, 3},
                {"Concrete", 1, concrete, 1, 1},
                {"Concrete", 1, concrete, 1, 2},
                {"Concrete", 1, concrete, 1, 3},
                {"Concrete", 1, concrete, 2, 2},
                {"Concrete", 1, concrete, 2, 3},
                {"Concrete", 1, concrete, 3, 3},
                {"Uniform", 1, uniform, 0, 0},
                {"Uniform", 4, uniform, 0, 1},
                {"Uniform", 16, uniform, 0, 2},
                {"Uniform", 64, uniform, 0, 3},
                {"Uniform", 1, uniform, 1, 1},
                {"Uniform", 4, uniform, 1, 2},
                {"Uniform", 16, uniform, 1, 3},
                {"Uniform", 1, uniform, 2, 2},
                {"Uniform", 4, uniform, 2, 3},
                {"Uniform", 1, uniform, 3, 3},
                {"Non-uniform", 1, nonUniform, 0, 0},
                {"Non-uniform", 4, nonUniform, 0, 1},
                {"Non-uniform", 16, nonUniform, 0, 2},
                {"Non-uniform", 37, nonUniform, 0, 3},
                {"Non-uniform", 1, nonUniform, 1, 1},
                {"Non-uniform", 4, nonUniform, 1, 2},
                {"Non-uniform", 16, nonUniform, 1, 3},
                {"Non-uniform", 1, nonUniform, 2, 2},
                {"Non-uniform", 4, nonUniform, 2, 3},
                {"Non-uniform", 1, nonUniform, 3, 3}
        });
    }

    @Before
    public void setup() {

        // *** arrange ***
        PreciseSubstring operation = new PreciseSubstring(this.start, this.end);

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
